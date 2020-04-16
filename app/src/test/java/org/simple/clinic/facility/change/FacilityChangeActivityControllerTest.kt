package org.simple.clinic.facility.change

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.TestData
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.facility.change.FacilitiesUpdateType.FIRST_UPDATE
import org.simple.clinic.facility.change.FacilitiesUpdateType.SUBSEQUENT_UPDATE
import org.simple.clinic.location.Coordinates
import org.simple.clinic.location.LocationRepository
import org.simple.clinic.location.LocationUpdate
import org.simple.clinic.location.LocationUpdate.Available
import org.simple.clinic.location.LocationUpdate.Unavailable
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Distance
import org.simple.clinic.util.Just
import org.simple.clinic.util.RuntimePermissionResult.DENIED
import org.simple.clinic.util.RuntimePermissionResult.GRANTED
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestElapsedRealtimeClock
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Duration
import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.SECONDS

@RunWith(JUnitParamsRunner::class)
class FacilityChangeActivityControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val uiEvents = PublishSubject.create<UiEvent>()!!
  private val screen = mock<FacilityChangeActivity>()
  private val facilityRepository = mock<FacilityRepository>()
  private val userSession = mock<UserSession>()
  private val locationRepository = mock<LocationRepository>()
  private val listItemBuilder = mock<FacilityListItemBuilder>()
  private val testComputationScheduler = TestScheduler()

  private val user = TestData.loggedInUser()
  val currentFacility = TestData.facility(UUID.fromString("6dc536d9-b460-4143-9b3b-7caedf17c0d9"))
  private val elapsedRealtimeClock = TestElapsedRealtimeClock()

  private val configTemplate = FacilityChangeConfig(
      locationListenerExpiry = Duration.ofSeconds(0),
      locationUpdateInterval = Duration.ofSeconds(0),
      proximityThresholdForNearbyFacilities = Distance.ofKilometers(0.0),
      staleLocationThreshold = Duration.ofSeconds(0))
  private val configProvider = BehaviorSubject.createDefault(configTemplate)

  private lateinit var controller: FacilityChangeActivityController

  @Before
  fun setUp() {
    // To control time used by Observable.timer().
    RxJavaPlugins.setComputationSchedulerHandler { testComputationScheduler }

    // Location updates are listened on a background thread.
    RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }

    controller = FacilityChangeActivityController(
        facilityRepository = facilityRepository,
        userSession = userSession,
        locationRepository = locationRepository,
        configProvider = configProvider,
        elapsedRealtimeClock = elapsedRealtimeClock,
        listItemBuilder = listItemBuilder)

    whenever(userSession.requireLoggedInUser()).thenReturn(Observable.just(user))
    whenever(userSession.loggedInUser()).thenReturn(Observable.just(Just(user)))
    whenever(facilityRepository.currentFacility(user)) doReturn Observable.just(currentFacility)

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when screen starts then facilities UI models should be created`() {
    val facility1 = TestData.facility()
    val facility2 = TestData.facility()
    val facilities = listOf(facility1, facility2)
    whenever(facilityRepository.facilitiesInCurrentGroup(user = user)).thenReturn(Observable.just(facilities, facilities))

    val searchQuery = ""
    val facilityListItems = emptyList<FacilityListItem>()
    whenever(listItemBuilder.build(any(), any(), any(), any())).thenReturn(facilityListItems)

    uiEvents.onNext(FacilityChangeUserLocationUpdated(Unavailable))
    uiEvents.onNext(FacilityChangeSearchQueryChanged(searchQuery))

    verify(listItemBuilder, times(2)).build(
        facilities = facilities,
        searchQuery = searchQuery,
        userLocation = null,
        proximityThreshold = configTemplate.proximityThresholdForNearbyFacilities)
    verify(screen).updateFacilities(facilityListItems, FIRST_UPDATE)
    verify(screen).updateFacilities(facilityListItems, SUBSEQUENT_UPDATE)
  }

  @Test
  fun `when search query is changed then the query should be used for fetching filtered facilities`() {
    val facilities = listOf(
        TestData.facility(name = "Facility 1"),
        TestData.facility(name = "Facility 2"))
    whenever(facilityRepository.facilitiesInCurrentGroup(any(), eq(user))).thenReturn(Observable.just(facilities))

    uiEvents.onNext(ScreenCreated())
    uiEvents.onNext(FacilityChangeUserLocationUpdated(Unavailable))
    uiEvents.onNext(FacilityChangeSearchQueryChanged(query = "F"))
    uiEvents.onNext(FacilityChangeSearchQueryChanged(query = "Fa"))
    uiEvents.onNext(FacilityChangeSearchQueryChanged(query = "Fac"))

    verify(facilityRepository).facilitiesInCurrentGroup(searchQuery = "F", user = user)
    verify(facilityRepository).facilitiesInCurrentGroup(searchQuery = "Fa", user = user)
    verify(facilityRepository).facilitiesInCurrentGroup(searchQuery = "Fac", user = user)
  }

  @Test
  fun `when a new facility is selected then the confirmation sheet to change facility should open`() {
    //given
    val newFacility = TestData.facility(UUID.fromString("ce22e8b1-eba2-463f-8e91-0c237ebebf6b"))

    //when
    uiEvents.onNext(ScreenCreated())
    uiEvents.onNext(FacilityChangeClicked(newFacility))

    //then
    verify(screen).openConfirmationSheet(newFacility)
  }

  @Test
  fun `when the same facility is selected then the sheet should close`() {
    //given
    val newFacility = currentFacility

    //when
    uiEvents.onNext(ScreenCreated())
    uiEvents.onNext(FacilityChangeClicked(newFacility))

    //then
    verify(screen).goBack()
  }

  @Test
  fun `when screen is started and location permission is available then location should be fetched`() {
    configProvider.onNext(configTemplate.copy(locationUpdateInterval = Duration.ofDays(5)))
    whenever(locationRepository.streamUserLocation(any(), any())).thenReturn(Observable.never())

    uiEvents.onNext(ScreenCreated())
    uiEvents.onNext(FacilityChangeLocationPermissionChanged(GRANTED))

    verify(locationRepository).streamUserLocation(updateInterval = eq(Duration.ofDays(5)), updateScheduler = any())
  }

  @Test
  fun `when screen is started and location permission was denied then location should not be fetched and facilities should be shown`() {
    val facilities = listOf(
        TestData.facility(name = "Facility 1"),
        TestData.facility(name = "Facility 2"))
    whenever(facilityRepository.facilitiesInCurrentGroup(any(), any())).thenReturn(Observable.just(facilities))
    whenever(locationRepository.streamUserLocation(any(), any())).thenReturn(Observable.never())

    uiEvents.onNext(ScreenCreated())
    uiEvents.onNext(FacilityChangeSearchQueryChanged(""))
    uiEvents.onNext(FacilityChangeLocationPermissionChanged(DENIED))

    verify(locationRepository, never()).streamUserLocation(any(), any())
    verify(screen).updateFacilities(any(), any())
  }

  @Test
  fun `when screen is started then location should only be read once`() {
    configProvider.onNext(configTemplate.copy(locationListenerExpiry = Duration.ofSeconds(5)))

    val facilities = listOf(
        TestData.facility(name = "Facility 1"),
        TestData.facility(name = "Facility 2"))
    whenever(facilityRepository.facilitiesInCurrentGroup(any(), any())).thenReturn(Observable.just(facilities))

    val timeSinceBootWhenRecorded = Duration.ofMillis(elapsedRealtimeClock.millis())
    whenever(locationRepository.streamUserLocation(any(), any())).thenReturn(
        Observable.just(
            Available(Coordinates(0.0, 0.0), timeSinceBootWhenRecorded),
            Unavailable,
            Available(Coordinates(0.0, 0.0), timeSinceBootWhenRecorded)))

    uiEvents.onNext(ScreenCreated())
    uiEvents.onNext(FacilityChangeSearchQueryChanged(""))
    uiEvents.onNext(FacilityChangeLocationPermissionChanged(GRANTED))

    testComputationScheduler.advanceTimeBy(6, SECONDS)

    verify(locationRepository).streamUserLocation(any(), any())
    verify(screen, times(1)).updateFacilities(any(), any())
  }

  @Test
  fun `when the user's location updates are received then only one recent update should be read`() {
    val config = configTemplate.copy(staleLocationThreshold = Duration.ofMinutes(10))
    configProvider.onNext(config)

    val facilities = listOf(TestData.facility(name = "Facility 1"))
    whenever(facilityRepository.facilitiesInCurrentGroup(any(), any())).thenReturn(Observable.just(facilities))

    val locationUpdates = PublishSubject.create<LocationUpdate>()
    whenever(locationRepository.streamUserLocation(any(), any())).thenReturn(locationUpdates)

    uiEvents.onNext(ScreenCreated())
    uiEvents.onNext(FacilityChangeSearchQueryChanged(""))
    uiEvents.onNext(FacilityChangeLocationPermissionChanged(GRANTED))

    val locationOlderThanStaleThreshold = Available(
        location = Coordinates(0.0, 0.0),
        timeSinceBootWhenRecorded = Duration.ofMillis(elapsedRealtimeClock.millis()))

    elapsedRealtimeClock.advanceBy(config.staleLocationThreshold + Duration.ofSeconds(1))

    locationUpdates.onNext(locationOlderThanStaleThreshold)
    verify(screen, never()).updateFacilities(any(), any())

    locationUpdates.onNext(locationOlderThanStaleThreshold)
    verify(screen, never()).updateFacilities(any(), any())

    elapsedRealtimeClock.advanceBy(config.staleLocationThreshold)

    val locationNewerThanStaleThreshold = Available(
        location = Coordinates(0.0, 0.0),
        timeSinceBootWhenRecorded = Duration.ofMillis(elapsedRealtimeClock.millis()))

    locationUpdates.onNext(locationNewerThanStaleThreshold)
    verify(screen).updateFacilities(any(), any())
  }

  @Test
  @Parameters("5", "6", "7", "8")
  fun `when location is being fetched then it should expire after a fixed time duration`(
      secondsSpentWaitingForLocation: Long
  ) {
    configProvider.onNext(configTemplate.copy(locationListenerExpiry = Duration.ofSeconds(5)))

    whenever(facilityRepository.facilitiesInCurrentGroup(any(), any())).thenReturn(Observable.just(emptyList()))
    whenever(locationRepository.streamUserLocation(any(), any())).thenReturn(Observable.never())

    uiEvents.onNext(ScreenCreated())
    uiEvents.onNext(FacilityChangeLocationPermissionChanged(GRANTED))
    verify(screen).showProgressIndicator()

    testComputationScheduler.advanceTimeBy(secondsSpentWaitingForLocation, SECONDS)
    verify(screen).hideProgressIndicator()
  }

  @Test
  fun `when screen starts and location permission is available then progress indicator should be shown`() {
    whenever(locationRepository.streamUserLocation(any(), any())).thenReturn(Observable.never())

    uiEvents.onNext(ScreenCreated())
    uiEvents.onNext(FacilityChangeLocationPermissionChanged(GRANTED))
    uiEvents.onNext(FacilityChangeLocationPermissionChanged(GRANTED))

    verify(screen, times(1)).showProgressIndicator()
  }

  @Test
  fun `when screen starts and location permission was denied then progress indicator should not be shown`() {
    whenever(locationRepository.streamUserLocation(any(), any())).thenReturn(Observable.never())

    uiEvents.onNext(ScreenCreated())
    uiEvents.onNext(FacilityChangeLocationPermissionChanged(DENIED))

    verify(screen, never()).showProgressIndicator()
  }

  @Test
  @Parameters(method = "params for location updates")
  fun `when a location update is received then progress indicator should be hidden`(
      locationUpdate: LocationUpdate
  ) {
    whenever(locationRepository.streamUserLocation(any(), any())).thenReturn(Observable.just(locationUpdate))

    uiEvents.onNext(ScreenCreated())
    uiEvents.onNext(FacilityChangeLocationPermissionChanged(GRANTED))

    verify(screen).hideProgressIndicator()
  }

  @Suppress("unused")
  fun `params for location updates`() = listOf(
      Unavailable,
      Available(location = Coordinates(0.0, 0.0), timeSinceBootWhenRecorded = Duration.ofNanos(0))
  )

  @Test
  fun `when both facilities and location are fetched only then should facilities be shown`() {
    configProvider.onNext(configTemplate.copy(locationListenerExpiry = Duration.ofSeconds(5)))

    val facilities = listOf(
        TestData.facility(name = "Facility 1"),
        TestData.facility(name = "Facility 2"))
    whenever(facilityRepository.facilitiesInCurrentGroup(any(), any())).thenReturn(Observable.just(facilities))

    val locationUpdates = PublishSubject.create<LocationUpdate>()
    whenever(locationRepository.streamUserLocation(any(), any())).thenReturn(locationUpdates)

    uiEvents.run {
      onNext(ScreenCreated())
      onNext(FacilityChangeSearchQueryChanged(""))
      onNext(FacilityChangeLocationPermissionChanged(GRANTED))
    }
    verify(screen, never()).updateFacilities(any(), any())

    locationUpdates.onNext(Available(Coordinates(0.0, 0.0), timeSinceBootWhenRecorded = Duration.ofNanos(0)))
    verify(screen).updateFacilities(any(), any())
  }

  @Test
  fun `when facilities are fetched, but location listener expires then facilities should still be shown`() {
    configProvider.onNext(configTemplate.copy(locationListenerExpiry = Duration.ofSeconds(5)))

    val facilities = listOf(
        TestData.facility(name = "Facility 1"),
        TestData.facility(name = "Facility 2"))
    whenever(facilityRepository.facilitiesInCurrentGroup(any(), any())).thenReturn(Observable.just(facilities))
    whenever(locationRepository.streamUserLocation(any(), any())).thenReturn(Observable.never())

    uiEvents.run {
      onNext(ScreenCreated())
      onNext(FacilityChangeSearchQueryChanged("f"))
      onNext(FacilityChangeLocationPermissionChanged(GRANTED))
    }
    verify(screen, never()).updateFacilities(any(), any())

    testComputationScheduler.advanceTimeBy(6, TimeUnit.SECONDS)
    verify(screen).updateFacilities(any(), any())
  }

  @Test
  @Parameters(method = "params for user location updates")
  fun `search field should only be shown when a user location update is received`(
      locationUpdate: LocationUpdate
  ) {
    uiEvents.onNext(ScreenCreated())

    val inOrder = inOrder(screen)
    inOrder.verify(screen).showToolbarWithoutSearchField()

    uiEvents.onNext(FacilityChangeUserLocationUpdated(Unavailable))
    inOrder.verify(screen).showToolbarWithSearchField()
  }

  @Suppress("unused")
  fun `params for user location updates`(): List<Any> {
    return listOf(
        Unavailable,
        Available(location = Coordinates(0.0, 0.0), timeSinceBootWhenRecorded = Duration.ofNanos(0)))
  }
}
