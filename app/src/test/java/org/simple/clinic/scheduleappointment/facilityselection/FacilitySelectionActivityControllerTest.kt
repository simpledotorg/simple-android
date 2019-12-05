package org.simple.clinic.scheduleappointment.facilityselection

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
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
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.facility.change.FacilitiesUpdateType
import org.simple.clinic.facility.change.FacilityChangeConfig
import org.simple.clinic.facility.change.FacilityListItem
import org.simple.clinic.facility.change.FacilityListItemBuilder
import org.simple.clinic.location.Coordinates
import org.simple.clinic.location.LocationRepository
import org.simple.clinic.location.LocationUpdate
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Distance
import org.simple.clinic.util.RuntimePermissionResult
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestElapsedRealtimeClock
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Duration
import java.util.concurrent.TimeUnit

@RunWith(JUnitParamsRunner::class)
class FacilitySelectionActivityControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val uiEvents = PublishSubject.create<UiEvent>()!!
  private val screen = mock<FacilitySelectionActivity>()
  private val facilityRepository = mock<FacilityRepository>()
  private val userSession = mock<UserSession>()
  private val locationRepository = mock<LocationRepository>()
  private val listItemBuilder = mock<FacilityListItemBuilder>()
  private val testComputationScheduler = TestScheduler()
  private val user = PatientMocker.loggedInUser()
  private val elapsedRealtimeClock = TestElapsedRealtimeClock()

  private val configTemplate = FacilityChangeConfig(
      locationListenerExpiry = Duration.ofSeconds(0),
      locationUpdateInterval = Duration.ofSeconds(0),
      proximityThresholdForNearbyFacilities = Distance.ofKilometers(0.0),
      staleLocationThreshold = Duration.ofSeconds(0))
  private val configProvider = BehaviorSubject.createDefault(configTemplate)

  private lateinit var controller: FacilitySelectionActivityController

  @Before
  fun setUp() {
    // To control time used by Observable.timer().
    RxJavaPlugins.setComputationSchedulerHandler { testComputationScheduler }

    // Location updates are listened on a background thread.
    RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }

    controller = FacilitySelectionActivityController(
        facilityRepository = facilityRepository,
        userSession = userSession,
        locationRepository = locationRepository,
        configProvider = configProvider,
        elapsedRealtimeClock = elapsedRealtimeClock,
        listItemBuilder = listItemBuilder)

    whenever(userSession.requireLoggedInUser()).thenReturn(Observable.just(user))

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when screen starts then facilities UI models should be created`() {
    val facility1 = PatientMocker.facility()
    val facility2 = PatientMocker.facility()
    val facilities = listOf(facility1, facility2)
    whenever(facilityRepository.facilitiesInCurrentGroup(user = user)).thenReturn(Observable.just(facilities, facilities))

    val searchQuery = ""
    val facilityListItems = emptyList<FacilityListItem>()
    whenever(listItemBuilder.build(any(), any(), any(), any())).thenReturn(facilityListItems)

    uiEvents.onNext(FacilitySelectionUserLocationUpdated(LocationUpdate.Unavailable))
    uiEvents.onNext(FacilitySelectionSearchQueryChanged(searchQuery))

    verify(listItemBuilder, times(2)).build(
        facilities = facilities,
        searchQuery = searchQuery,
        userLocation = null,
        proximityThreshold = configTemplate.proximityThresholdForNearbyFacilities)
    verify(screen).updateFacilities(facilityListItems, FacilitiesUpdateType.FIRST_UPDATE)
    verify(screen).updateFacilities(facilityListItems, FacilitiesUpdateType.SUBSEQUENT_UPDATE)
  }

  @Test
  fun `when search query is changed then the query should be used for fetching filtered facilities`() {
    val facilities = listOf(
        PatientMocker.facility(name = "Facility 1"),
        PatientMocker.facility(name = "Facility 2"))
    whenever(facilityRepository.facilitiesInCurrentGroup(any(), eq(user))).thenReturn(Observable.just(facilities))

    uiEvents.onNext(ScreenCreated())
    uiEvents.onNext(FacilitySelectionUserLocationUpdated(LocationUpdate.Unavailable))
    uiEvents.onNext(FacilitySelectionSearchQueryChanged(query = "F"))
    uiEvents.onNext(FacilitySelectionSearchQueryChanged(query = "Fa"))
    uiEvents.onNext(FacilitySelectionSearchQueryChanged(query = "Fac"))

    verify(facilityRepository).facilitiesInCurrentGroup(searchQuery = "F", user = user)
    verify(facilityRepository).facilitiesInCurrentGroup(searchQuery = "Fa", user = user)
    verify(facilityRepository).facilitiesInCurrentGroup(searchQuery = "Fac", user = user)
  }

  @Test
  fun `when facility is selected then it should be passed back as result`() {
    val newFacility = PatientMocker.facility()

    uiEvents.onNext(FacilitySelected(newFacility))

    verify(screen).sendSelectedFacility(newFacility.uuid)
  }

  @Test
  fun `when screen is started and location permission is available then location should be fetched`() {
    configProvider.onNext(configTemplate.copy(locationUpdateInterval = Duration.ofDays(5)))
    whenever(locationRepository.streamUserLocation(any(), any())).thenReturn(Observable.never())

    uiEvents.onNext(ScreenCreated())
    uiEvents.onNext(FacilitySelectionLocationPermissionChanged(RuntimePermissionResult.GRANTED))

    verify(locationRepository).streamUserLocation(updateInterval = eq(Duration.ofDays(5)), updateScheduler = any())
  }

  @Test
  @Parameters(method = "params for permission denials")
  fun `when screen is started and location permission was denied then location should not be fetched and facilities should be shown`(
      deniedResult: RuntimePermissionResult
  ) {
    val facilities = listOf(
        PatientMocker.facility(name = "Facility 1"),
        PatientMocker.facility(name = "Facility 2"))
    whenever(facilityRepository.facilitiesInCurrentGroup(any(), any())).thenReturn(Observable.just(facilities))
    whenever(locationRepository.streamUserLocation(any(), any())).thenReturn(Observable.never())

    uiEvents.onNext(ScreenCreated())
    uiEvents.onNext(FacilitySelectionSearchQueryChanged(""))
    uiEvents.onNext(FacilitySelectionLocationPermissionChanged(deniedResult))

    verify(locationRepository, never()).streamUserLocation(any(), any())
    verify(screen).updateFacilities(any(), any())
  }

  @Suppress("unused")
  fun `params for permission denials`(): List<RuntimePermissionResult> {
    return listOf(RuntimePermissionResult.DENIED, RuntimePermissionResult.NEVER_ASK_AGAIN)
  }

  @Test
  fun `when screen is started then location should only be read once`() {
    configProvider.onNext(configTemplate.copy(locationListenerExpiry = Duration.ofSeconds(5)))

    val facilities = listOf(
        PatientMocker.facility(name = "Facility 1"),
        PatientMocker.facility(name = "Facility 2"))
    whenever(facilityRepository.facilitiesInCurrentGroup(any(), any())).thenReturn(Observable.just(facilities))

    val timeSinceBootWhenRecorded = Duration.ofMillis(elapsedRealtimeClock.millis())
    whenever(locationRepository.streamUserLocation(any(), any())).thenReturn(
        Observable.just(
            LocationUpdate.Available(Coordinates(0.0, 0.0), timeSinceBootWhenRecorded),
            LocationUpdate.Unavailable,
            LocationUpdate.Available(Coordinates(0.0, 0.0), timeSinceBootWhenRecorded)))

    uiEvents.onNext(ScreenCreated())
    uiEvents.onNext(FacilitySelectionSearchQueryChanged(""))
    uiEvents.onNext(FacilitySelectionLocationPermissionChanged(RuntimePermissionResult.GRANTED))

    testComputationScheduler.advanceTimeBy(6, TimeUnit.SECONDS)

    verify(locationRepository).streamUserLocation(any(), any())
    verify(screen, times(1)).updateFacilities(any(), any())
  }

  @Test
  fun `when the user's location updates are received then only one recent update should be read`() {
    val config = configTemplate.copy(staleLocationThreshold = Duration.ofMinutes(10))
    configProvider.onNext(config)

    val facilities = listOf(PatientMocker.facility(name = "Facility 1"))
    whenever(facilityRepository.facilitiesInCurrentGroup(any(), any())).thenReturn(Observable.just(facilities))

    val locationUpdates = PublishSubject.create<LocationUpdate>()
    whenever(locationRepository.streamUserLocation(any(), any())).thenReturn(locationUpdates)

    uiEvents.onNext(ScreenCreated())
    uiEvents.onNext(FacilitySelectionSearchQueryChanged(""))
    uiEvents.onNext(FacilitySelectionLocationPermissionChanged(RuntimePermissionResult.GRANTED))

    val locationOlderThanStaleThreshold = LocationUpdate.Available(
        location = Coordinates(0.0, 0.0),
        timeSinceBootWhenRecorded = Duration.ofMillis(elapsedRealtimeClock.millis()))

    elapsedRealtimeClock.advanceBy(config.staleLocationThreshold + Duration.ofSeconds(1))

    locationUpdates.onNext(locationOlderThanStaleThreshold)
    verify(screen, never()).updateFacilities(any(), any())

    locationUpdates.onNext(locationOlderThanStaleThreshold)
    verify(screen, never()).updateFacilities(any(), any())

    elapsedRealtimeClock.advanceBy(config.staleLocationThreshold)

    val locationNewerThanStaleThreshold = LocationUpdate.Available(
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
    uiEvents.onNext(FacilitySelectionLocationPermissionChanged(RuntimePermissionResult.GRANTED))
    verify(screen).showProgressIndicator()

    testComputationScheduler.advanceTimeBy(secondsSpentWaitingForLocation, TimeUnit.SECONDS)
    verify(screen).hideProgressIndicator()
  }

  @Test
  fun `when screen starts and location permission is available then progress indicator should be shown`() {
    whenever(locationRepository.streamUserLocation(any(), any())).thenReturn(Observable.never())

    uiEvents.onNext(ScreenCreated())
    uiEvents.onNext(FacilitySelectionLocationPermissionChanged(RuntimePermissionResult.GRANTED))
    uiEvents.onNext(FacilitySelectionLocationPermissionChanged(RuntimePermissionResult.GRANTED))

    verify(screen, times(1)).showProgressIndicator()
  }

  @Test
  @Parameters(method = "params for permission denials")
  fun `when screen starts and location permission was denied then progress indicator should not be shown`(
      deniedResult: RuntimePermissionResult
  ) {
    whenever(locationRepository.streamUserLocation(any(), any())).thenReturn(Observable.never())

    uiEvents.onNext(ScreenCreated())
    uiEvents.onNext(FacilitySelectionLocationPermissionChanged(deniedResult))

    verify(screen, never()).showProgressIndicator()
  }

  @Test
  @Parameters(method = "params for location updates")
  fun `when a location update is received then progress indicator should be hidden`(
      locationUpdate: LocationUpdate
  ) {
    whenever(locationRepository.streamUserLocation(any(), any())).thenReturn(Observable.just(locationUpdate))

    uiEvents.onNext(ScreenCreated())
    uiEvents.onNext(FacilitySelectionLocationPermissionChanged(RuntimePermissionResult.GRANTED))

    verify(screen).hideProgressIndicator()
  }

  @Suppress("unused")
  fun `params for location updates`() = listOf(
      LocationUpdate.Unavailable,
      LocationUpdate.Available(location = Coordinates(0.0, 0.0), timeSinceBootWhenRecorded = Duration.ofNanos(0))
  )

  @Test
  fun `when both facilities and location are fetched only then should facilities be shown`() {
    configProvider.onNext(configTemplate.copy(locationListenerExpiry = Duration.ofSeconds(5)))

    val facilities = listOf(
        PatientMocker.facility(name = "Facility 1"),
        PatientMocker.facility(name = "Facility 2"))
    whenever(facilityRepository.facilitiesInCurrentGroup(any(), any())).thenReturn(Observable.just(facilities))

    val locationUpdates = PublishSubject.create<LocationUpdate>()
    whenever(locationRepository.streamUserLocation(any(), any())).thenReturn(locationUpdates)

    uiEvents.run {
      onNext(ScreenCreated())
      onNext(FacilitySelectionSearchQueryChanged(""))
      onNext(FacilitySelectionLocationPermissionChanged(RuntimePermissionResult.GRANTED))
    }
    verify(screen, never()).updateFacilities(any(), any())

    locationUpdates.onNext(LocationUpdate.Available(Coordinates(0.0, 0.0), timeSinceBootWhenRecorded = Duration.ofNanos(0)))
    verify(screen).updateFacilities(any(), any())
  }

  @Test
  fun `when facilities are fetched, but location listener expires then facilities should still be shown`() {
    configProvider.onNext(configTemplate.copy(locationListenerExpiry = Duration.ofSeconds(5)))

    val facilities = listOf(
        PatientMocker.facility(name = "Facility 1"),
        PatientMocker.facility(name = "Facility 2"))
    whenever(facilityRepository.facilitiesInCurrentGroup(any(), any())).thenReturn(Observable.just(facilities))
    whenever(locationRepository.streamUserLocation(any(), any())).thenReturn(Observable.never())

    uiEvents.run {
      onNext(ScreenCreated())
      onNext(FacilitySelectionSearchQueryChanged("f"))
      onNext(FacilitySelectionLocationPermissionChanged(RuntimePermissionResult.GRANTED))
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

    uiEvents.onNext(FacilitySelectionUserLocationUpdated(LocationUpdate.Unavailable))
    inOrder.verify(screen).showToolbarWithSearchField()
  }

  @Suppress("unused")
  fun `params for user location updates`(): List<Any> {
    return listOf(
        LocationUpdate.Unavailable,
        LocationUpdate.Available(location = Coordinates(0.0, 0.0), timeSinceBootWhenRecorded = Duration.ofNanos(0)))
  }

}
