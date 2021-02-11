package org.simple.clinic.facilitypicker

import com.nhaarman.mockitokotlin2.clearInvocations
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.facility.change.FacilityListItemBuilder
import org.simple.clinic.facilitypicker.PickFrom.AllFacilities
import org.simple.clinic.location.Coordinates
import org.simple.clinic.location.DistanceCalculator
import org.simple.clinic.location.LocationUpdate
import org.simple.clinic.location.LocationUpdate.Unavailable
import org.simple.clinic.location.ScreenLocationUpdates
import org.simple.clinic.util.Distance
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.mobius.migration.MobiusTestFixture
import java.time.Duration
import java.util.UUID

class FacilityPickerLogicTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val uiEvents = PublishSubject.create<FacilityPickerEvent>()
  private val ui = mock<FacilityPickerUi>()
  private val uiActions = mock<FacilityPickerUiActions>()
  private val facilityRepository = mock<FacilityRepository>()
  private val listItemBuilder = FacilityListItemBuilder(DistanceCalculator())
  private val screenLocationUpdates = mock<ScreenLocationUpdates>()

  private lateinit var testFixture: MobiusTestFixture<FacilityPickerModel, FacilityPickerEvent, FacilityPickerEffect>

  private val config = FacilityPickerConfig(
      locationListenerExpiry = Duration.ofSeconds(0),
      locationUpdateInterval = Duration.ofSeconds(0),
      proximityThresholdForNearbyFacilities = Distance.ofKilometers(0.0),
      staleLocationThreshold = Duration.ofSeconds(0)
  )

  @After
  fun tearDown() {
    testFixture.dispose()
  }

  @Test
  fun `when screen is started, location should be fetched`() {
    // given
    val locationUpdateInterval = Duration.ofDays(5)
    whenever(facilityRepository.facilities("")).thenReturn(Observable.just(emptyList()))
    whenever(facilityRepository.recordCount()).thenReturn(Observable.just(0))

    // when
    setupController(
        config.copy(locationUpdateInterval = locationUpdateInterval),
        locationUpdate = Observable.never()
    )

    // then
    verify(screenLocationUpdates).streamUserLocation(
        updateInterval = locationUpdateInterval,
        timeout = config.locationListenerExpiry,
        discardOlderThan = config.staleLocationThreshold
    )
    verify(ui, times(3)).showProgressIndicator()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `while location is being fetched then progress indicator should be shown`() {
    // given
    val facilities = emptyList<Facility>()
    whenever(facilityRepository.facilities()).thenReturn(Observable.just(facilities))
    whenever(facilityRepository.recordCount()).thenReturn(Observable.just(facilities.size))

    // when
    setupController()

    // then
    val inOrder = inOrder(ui)
    inOrder.verify(ui, times(3)).showProgressIndicator()
    inOrder.verify(ui).hideProgressIndicator()
    inOrder.verify(ui).updateFacilities(emptyList())
    inOrder.verifyNoMoreInteractions()
  }

  @Test
  fun `when facilities are fetched, but location is unavailable then facilities should still be shown`() {
    // given
    val phcObvious = TestData.facility(name = "PHC Obvious", district = "Bangalore Central", state = "Karnataka")
    val chcNilenso = TestData.facility(name = "CHC Nilenso", streetAddress = "10th Cross Road", district = "Indiranagar", state = "Karnataka")
    val facilities = listOf(phcObvious, chcNilenso)
    val searchQuery = "f"

    whenever(facilityRepository.facilities("")).thenReturn(Observable.just(facilities))
    whenever(facilityRepository.facilities(searchQuery)).thenReturn(Observable.just(facilities))
    whenever(facilityRepository.recordCount()).thenReturn(Observable.just(facilities.size))

    // when
    setupController()
    uiEvents.onNext(SearchQueryChanged(searchQuery))

    // then
    val expectedFacilityListItems = listItemBuilder.build(facilities, searchQuery, null, config.proximityThresholdForNearbyFacilities)
    verify(ui, times(3)).showProgressIndicator()
    verify(ui, times(2)).hideProgressIndicator()
    verify(ui, times(2)).updateFacilities(expectedFacilityListItems)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when search query is changed then the query should be used for fetching filtered facilities`() {
    // given
    val phcObvious = TestData.facility(name = "PHC Obvious", streetAddress = "Richmond Road", district = "Bangalore Central", state = "Karnataka")
    val chcNilenso = TestData.facility(name = "CHC Nilenso", district = "Indiranagar", state = "Karnataka")

    whenever(facilityRepository.facilities("")).thenReturn(Observable.just(listOf(phcObvious, chcNilenso)))
    whenever(facilityRepository.facilities("HC")).thenReturn(Observable.just(listOf(phcObvious, chcNilenso)))
    whenever(facilityRepository.facilities("PHC")).thenReturn(Observable.just(listOf(phcObvious)))
    whenever(facilityRepository.facilities("CHC")).thenReturn(Observable.just(listOf(chcNilenso)))
    whenever(facilityRepository.recordCount()).thenReturn(Observable.just(2))

    // when
    setupController()
    uiEvents.onNext(SearchQueryChanged(query = "HC"))

    // then
    verify(ui, times(3)).showProgressIndicator()
    verify(ui, times(2)).hideProgressIndicator()
    verify(ui).updateFacilities(listItemBuilder.build(listOf(phcObvious, chcNilenso), "", null, config.proximityThresholdForNearbyFacilities))
    verify(ui).updateFacilities(listItemBuilder.build(listOf(phcObvious, chcNilenso), "HC", null, config.proximityThresholdForNearbyFacilities))
    verifyNoMoreInteractions(ui)

    // when
    clearInvocations(ui)
    uiEvents.onNext(SearchQueryChanged(query = "PHC"))

    // then
    verify(ui).hideProgressIndicator()
    verify(ui).updateFacilities(listItemBuilder.build(listOf(phcObvious), "PHC", null, config.proximityThresholdForNearbyFacilities))
    verifyNoMoreInteractions(ui)

    // when
    clearInvocations(ui)
    uiEvents.onNext(SearchQueryChanged(query = "CHC"))

    // then
    verify(ui).hideProgressIndicator()
    verify(ui).updateFacilities(listItemBuilder.build(listOf(chcNilenso), "CHC", null, config.proximityThresholdForNearbyFacilities))
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when facilities are received then their UI models for facility list should be created`() {
    // given
    val phcObvious = TestData.facility(name = "PHC Obvious", streetAddress = "Richmond Road", district = "Bangalore Central", state = "Karnataka")
    val chcNilenso = TestData.facility(name = "CHC Nilenso", district = "Indiranagar", state = "Karnataka")
    val facilities = listOf(phcObvious, chcNilenso)
    val searchQuery = ""
    val location = LocationUpdate.Available(Coordinates(0.0, 0.0), timeSinceBootWhenRecorded = Duration.ofNanos(0))

    whenever(facilityRepository.facilities(searchQuery)).thenReturn(Observable.just(facilities, facilities))
    whenever(facilityRepository.recordCount()).thenReturn(Observable.just(facilities.size, facilities.size))

    // when
    setupController(locationUpdate = Observable.just(location))
    uiEvents.onNext(SearchQueryChanged(searchQuery))

    // then
    val expectedFacilityListItems = listItemBuilder.build(facilities, searchQuery, null, config.proximityThresholdForNearbyFacilities)
    verify(ui, times(3)).showProgressIndicator()
    verify(ui, times(4)).hideProgressIndicator()
    verify(ui, times(4)).updateFacilities(expectedFacilityListItems)
    verifyNoMoreInteractions(ui)

  }

  @Test
  fun `when a facility is clicked then dispatch the facility selected callback`() {
    // given
    val facility1 = TestData.facility(name = "Hoshiarpur", uuid = UUID.fromString("5cf9d744-7f34-4633-aa46-a6c7e7542060"))

    whenever(facilityRepository.facilities("")).thenReturn(Observable.just(listOf(facility1)))
    whenever(facilityRepository.recordCount()).thenReturn(Observable.just(1))

    // when
    setupController()
    uiEvents.onNext(FacilityClicked(facility1))

    // then
    verify(ui, times(3)).showProgressIndicator()
    verify(ui).hideProgressIndicator()
    verify(ui).updateFacilities(listItemBuilder.build(listOf(facility1), "", null, config.proximityThresholdForNearbyFacilities))
    verify(uiActions).dispatchSelectedFacility(facility1)
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `search field should only be shown when facilities are available`() {
    // given
    whenever(facilityRepository.facilities("")).thenReturn(Observable.just(emptyList()))
    whenever(facilityRepository.recordCount()).thenReturn(Observable.just(10))

    // when
    setupController()

    // then
    verify(ui, times(3)).showProgressIndicator()
    verify(ui).hideProgressIndicator()
    verify(ui).updateFacilities(emptyList())
    verifyNoMoreInteractions(ui)
  }

  private fun setupController(
      config: FacilityPickerConfig = this.config,
      locationUpdate: Observable<LocationUpdate> = Observable.just(Unavailable),
      pickFrom: PickFrom = AllFacilities
  ) {
    whenever(screenLocationUpdates.streamUserLocation(
        updateInterval = config.locationUpdateInterval,
        timeout = config.locationListenerExpiry,
        discardOlderThan = config.staleLocationThreshold
    )).thenReturn(locationUpdate)

    val effectHandler = FacilityPickerEffectHandler(
        schedulers = TestSchedulersProvider.trampoline(),
        screenLocationUpdates = screenLocationUpdates,
        facilityRepository = facilityRepository,
        uiActions = uiActions
    )
    val uiRenderer = FacilityPickerUiRenderer(listItemBuilder, config, ui)

    testFixture = MobiusTestFixture(
        events = uiEvents.ofType(),
        defaultModel = FacilityPickerModel.create(),
        init = FacilityPickerInit(pickFrom, config),
        update = FacilityPickerUpdate(pickFrom),
        effectHandler = effectHandler.build(),
        modelUpdateListener = uiRenderer::render
    )
    testFixture.start()
  }
}
