package org.simple.clinic.registration.facility

import com.nhaarman.mockitokotlin2.clearInvocations
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.facility.change.FacilitiesUpdateType.FIRST_UPDATE
import org.simple.clinic.facility.change.FacilitiesUpdateType.SUBSEQUENT_UPDATE
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
  fun `when screen is started, location should be fetched`() {
    val locationUpdateInterval = Duration.ofDays(5)
    whenever(facilityRepository.recordCount()).thenReturn(Observable.never())

    setupController(
        registrationConfig.copy(locationUpdateInterval = locationUpdateInterval),
        locationUpdate = Observable.never()
    )

    verify(screenLocationUpdates).streamUserLocation(
        updateInterval = locationUpdateInterval,
        timeout = registrationConfig.locationListenerExpiry,
        discardOlderThan = registrationConfig.staleLocationThreshold
    )
    verify(screen).showProgressIndicator()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `while location is being fetched then progress indicator should be shown`() {
    val facilities = emptyList<Facility>()
    whenever(facilityRepository.facilities()).thenReturn(Observable.just(facilities))
    whenever(facilityRepository.recordCount()).thenReturn(Observable.just(facilities.size))

    setupController()

    val inOrder = inOrder(screen)
    inOrder.verify(screen).showProgressIndicator()
    inOrder.verify(screen).hideProgressIndicator()
    inOrder.verifyNoMoreInteractions()
  }

  @Test
  fun `when facilities are fetched, but location is unavailable then facilities should still be shown`() {
    val phcObvious = TestData.facility(name = "PHC Obvious", district = "Bangalore Central", state = "Karnataka")
    val chcNilenso = TestData.facility(name = "CHC Nilenso", streetAddress = "10th Cross Road", district = "Indiranagar", state = "Karnataka")
    val facilities = listOf(phcObvious, chcNilenso)
    val searchQuery = "f"

    whenever(facilityRepository.facilities(searchQuery)).thenReturn(Observable.just(facilities))
    whenever(facilityRepository.recordCount()).thenReturn(Observable.just(facilities.size))

    setupController()
    uiEvents.onNext(RegistrationFacilitySearchQueryChanged(searchQuery))

    val expectedFacilityListItems = listOf(
        FacilityOption(phcObvious, Name.Plain("PHC Obvious"), address = Address.WithoutStreet(district = "Bangalore Central", state = "Karnataka")),
        FacilityOption(chcNilenso, Name.Plain("CHC Nilenso"), address = Address.WithStreet(street = "10th Cross Road", district = "Indiranagar", state = "Karnataka"))
    )
    verify(screen).showProgressIndicator()
    verify(screen).hideProgressIndicator()
    verify(screen).showToolbarWithSearchField()
    verify(screen).updateFacilities(expectedFacilityListItems, FIRST_UPDATE)
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when search query is changed then the query should be used for fetching filtered facilities`() {
    val phcObvious = TestData.facility(name = "PHC Obvious", streetAddress = "Richmond Road", district = "Bangalore Central", state = "Karnataka")
    val chcNilenso = TestData.facility(name = "CHC Nilenso", district = "Indiranagar", state = "Karnataka")

    whenever(facilityRepository.facilities("HC")).thenReturn(Observable.just(listOf(phcObvious, chcNilenso)))
    whenever(facilityRepository.facilities("PHC")).thenReturn(Observable.just(listOf(phcObvious)))
    whenever(facilityRepository.facilities("CHC")).thenReturn(Observable.just(listOf(chcNilenso)))
    whenever(facilityRepository.recordCount()).thenReturn(Observable.just(2))

    setupController()

    uiEvents.onNext(RegistrationFacilitySearchQueryChanged(query = "HC"))
    verify(screen).showProgressIndicator()
    verify(screen).hideProgressIndicator()
    verify(screen).showToolbarWithSearchField()
    verify(screen).updateFacilities(listOf(
        FacilityOption(phcObvious, Name.Highlighted("PHC Obvious", 1, 3), address = Address.WithStreet(street = "Richmond Road", district = "Bangalore Central", state = "Karnataka")),
        FacilityOption(chcNilenso, Name.Highlighted("CHC Nilenso", 1, 3), address = Address.WithoutStreet(district = "Indiranagar", state = "Karnataka"))
    ), FIRST_UPDATE)
    verifyNoMoreInteractions(screen)

    clearInvocations(screen)
    uiEvents.onNext(RegistrationFacilitySearchQueryChanged(query = "PHC"))
    verify(screen).updateFacilities(listOf(
        FacilityOption(phcObvious, Name.Highlighted("PHC Obvious", 0, 3), address = Address.WithStreet(street = "Richmond Road", district = "Bangalore Central", state = "Karnataka"))
    ), SUBSEQUENT_UPDATE)
    verifyNoMoreInteractions(screen)

    clearInvocations(screen)
    uiEvents.onNext(RegistrationFacilitySearchQueryChanged(query = "CHC"))
    verify(screen).updateFacilities(listOf(
        FacilityOption(chcNilenso, Name.Highlighted("CHC Nilenso", 0, 3), address = Address.WithoutStreet(district = "Indiranagar", state = "Karnataka"))
    ), SUBSEQUENT_UPDATE)
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when facilities are received then their UI models for facility list should be created`() {
    val phcObvious = TestData.facility(name = "PHC Obvious", streetAddress = "Richmond Road", district = "Bangalore Central", state = "Karnataka")
    val chcNilenso = TestData.facility(name = "CHC Nilenso", district = "Indiranagar", state = "Karnataka")
    val facilities = listOf(phcObvious, chcNilenso)
    val searchQuery = ""

    whenever(facilityRepository.facilities(searchQuery)).thenReturn(Observable.just(facilities, facilities))
    whenever(facilityRepository.recordCount()).thenReturn(Observable.just(facilities.size, facilities.size))

    val expectedFacilityListItems = listOf(
        FacilityOption(phcObvious, Name.Plain("PHC Obvious"), address = Address.WithStreet(street = "Richmond Road", district = "Bangalore Central", state = "Karnataka")),
        FacilityOption(chcNilenso, Name.Plain("CHC Nilenso"), address = Address.WithoutStreet(district = "Indiranagar", state = "Karnataka"))
    )

    val location = LocationUpdate.Available(Coordinates(0.0, 0.0), timeSinceBootWhenRecorded = Duration.ofNanos(0))
    setupController(locationUpdate = Observable.just(location))

    uiEvents.onNext(RegistrationFacilitySearchQueryChanged(""))
    verify(screen).showProgressIndicator()
    verify(screen).hideProgressIndicator()
    verify(screen).showToolbarWithSearchField()
    verify(screen).updateFacilities(expectedFacilityListItems, FIRST_UPDATE)
    verify(screen).updateFacilities(expectedFacilityListItems, SUBSEQUENT_UPDATE)
    verifyNoMoreInteractions(screen)

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
    whenever(facilityRepository.recordCount()).thenReturn(Observable.just(1))

    val facility1 = TestData.facility(name = "Hoshiarpur", uuid = UUID.fromString("5cf9d744-7f34-4633-aa46-a6c7e7542060"))

    setupController()
    uiEvents.onNext(RegistrationFacilityClicked(facility1))

    verify(screen).showProgressIndicator()
    verify(screen).hideProgressIndicator()
    verify(screen).showToolbarWithSearchField()
    verify(screen).showConfirmFacilitySheet(facility1.uuid, facility1.name)
    verifyNoMoreInteractions(screen)
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
    whenever(facilityRepository.recordCount()).thenReturn(Observable.just(1))

    val facility1 = TestData.facility(name = "Hoshiarpur", uuid = UUID.fromString("bc761c6c-032f-4f1d-a66a-3ec81e9e8aa3"))

    setupController()
    uiEvents.onNext(RegistrationFacilityConfirmed(facility1.uuid))

    verify(screen).showProgressIndicator()
    verify(screen).hideProgressIndicator()
    verify(screen).showToolbarWithSearchField()
    verify(screen).openIntroVideoScreen()
    verifyNoMoreInteractions(screen)
    verify(userSession).saveOngoingRegistrationEntry(ongoingEntry.copy(facilityId = facility1.uuid))
    verify(userSession).saveOngoingRegistrationEntryAsUser(currentTime)
  }

  @Test
  fun `search field should only be shown when facilities are available`() {
    whenever(facilityRepository.recordCount()).thenReturn(Observable.just(0, 10))

    setupController()

    val inOrder = inOrder(screen)
    inOrder.verify(screen).showProgressIndicator()
    inOrder.verify(screen).showToolbarWithoutSearchField()
    inOrder.verify(screen).showToolbarWithSearchField()
    inOrder.verify(screen).hideProgressIndicator()
    inOrder.verifyNoMoreInteractions()
  }

  private fun setupController(
      config: RegistrationConfig = registrationConfig,
      locationUpdate: Observable<LocationUpdate> = Observable.just(Unavailable)
  ) {
    whenever(screenLocationUpdates.streamUserLocation(
        updateInterval = config.locationUpdateInterval,
        timeout = config.locationListenerExpiry,
        discardOlderThan = config.staleLocationThreshold
    )).thenReturn(locationUpdate)

    val controller = RegistrationFacilitySelectionScreenController(
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

    uiEvents.onNext(ScreenCreated())
  }
}
