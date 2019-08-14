package org.simple.clinic.registration.facility

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
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
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.FacilityPullResult
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.facility.FacilitySync
import org.simple.clinic.facility.change.FacilitiesUpdateType.FIRST_UPDATE
import org.simple.clinic.facility.change.FacilitiesUpdateType.SUBSEQUENT_UPDATE
import org.simple.clinic.facility.change.FacilityListItem
import org.simple.clinic.facility.change.FacilityListItemBuilder
import org.simple.clinic.location.Coordinates
import org.simple.clinic.location.LocationRepository
import org.simple.clinic.location.LocationUpdate
import org.simple.clinic.location.LocationUpdate.Available
import org.simple.clinic.location.LocationUpdate.Unavailable
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.registration.RegistrationConfig
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Distance
import org.simple.clinic.util.RuntimePermissionResult
import org.simple.clinic.util.RuntimePermissionResult.DENIED
import org.simple.clinic.util.RuntimePermissionResult.GRANTED
import org.simple.clinic.util.RuntimePermissionResult.NEVER_ASK_AGAIN
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestElapsedRealtimeClock
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import java.util.UUID
import java.util.concurrent.TimeUnit

@RunWith(JUnitParamsRunner::class)
class RegistrationFacilitySelectionScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val uiEvents = PublishSubject.create<UiEvent>()!!
  private val screen = mock<RegistrationFacilitySelectionScreen>()
  private val facilitySync = mock<FacilitySync>()
  private val facilityRepository = mock<FacilityRepository>()
  private val locationRepository = mock<LocationRepository>()
  private val userSession = mock<UserSession>()
  private val testComputationScheduler = TestScheduler()
  private val elapsedRealtimeClock = TestElapsedRealtimeClock()
  private val listItemBuilder = mock<FacilityListItemBuilder>()

  private lateinit var controller: RegistrationFacilitySelectionScreenController

  private val configTemplate = RegistrationConfig(
      locationListenerExpiry = Duration.ofSeconds(0),
      locationUpdateInterval = Duration.ofSeconds(0),
      proximityThresholdForNearbyFacilities = Distance.ofKilometers(0.0),
      staleLocationThreshold = Duration.ofSeconds(0))
  private val configProvider = BehaviorSubject.createDefault(configTemplate)

  @Before
  fun setUp() {
    // To control time used by Observable.timer().
    RxJavaPlugins.setComputationSchedulerHandler { testComputationScheduler }

    // Location updates are listened on a background thread.
    RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }

    controller = RegistrationFacilitySelectionScreenController(
        facilitySync = facilitySync,
        facilityRepository = facilityRepository,
        userSession = userSession,
        locationRepository = locationRepository,
        configProvider = configProvider.firstOrError(),
        elapsedRealtimeClock = elapsedRealtimeClock,
        listItemBuilder = listItemBuilder)

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
    whenever(locationRepository.streamUserLocation(any(), any())).thenReturn(Observable.just(Unavailable))

    uiEvents.onNext(ScreenCreated())

    verify(facilitySync).pullWithResult()
  }

  @Test
  fun `when screen is started and location permission is available then location should be fetched`() {
    configProvider.onNext(configTemplate.copy(locationUpdateInterval = Duration.ofDays(5)))
    whenever(facilityRepository.recordCount()).thenReturn(Observable.never())
    whenever(locationRepository.streamUserLocation(any(), any())).thenReturn(Observable.never())

    uiEvents.onNext(ScreenCreated())
    uiEvents.onNext(RegistrationFacilityLocationPermissionChanged(GRANTED))

    verify(locationRepository).streamUserLocation(updateInterval = eq(Duration.ofDays(5)), updateScheduler = any())
  }

  @Test
  @Parameters(method = "params for permission denials")
  fun `when screen is started and location permission was denied then location should not be fetched and facilities should be shown`(
      permissionResult: RuntimePermissionResult
  ) {
    val facilities = listOf(
        PatientMocker.facility(name = "Facility 1"),
        PatientMocker.facility(name = "Facility 2"))
    whenever(facilityRepository.facilities(any())).thenReturn(Observable.just(facilities))
    whenever(facilityRepository.recordCount()).thenReturn(Observable.just(facilities.size))
    whenever(locationRepository.streamUserLocation(any(), any())).thenReturn(Observable.never())

    uiEvents.onNext(ScreenCreated())
    uiEvents.onNext(RegistrationFacilitySearchQueryChanged(""))
    uiEvents.onNext(RegistrationFacilityLocationPermissionChanged(permissionResult))

    verify(locationRepository, never()).streamUserLocation(any(), any())
    verify(screen).updateFacilities(any(), any())
  }

  @Test
  fun `when screen is started then location should only be read once`() {
    configProvider.onNext(configTemplate.copy(locationListenerExpiry = Duration.ofSeconds(5)))

    val facilities = listOf(
        PatientMocker.facility(name = "Facility 1"),
        PatientMocker.facility(name = "Facility 2"))
    whenever(facilityRepository.facilities(any())).thenReturn(Observable.just(facilities))
    whenever(facilityRepository.recordCount()).thenReturn(Observable.just(facilities.size))

    val timeSinceBootWhenRecorded = Duration.ofMillis(elapsedRealtimeClock.millis())
    whenever(locationRepository.streamUserLocation(any(), any())).thenReturn(
        Observable.just(
            Available(Coordinates(0.0, 0.0), timeSinceBootWhenRecorded),
            Unavailable,
            Available(Coordinates(0.0, 0.0), timeSinceBootWhenRecorded)))

    uiEvents.onNext(ScreenCreated())
    uiEvents.onNext(RegistrationFacilitySearchQueryChanged(""))
    uiEvents.onNext(RegistrationFacilityLocationPermissionChanged(GRANTED))

    testComputationScheduler.advanceTimeBy(6, TimeUnit.SECONDS)

    verify(locationRepository).streamUserLocation(any(), any())
    verify(screen, times(1)).updateFacilities(any(), any())
  }

  @Test
  fun `when the user's location updates are received then only one recent update should be read`() {
    val config = configTemplate.copy(staleLocationThreshold = Duration.ofMinutes(10))
    configProvider.onNext(config)

    val facilities = listOf(PatientMocker.facility(name = "Facility 1"))
    whenever(facilityRepository.facilities(any())).thenReturn(Observable.just(facilities))
    whenever(facilityRepository.recordCount()).thenReturn(Observable.just(facilities.size))

    val locationUpdates = PublishSubject.create<LocationUpdate>()
    whenever(locationRepository.streamUserLocation(any(), any())).thenReturn(locationUpdates)

    uiEvents.onNext(ScreenCreated())
    uiEvents.onNext(RegistrationFacilitySearchQueryChanged(""))
    uiEvents.onNext(RegistrationFacilityLocationPermissionChanged(GRANTED))

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

  @Suppress("unused")
  fun `params for permission denials`(): List<RuntimePermissionResult> {
    return listOf(DENIED, NEVER_ASK_AGAIN)
  }

  @Test
  @Parameters("5", "6", "7", "8")
  fun `when location is being fetched then it should expire after a fixed time duration`(
      secondsSpentWaitingForLocation: Long
  ) {
    configProvider.onNext(configTemplate.copy(locationListenerExpiry = Duration.ofSeconds(5)))

    val facilities = emptyList<Facility>()
    whenever(facilityRepository.facilities()).thenReturn(Observable.just(facilities))
    whenever(facilityRepository.recordCount()).thenReturn(Observable.just(facilities.size))
    whenever(facilitySync.pullWithResult()).thenReturn(Single.just(FacilityPullResult.Success))
    whenever(locationRepository.streamUserLocation(any(), any())).thenReturn(Observable.never())

    uiEvents.onNext(ScreenCreated())
    uiEvents.onNext(RegistrationFacilityLocationPermissionChanged(GRANTED))
    verify(screen).showProgressIndicator()

    testComputationScheduler.advanceTimeBy(secondsSpentWaitingForLocation, TimeUnit.SECONDS)
    verify(screen).hideProgressIndicator()
  }

  @Test
  fun `while facilities and location are being fetched then progress indicator should be shown`() {
    val facilities = emptyList<Facility>()
    whenever(facilityRepository.facilities()).thenReturn(Observable.just(facilities))
    whenever(facilityRepository.recordCount()).thenReturn(Observable.just(facilities.size))
    whenever(facilitySync.pullWithResult()).thenReturn(Single.just(FacilityPullResult.Success))
    whenever(locationRepository.streamUserLocation(any(), any())).thenReturn(Observable.just(Unavailable))

    uiEvents.onNext(ScreenCreated())
    uiEvents.onNext(RegistrationFacilityLocationPermissionChanged(GRANTED))

    val inOrder = inOrder(screen)
    inOrder.verify(screen).showProgressIndicator()
    inOrder.verify(screen).hideProgressIndicator()
  }

  @Test
  fun `when both facilities and location are fetched only then should facilities be shown`() {
    configProvider.onNext(configTemplate.copy(locationListenerExpiry = Duration.ofSeconds(5)))

    val facilities = listOf(
        PatientMocker.facility(name = "Facility 1"),
        PatientMocker.facility(name = "Facility 2"))
    whenever(facilityRepository.facilities(any())).thenReturn(Observable.just(facilities))
    whenever(facilityRepository.recordCount()).thenReturn(Observable.just(facilities.size))
    whenever(facilitySync.pullWithResult()).thenReturn(Single.just(FacilityPullResult.Success))

    val locationUpdates = PublishSubject.create<LocationUpdate>()
    whenever(locationRepository.streamUserLocation(any(), any())).thenReturn(locationUpdates)

    uiEvents.run {
      onNext(ScreenCreated())
      onNext(RegistrationFacilitySearchQueryChanged(""))
      onNext(RegistrationFacilityLocationPermissionChanged(GRANTED))
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
    whenever(facilityRepository.facilities(any())).thenReturn(Observable.just(facilities))
    whenever(facilityRepository.recordCount()).thenReturn(Observable.just(facilities.size))
    whenever(locationRepository.streamUserLocation(any(), any())).thenReturn(Observable.never())

    uiEvents.onNext(ScreenCreated())
    uiEvents.onNext(RegistrationFacilitySearchQueryChanged("f"))
    uiEvents.onNext(RegistrationFacilityLocationPermissionChanged(GRANTED))
    verify(screen, never()).updateFacilities(any(), any())

    testComputationScheduler.advanceTimeBy(6, TimeUnit.SECONDS)
    verify(screen).updateFacilities(any(), any())
  }

  @Test
  fun `when screen is started then facilities should not be fetched if they are already available`() {
    val facilities = listOf(PatientMocker.facility())
    whenever(facilityRepository.facilities()).thenReturn(Observable.just(facilities))
    whenever(facilityRepository.recordCount()).thenReturn(Observable.just(facilities.size))
    whenever(facilitySync.pullWithResult()).thenReturn(Single.just(FacilityPullResult.Success))
    whenever(locationRepository.streamUserLocation(any(), any())).thenReturn(Observable.just(Unavailable))

    uiEvents.onNext(ScreenCreated())

    verify(facilitySync, never()).pullWithResult()
  }

  @Test
  fun `when search query is changed then the query should be used for fetching filtered facilities`() {
    val facilities = listOf(
        PatientMocker.facility(name = "Facility 1"),
        PatientMocker.facility(name = "Facility 2"))
    whenever(facilityRepository.facilities(any())).thenReturn(Observable.just(facilities))
    whenever(facilityRepository.recordCount()).thenReturn(Observable.just(facilities.size))
    whenever(facilitySync.pullWithResult()).thenReturn(Single.just(FacilityPullResult.Success))

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
    whenever(locationRepository.streamUserLocation(any(), any())).thenReturn(Observable.just(Unavailable))
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
    whenever(locationRepository.streamUserLocation(any(), any())).thenReturn(Observable.just(Unavailable))

    uiEvents.onNext(RegistrationFacilityUserLocationUpdated(Unavailable))
    uiEvents.onNext(RegistrationFacilitySelectionRetryClicked())

    verify(screen).hideError()
    verify(screen).showProgressIndicator()
    verify(facilitySync).pullWithResult()
    verify(screen).hideProgressIndicator()
  }

  @Test
  fun `when facilities are received then their UI models for facility list should be created`() {
    val facility1 = PatientMocker.facility(name = "Facility 1")
    val facility2 = PatientMocker.facility(name = "Facility 2")
    val facilities = listOf(facility1, facility2)
    whenever(facilityRepository.facilities()).thenReturn(Observable.just(facilities, facilities))
    whenever(facilityRepository.recordCount()).thenReturn(Observable.just(facilities.size, facilities.size))

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
  fun `when a facility is clicked then the ongoing entry should be updated with selected facility and the user should be logged in`() {
    val ongoingEntry = OngoingRegistrationEntry(
        uuid = UUID.randomUUID(),
        phoneNumber = "1234567890",
        fullName = "Ashok",
        pin = "1234",
        pinConfirmation = "5678",
        createdAt = Instant.now())
    whenever(userSession.ongoingRegistrationEntry()).thenReturn(Single.just(ongoingEntry))
    whenever(userSession.saveOngoingRegistrationEntry(any())).thenReturn(Completable.complete())
    whenever(userSession.saveOngoingRegistrationEntryAsUser()).thenReturn(Completable.complete())

    val facility1 = PatientMocker.facility(name = "Hoshiarpur", uuid = UUID.randomUUID())
    uiEvents.onNext(RegistrationFacilityClicked(facility1))

    verify(screen).openRegistrationScreen()
    verify(userSession).saveOngoingRegistrationEntry(ongoingEntry.copy(facilityId = facility1.uuid))
    verify(userSession).saveOngoingRegistrationEntryAsUser()
  }

  @Test
  fun `search field should only be shown when facilities are available`() {
    whenever(facilityRepository.recordCount()).thenReturn(Observable.just(0, 10))
    whenever(facilitySync.pullWithResult()).thenReturn(Single.never())

    uiEvents.onNext(ScreenCreated())

    val inOrder = inOrder(screen)
    inOrder.verify(screen).showToolbarWithoutSearchField()
    inOrder.verify(screen).showToolbarWithSearchField()
  }
}
