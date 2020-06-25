package org.simple.clinic.registration.facility

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.FacilityPullResult
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.facility.FacilitySync
import org.simple.clinic.facility.change.FacilitiesUpdateType.FIRST_UPDATE
import org.simple.clinic.facility.change.FacilityListItem.FacilityOption
import org.simple.clinic.facility.change.FacilityListItem.FacilityOption.Address
import org.simple.clinic.facility.change.FacilityListItem.FacilityOption.Name
import org.simple.clinic.facility.change.FacilityListItemBuilder
import org.simple.clinic.location.Coordinates
import org.simple.clinic.location.DistanceCalculator
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

  private val uiEvents = PublishSubject.create<UiEvent>()
  private val screen = mock<RegistrationFacilitySelectionScreen>()
  private val facilitySync = mock<FacilitySync>()
  private val facilityRepository = mock<FacilityRepository>()
  private val userSession = mock<UserSession>()
  private val currentTime = Instant.parse("2018-01-01T00:00:00Z")
  private val utcClock = TestUtcClock(currentTime)
  private val listItemBuilder = FacilityListItemBuilder(DistanceCalculator())
  private val screenLocationUpdates = mock<ScreenLocationUpdates>()

  private lateinit var controllerSubscription: Disposable

  private val registrationConfig = RegistrationConfig(
      locationListenerExpiry = Duration.ofSeconds(0),
      locationUpdateInterval = Duration.ofSeconds(0),
      proximityThresholdForNearbyFacilities = Distance.ofKilometers(0.0),
      staleLocationThreshold = Duration.ofSeconds(0)
  )

  @After
  fun tearDown() {
    controllerSubscription.dispose()
  }

  @Test
  fun `when screen is started then facilities should be fetched if they are empty`() {
    val facilities = emptyList<Facility>()
    whenever(facilityRepository.facilities()).thenReturn(Observable.just(facilities))
    whenever(facilityRepository.recordCount()).thenReturn(Observable.just(facilities.size))
    whenever(facilitySync.pullWithResult()).thenReturn(Single.just(FacilityPullResult.Success))
    whenever(screenLocationUpdates.streamUserLocation(any(), any(), any())).thenReturn(Observable.just(Unavailable))

    setupController()
    uiEvents.onNext(ScreenCreated())

    verify(facilitySync).pullWithResult()
  }

  @Test
  fun `when screen is started, location should be fetched`() {
    whenever(facilityRepository.recordCount()).thenReturn(Observable.never())
    whenever(screenLocationUpdates.streamUserLocation(any(), any(), any())).thenReturn(Observable.never())

    setupController(registrationConfig.copy(locationUpdateInterval = Duration.ofDays(5)))
    uiEvents.onNext(ScreenCreated())

    verify(screenLocationUpdates).streamUserLocation(
        updateInterval = Duration.ofDays(5),
        timeout = registrationConfig.locationListenerExpiry,
        discardOlderThan = registrationConfig.staleLocationThreshold
    )
  }

  @Test
  fun `while facilities and location are being fetched then progress indicator should be shown`() {
    val facilities = emptyList<Facility>()
    whenever(facilityRepository.facilities()).thenReturn(Observable.just(facilities))
    whenever(facilityRepository.recordCount()).thenReturn(Observable.just(facilities.size))
    whenever(facilitySync.pullWithResult()).thenReturn(Single.just(FacilityPullResult.Success))
    whenever(screenLocationUpdates.streamUserLocation(any(), any(), any())).thenReturn(Observable.just(Unavailable))

    setupController()
    uiEvents.onNext(ScreenCreated())

    val inOrder = inOrder(screen)
    inOrder.verify(screen).showProgressIndicator()
    inOrder.verify(screen).hideProgressIndicator()
  }

  @Test
  fun `when facilities are fetched, but location is unavailable then facilities should still be shown`() {
    val facilities = listOf(
        TestData.facility(name = "Facility 1"),
        TestData.facility(name = "Facility 2"))
    whenever(facilityRepository.facilities(any())).thenReturn(Observable.just(facilities))
    whenever(facilityRepository.recordCount()).thenReturn(Observable.just(facilities.size))
    whenever(screenLocationUpdates.streamUserLocation(any(), any(), any())).thenReturn(Observable.just(Unavailable))

    setupController()
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

    setupController()
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

    setupController()
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

    setupController()
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

    setupController()
    uiEvents.onNext(RegistrationFacilityUserLocationUpdated(Unavailable))
    uiEvents.onNext(RegistrationFacilitySelectionRetryClicked())

    verify(screen).hideError()
    verify(screen).showProgressIndicator()
    verify(facilitySync).pullWithResult()
    verify(screen).hideProgressIndicator()
  }

  @Test
  fun `when facilities are received then their UI models for facility list should be created`() {
    val phcObvious = TestData.facility(name = "PHC Obvious", streetAddress = "Richmond Road", district = "Bangalore Central", state = "Karnataka")
    val chcNilenso = TestData.facility(name = "CHC Nilenso", district = "Indiranagar", state = "Karnataka")
    val facilities = listOf(phcObvious, chcNilenso)
    val searchQuery = ""

    whenever(facilityRepository.facilities(searchQuery)).thenReturn(Observable.just(facilities, facilities))
    whenever(facilityRepository.recordCount()).thenReturn(Observable.just(facilities.size, facilities.size))
    whenever(screenLocationUpdates.streamUserLocation(any(), any(), any())).thenReturn(Observable.just(Unavailable))

    val expectedFacilityListItems = listOf(
        FacilityOption(phcObvious, Name.Plain("PHC Obvious"), address = Address.WithStreet(street = "Richmond Road", district = "Bangalore Central", state = "Karnataka")),
        FacilityOption(chcNilenso, Name.Plain("CHC Nilenso"), address = Address.WithoutStreet(district = "Indiranagar", state = "Karnataka"))
    )

    setupController()
    uiEvents.onNext(ScreenCreated())

    val location = LocationUpdate.Available(Coordinates(0.0, 0.0), timeSinceBootWhenRecorded = Duration.ofNanos(0))
    uiEvents.onNext(RegistrationFacilityUserLocationUpdated(location))
    uiEvents.onNext(RegistrationFacilitySearchQueryChanged(""))
    verify(screen).updateFacilities(expectedFacilityListItems, FIRST_UPDATE)
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

    setupController()
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

    setupController()
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

    setupController()
    uiEvents.onNext(ScreenCreated())

    val inOrder = inOrder(screen)
    inOrder.verify(screen).showToolbarWithoutSearchField()
    inOrder.verify(screen).showToolbarWithSearchField()
  }

  private fun setupController(
      config: RegistrationConfig = registrationConfig
  ) {
    val controller = RegistrationFacilitySelectionScreenController(
        facilitySync = facilitySync,
        facilityRepository = facilityRepository,
        userSession = userSession,
        config = config,
        listItemBuilder = listItemBuilder,
        screenLocationUpdates = screenLocationUpdates,
        utcClock = utcClock
    )

    controllerSubscription = uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }
}
