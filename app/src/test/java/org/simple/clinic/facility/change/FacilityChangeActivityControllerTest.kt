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
import org.simple.clinic.location.Coordinates
import org.simple.clinic.location.LocationUpdate
import org.simple.clinic.location.LocationUpdate.Available
import org.simple.clinic.location.LocationUpdate.Unavailable
import org.simple.clinic.location.ScreenLocationUpdates
import org.simple.clinic.platform.util.RuntimePermissionResult.DENIED
import org.simple.clinic.platform.util.RuntimePermissionResult.GRANTED
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Distance
import org.simple.clinic.util.Just
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import java.time.Duration
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class FacilityChangeActivityControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val uiEvents = PublishSubject.create<UiEvent>()!!
  private val screen = mock<FacilityChangeActivity>()
  private val facilityRepository = mock<FacilityRepository>()
  private val userSession = mock<UserSession>()
  private val listItemBuilder = mock<FacilityListItemBuilder>()

  private val user = TestData.loggedInUser()
  val currentFacility = TestData.facility(UUID.fromString("6dc536d9-b460-4143-9b3b-7caedf17c0d9"))

  private val configTemplate = FacilityChangeConfig(
      locationListenerExpiry = Duration.ofSeconds(0),
      locationUpdateInterval = Duration.ofSeconds(0),
      proximityThresholdForNearbyFacilities = Distance.ofKilometers(0.0),
      staleLocationThreshold = Duration.ofSeconds(0))
  private val configProvider = BehaviorSubject.createDefault(configTemplate)
  private val screenLocationUpdates = mock<ScreenLocationUpdates>()

  private lateinit var controller: FacilityChangeActivityController

  @Before
  fun setUp() {
    controller = FacilityChangeActivityController(
        facilityRepository = facilityRepository,
        userSession = userSession,
        configProvider = configProvider,
        listItemBuilder = listItemBuilder,
        screenLocationUpdates = screenLocationUpdates
    )

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
    whenever(screenLocationUpdates.streamUserLocation(any(), any(), any())).thenReturn(Observable.just(Unavailable))

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
    verify(screen, times(2)).updateFacilities(facilityListItems)
  }

  @Test
  fun `when search query is changed then the query should be used for fetching filtered facilities`() {
    val facilities = listOf(
        TestData.facility(name = "Facility 1"),
        TestData.facility(name = "Facility 2"))
    whenever(facilityRepository.facilitiesInCurrentGroup(any(), eq(user))).thenReturn(Observable.just(facilities))
    whenever(screenLocationUpdates.streamUserLocation(any(), any(), any())).thenReturn(Observable.just(Unavailable))

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
    whenever(screenLocationUpdates.streamUserLocation(any(), any(), any())).thenReturn(Observable.just(Unavailable))
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
    whenever(screenLocationUpdates.streamUserLocation(any(), any(), any())).thenReturn(Observable.just(Unavailable))
    val newFacility = currentFacility

    //when
    uiEvents.onNext(ScreenCreated())
    uiEvents.onNext(FacilityChangeClicked(newFacility))

    //then
    verify(screen).goBack()
  }

  @Test
  fun `when screen starts and location permission is available then progress indicator should be shown`() {
    whenever(screenLocationUpdates.streamUserLocation(any(), any(), any())).thenReturn(Observable.just(Unavailable))

    uiEvents.onNext(ScreenCreated())
    uiEvents.onNext(FacilityChangeLocationPermissionChanged(GRANTED))
    uiEvents.onNext(FacilityChangeLocationPermissionChanged(GRANTED))

    verify(screen, times(1)).showProgressIndicator()
  }

  @Test
  fun `when screen starts and location permission was denied then progress indicator should not be shown`() {
    whenever(screenLocationUpdates.streamUserLocation(any(), any(), any())).thenReturn(Observable.just(Unavailable))

    uiEvents.onNext(ScreenCreated())
    uiEvents.onNext(FacilityChangeLocationPermissionChanged(DENIED))

    verify(screen, never()).showProgressIndicator()
  }

  @Test
  @Parameters(method = "params for location updates")
  fun `when a location update is received then progress indicator should be hidden`(
      locationUpdate: LocationUpdate
  ) {
    whenever(screenLocationUpdates.streamUserLocation(any(), any(), any())).thenReturn(Observable.just(locationUpdate))

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
    whenever(screenLocationUpdates.streamUserLocation(any(), any(), any())).thenReturn(locationUpdates)

    uiEvents.run {
      onNext(ScreenCreated())
      onNext(FacilityChangeSearchQueryChanged(""))
      onNext(FacilityChangeLocationPermissionChanged(GRANTED))
    }
    verify(screen, never()).updateFacilities(any())

    locationUpdates.onNext(Available(Coordinates(0.0, 0.0), timeSinceBootWhenRecorded = Duration.ofNanos(0)))
    verify(screen).updateFacilities(any())
  }

  @Test
  fun `when facilities are fetched, but location is not available then facilities should still be shown`() {
    configProvider.onNext(configTemplate.copy(locationListenerExpiry = Duration.ofSeconds(5)))

    val facilities = listOf(
        TestData.facility(name = "Facility 1"),
        TestData.facility(name = "Facility 2"))
    whenever(facilityRepository.facilitiesInCurrentGroup(any(), any())).thenReturn(Observable.just(facilities))
    whenever(screenLocationUpdates.streamUserLocation(any(), any(), any())).thenReturn(Observable.just(Unavailable))

    uiEvents.run {
      onNext(ScreenCreated())
      onNext(FacilityChangeSearchQueryChanged("f"))
      onNext(FacilityChangeLocationPermissionChanged(GRANTED))
    }
    verify(screen).updateFacilities(any())
  }

  @Test
  @Parameters(method = "params for user location updates")
  fun `search field should only be shown when a user location update is received`(
      locationUpdate: LocationUpdate
  ) {
    whenever(screenLocationUpdates.streamUserLocation(any(), any(), any())).thenReturn(Observable.just(locationUpdate))
    uiEvents.onNext(ScreenCreated())

    val inOrder = inOrder(screen)
    inOrder.verify(screen).showToolbarWithoutSearchField()
    inOrder.verify(screen).showToolbarWithSearchField()
  }

  @Suppress("unused")
  fun `params for user location updates`(): List<Any> {
    return listOf(
        Unavailable,
        Available(location = Coordinates(0.0, 0.0), timeSinceBootWhenRecorded = Duration.ofNanos(0)))
  }
}
