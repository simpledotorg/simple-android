package org.simple.clinic.home

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.simple.sharedTestCode.TestData
import org.simple.clinic.facility.Facility
import org.simple.sharedTestCode.util.RxErrorsRule
import org.simple.sharedTestCode.util.TestUserClock
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.widgets.UiEvent
import org.simple.mobius.migration.MobiusTestFixture
import java.time.LocalDate
import java.util.UUID

class HomeScreenLogicTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val uiEvents: PublishSubject<UiEvent> = PublishSubject.create()

  private val ui = mock<HomeScreenUi>()
  private val uiActions = mock<HomeScreenUiActions>()
  private val clock = TestUserClock()

  private lateinit var testFixture: MobiusTestFixture<HomeScreenModel, HomeScreenEvent, HomeScreenEffect>

  @After
  fun tearDown() {
    testFixture.dispose()
  }

  @Test
  fun `when home screen is created, then setup the home screen`() {
    // given
    val facility1 = TestData.facility(
        uuid = UUID.fromString("de250445-0ec9-43e4-be33-2a49ca334535"),
        name = "CHC Buchho"
    )
    val facility2 = TestData.facility(
        uuid = UUID.fromString("5b2136b8-11d5-4e20-8703-087281679aee"),
        name = "CHC Nathana"
    )

    // when
    setupController(Observable.just(facility1, facility2))

    uiEvents.onNext(OverdueAppointmentCountUpdated(3))
    uiEvents.onNext(OverdueAppointmentCountUpdated(0))

    // then
    verify(ui).setFacility("CHC Buchho")
    verify(ui, times(3)).setFacility("CHC Nathana")
    verify(ui).showOverdueAppointmentCount(3)
    verify(ui).removeOverdueAppointmentCount()
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when facility change button is clicked facility selection screen should open`() {
    // given
    val facility = TestData.facility(
        uuid = UUID.fromString("e497355e-723c-4b35-b55a-778a6233b720"),
        name = "CHC Buchho"
    )

    // when
    setupController(Observable.just(facility))
    uiEvents.onNext(HomeFacilitySelectionClicked)
    uiEvents.onNext(OverdueAppointmentCountUpdated(0))

    // then
    verify(ui, times(2)).setFacility("CHC Buchho")
    verify(ui).removeOverdueAppointmentCount()
    verify(uiActions).openFacilitySelection()
    verifyNoMoreInteractions(ui, uiActions)
  }

  private fun setupController(facilityStream: Observable<Facility>) {
    clock.setDate(LocalDate.parse("2018-01-01"))

    val viewEffectHandler = HomeScreenViewEffectHandler(uiActions)
    val effectHandler = HomeScreenEffectHandler(
        currentFacilityStream = facilityStream,
        patientRepository = mock(),
        schedulersProvider = TestSchedulersProvider.trampoline(),
        viewEffectsConsumer = viewEffectHandler::handle
    )

    val uiRenderer = HomeScreenUiRenderer(ui)

    testFixture = MobiusTestFixture(
        events = uiEvents.ofType(),
        defaultModel = HomeScreenModel.create(),
        init = HomeScreenInit(),
        update = HomeScreenUpdate(),
        effectHandler = effectHandler.build(),
        modelUpdateListener = uiRenderer::render
    )

    testFixture.start()
  }
}
