package org.simple.clinic.registration.facility

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.FacilityPullResult
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.facility.FacilitySync
import org.simple.clinic.facility.change.FacilitiesUpdateType.FIRST_UPDATE
import org.simple.clinic.facility.change.FacilitiesUpdateType.SUBSEQUENT_UPDATE
import org.simple.clinic.facility.change.FacilityListItem
import org.simple.clinic.facility.change.FacilityListItemBuilder
import org.simple.clinic.location.Coordinates
import org.simple.clinic.location.LocationUpdate
import org.simple.clinic.location.LocationUpdate.Unavailable
import org.simple.clinic.location.ScreenLocationUpdates
import org.simple.clinic.registration.RegistrationConfig
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Distance
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.util.toOptional
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import java.util.UUID

class RegistrationFacilitySelectionScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val uiEvents = PublishSubject.create<UiEvent>()!!
  private val screen = mock<RegistrationFacilitySelectionScreen>()
  private val facilitySync = mock<FacilitySync>()
  private val facilityRepository = mock<FacilityRepository>()
  private val userSession = mock<UserSession>()
  private val currentTime = Instant.parse("2018-01-01T00:00:00Z")
  private val utcClock = TestUtcClock(currentTime)
  private val listItemBuilder = mock<FacilityListItemBuilder>()
  private val screenLocationUpdates = mock<ScreenLocationUpdates>()

  private lateinit var controller: RegistrationFacilitySelectionScreenController

  private val configTemplate = RegistrationConfig(
      locationListenerExpiry = Duration.ofSeconds(0),
      locationUpdateInterval = Duration.ofSeconds(0),
      proximityThresholdForNearbyFacilities = Distance.ofKilometers(0.0),
      staleLocationThreshold = Duration.ofSeconds(0))
  private val configProvider = BehaviorSubject.createDefault(configTemplate)

  @Before
  fun setUp() {
    controller = RegistrationFacilitySelectionScreenController(
        facilitySync = facilitySync,
        facilityRepository = facilityRepository,
        userSession = userSession,
        configProvider = configProvider.firstOrError(),
        listItemBuilder = listItemBuilder,
        screenLocationUpdates = screenLocationUpdates,
        utcClock = utcClock
    )

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when screen is started then facilities should be fetched if they are empty`() {
    val facilities = emptyList<Facility>()
    whenever(facilityRepository.facilities()).thenReturn(Observable.just(facilities))
    whenever(facilityRepository.recordCount()).thenReturn(Observable.just(facilities.size))
    whenever(facilitySync.pullWithResult()).thenReturn(Single.just(FacilityPullResult.Success))
    whenever(screenLocationUpdates.streamUserLocation(any(), any(), any())).thenReturn(Observable.just(Unavailable))

    uiEvents.onNext(ScreenCreated())

    verify(facilitySync).pullWithResult()
  }

  @Test
  fun `when screen is started, location should be fetched`() {
    configProvider.onNext(configTemplate.copy(locationUpdateInterval = Duration.ofDays(5)))
    whenever(facilityRepository.recordCount()).thenReturn(Observable.never())
    whenever(screenLocationUpdates.streamUserLocation(any(), any(), any())).thenReturn(Observable.never())

    uiEvents.onNext(ScreenCreated())

    verify(screenLocationUpdates).streamUserLocation(
        // This should be `Duration.ofDays(5)`, but because of a problem
        // with the current way of setting up controllers, the updated
        // configuration isn't available to the controller when it
        // subscribes to the stream. This has been changed in another
        // branch, and will get fixed when this commit gets pulled there.
        updateInterval = Duration.ZERO,
        timeout = configTemplate.locationListenerExpiry,
        discardOlderThan = configTemplate.staleLocationThreshold
    )
  }

  @Test
  fun `while facilities and location are being fetched then progress indicator should be shown`() {
    val facilities = emptyList<Facility>()
    whenever(facilityRepository.facilities()).thenReturn(Observable.just(facilities))
    whenever(facilityRepository.recordCount()).thenReturn(Observable.just(facilities.size))
    whenever(facilitySync.pullWithResult()).thenReturn(Single.just(FacilityPullResult.Success))
    whenever(screenLocationUpdates.streamUserLocation(any(), any(), any())).thenReturn(Observable.just(Unavailable))

    uiEvents.onNext(ScreenCreated())

    val inOrder = inOrder(screen)
    inOrder.verify(screen).showProgressIndicator()
    inOrder.verify(screen).hideProgressIndicator()
  }

  @Test
  fun `when both facilities and location are fetched only then should facilities be shown`() {
    configProvider.onNext(configTemplate.copy(locationListenerExpiry = Duration.ofSeconds(5)))

    val facilities = listOf(
        TestData.facility(name = "Facility 1"),
        TestData.facility(name = "Facility 2"))
    whenever(facilityRepository.facilities(any())).thenReturn(Observable.just(facilities))
    whenever(facilityRepository.recordCount()).thenReturn(Observable.just(facilities.size))
    whenever(facilitySync.pullWithResult()).thenReturn(Single.just(FacilityPullResult.Success))

    val locationUpdates = PublishSubject.create<LocationUpdate>()
    whenever(screenLocationUpdates.streamUserLocation(any(), any(), any())).thenReturn(locationUpdates)

    uiEvents.run {
      onNext(ScreenCreated())
      onNext(RegistrationFacilitySearchQueryChanged(""))
    }
    verify(screen, never()).updateFacilities(any(), any())

    locationUpdates.onNext(LocationUpdate.Available(Coordinates(0.0, 0.0), timeSinceBootWhenRecorded = Duration.ofNanos(0)))
    verify(screen).updateFacilities(any(), any())
  }

  @Test
  fun `when facilities are fetched, but location is unavailable then facilities should still be shown`() {
    configProvider.onNext(configTemplate.copy(locationListenerExpiry = Duration.ofSeconds(5)))

    val facilities = listOf(
        TestData.facility(name = "Facility 1"),
        TestData.facility(name = "Facility 2"))
    whenever(facilityRepository.facilities(any())).thenReturn(Observable.just(facilities))
    whenever(facilityRepository.recordCount()).thenReturn(Observable.just(facilities.size))
    whenever(screenLocationUpdates.streamUserLocation(any(), any(), any())).thenReturn(Observable.just(Unavailable))

    uiEvents.onNext(ScreenCreated())
    uiEvents.onNext(RegistrationFacilitySearchQueryChanged("f"))

    verify(screen).updateFacilities(any(), any())
  }

  @Test
  fun `when screen is started then facilities should not be fetched if they are already available`() {
    val facilities = listOf(TestData.facility())
    whenever(facilityRepository.facilities()).thenReturn(Observable.just(facilities))
    whenever(facilityRepository.recordCount()).thenReturn(Observable.just(facilities.size))
    whenever(facilitySync.pullWithResult()).thenReturn(Single.just(FacilityPullResult.Success))
    whenever(screenLocationUpdates.streamUserLocation(any(), any(), any())).thenReturn(Observable.just(Unavailable))

    uiEvents.onNext(ScreenCreated())

    verify(facilitySync, never()).pullWithResult()
  }

  @Test
  fun `when search query is changed then the query should be used for fetching filtered facilities`() {
    val facilities = listOf(
        TestData.facility(name = "Facility 1"),
        TestData.facility(name = "Facility 2"))
    whenever(facilityRepository.facilities(any())).thenReturn(Observable.just(facilities))
    whenever(facilityRepository.recordCount()).thenReturn(Observable.just(facilities.size))
    whenever(facilitySync.pullWithResult()).thenReturn(Single.just(FacilityPullResult.Success))
    whenever(screenLocationUpdates.streamUserLocation(any(), any(), any())).thenReturn(Observable.never())

    uiEvents.onNext(ScreenCreated())
    uiEvents.onNext(RegistrationFacilityUserLocationUpdated(Unavailable))
    uiEvents.onNext(RegistrationFacilitySearchQueryChanged(query = "F"))
    uiEvents.onNext(RegistrationFacilitySearchQueryChanged(query = "Fa"))
    uiEvents.onNext(RegistrationFacilitySearchQueryChanged(query = "Fac"))

    verify(facilityRepository).facilities("F")
    verify(facilityRepository).facilities("Fa")
    verify(facilityRepository).facilities("Fac")
  }

  @Test
  fun `when fetching facilities fails then an error should be shown`() {
    whenever(screenLocationUpdates.streamUserLocation(any(), any(), any())).thenReturn(Observable.just(Unavailable))
    whenever(facilityRepository.facilities()).thenReturn(Observable.just(emptyList()))
    whenever(facilityRepository.recordCount()).thenReturn(Observable.just(0))
    whenever(facilitySync.pullWithResult())
        .thenReturn(Single.just(FacilityPullResult.UnexpectedError))
        .thenReturn(Single.just(FacilityPullResult.NetworkError))

    uiEvents.onNext(RegistrationFacilityUserLocationUpdated(Unavailable))
    uiEvents.onNext(RegistrationFacilitySearchQueryChanged(query = ""))
    uiEvents.onNext(RegistrationFacilitySelectionRetryClicked())
    uiEvents.onNext(RegistrationFacilitySelectionRetryClicked())

    verify(screen).showNetworkError()
    verify(screen).showUnexpectedError()
  }

  @Test
  fun `when retry is clicked then the error should be cleared and facilities should be fetched again`() {
    whenever(facilityRepository.recordCount()).thenReturn(Observable.just(1))
    whenever(facilityRepository.facilities()).thenReturn(Observable.never())
    whenever(facilitySync.pullWithResult()).thenReturn(Single.just(FacilityPullResult.Success))
    whenever(screenLocationUpdates.streamUserLocation(any(), any(), any())).thenReturn(Observable.just(Unavailable))

    uiEvents.onNext(RegistrationFacilityUserLocationUpdated(Unavailable))
    uiEvents.onNext(RegistrationFacilitySelectionRetryClicked())

    verify(screen).hideError()
    verify(screen).showProgressIndicator()
    verify(facilitySync).pullWithResult()
    verify(screen).hideProgressIndicator()
  }

  @Test
  fun `when facilities are received then their UI models for facility list should be created`() {
    val facility1 = TestData.facility(name = "Facility 1")
    val facility2 = TestData.facility(name = "Facility 2")
    val facilities = listOf(facility1, facility2)
    whenever(facilityRepository.facilities()).thenReturn(Observable.just(facilities, facilities))
    whenever(facilityRepository.recordCount()).thenReturn(Observable.just(facilities.size, facilities.size))
    whenever(screenLocationUpdates.streamUserLocation(any(), any(), any())).thenReturn(Observable.just(Unavailable))

    val searchQuery = ""
    val facilityListItems = emptyList<FacilityListItem>()
    whenever(listItemBuilder.build(any(), any(), any(), any())).thenReturn(facilityListItems)

    uiEvents.onNext(ScreenCreated())
    uiEvents.onNext(RegistrationFacilityUserLocationUpdated(Unavailable))
    uiEvents.onNext(RegistrationFacilitySearchQueryChanged(searchQuery))

    verify(listItemBuilder, times(2)).build(
        facilities = facilities,
        searchQuery = searchQuery,
        userLocation = null,
        proximityThreshold = configTemplate.proximityThresholdForNearbyFacilities)
    verify(screen).updateFacilities(facilityListItems, FIRST_UPDATE)
    verify(screen).updateFacilities(facilityListItems, SUBSEQUENT_UPDATE)
  }

  @Test
  fun `when a facility is clicked then show confirm facility sheet`() {
    val ongoingEntry = OngoingRegistrationEntry(
        uuid = UUID.fromString("eb0a9bc0-b24d-4f3f-9990-aa05e217be1a"),
        phoneNumber = "1234567890",
        fullName = "Ashok",
        pin = "1234")
    whenever(userSession.ongoingRegistrationEntry()).thenReturn(ongoingEntry.toOptional())
    whenever(userSession.saveOngoingRegistrationEntryAsUser(currentTime)).thenReturn(Completable.complete())

    val facility1 = TestData.facility(name = "Hoshiarpur", uuid = UUID.fromString("5cf9d744-7f34-4633-aa46-a6c7e7542060"))
    uiEvents.onNext(RegistrationFacilityClicked(facility1))

    verify(screen).showConfirmFacilitySheet(facility1.uuid, facility1.name)
  }

  @Test
  fun `when a facility is confirmed then the ongoing entry should be updated with selected facility and the user should be logged in`() {
    val ongoingEntry = OngoingRegistrationEntry(
        uuid = UUID.fromString("252ef188-318c-443c-9c0c-37644e84bb6d"),
        phoneNumber = "1234567890",
        fullName = "Ashok",
        pin = "1234")
    whenever(userSession.ongoingRegistrationEntry()).thenReturn(ongoingEntry.toOptional())
    whenever(userSession.saveOngoingRegistrationEntryAsUser(currentTime)).thenReturn(Completable.complete())

    val facility1 = TestData.facility(name = "Hoshiarpur", uuid = UUID.fromString("bc761c6c-032f-4f1d-a66a-3ec81e9e8aa3"))
    uiEvents.onNext(RegistrationFacilityConfirmed(facility1.uuid))

    verify(screen).openIntroVideoScreen()
    verify(userSession).saveOngoingRegistrationEntry(ongoingEntry.copy(facilityId = facility1.uuid))
    verify(userSession).saveOngoingRegistrationEntryAsUser(currentTime)
  }

  @Test
  fun `search field should only be shown when facilities are available`() {
    whenever(facilityRepository.recordCount()).thenReturn(Observable.just(0, 10))
    whenever(facilitySync.pullWithResult()).thenReturn(Single.never())
    whenever(screenLocationUpdates.streamUserLocation(any(), any(), any())).thenReturn(Observable.just(Unavailable))

    uiEvents.onNext(ScreenCreated())

    val inOrder = inOrder(screen)
    inOrder.verify(screen).showToolbarWithoutSearchField()
    inOrder.verify(screen).showToolbarWithSearchField()
  }
}
