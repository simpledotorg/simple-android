package org.simple.clinic.scheduleappointment.facilityselection

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.widgets.UiEvent
import org.simple.mobius.migration.MobiusTestFixture
import java.util.UUID

class FacilitySelectionLogicTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private val ui = mock<FacilitySelectionUi>()
  private val uiActions = mock<FacilitySelectionUiActions>()

  private lateinit var testFixture: MobiusTestFixture<FacilitySelectionModel, FacilitySelectionEvent, FacilitySelectionEffect>

  @After
  fun tearDown() {
    testFixture.dispose()
  }

  @Test
  fun `when facility is selected then it should be passed back as result`() {
    // given
    val newFacility = TestData.facility(uuid = UUID.fromString("758f6c2e-2bd9-415f-9aaa-665923034e92"))

    // when
    setupController()
    uiEvents.onNext(FacilitySelected(newFacility))

    // then
    verify(uiActions).sendSelectedFacility(newFacility)
    verifyNoMoreInteractions(ui, uiActions)
  }

  private fun setupController() {
    val effectHandler = FacilitySelectionEffectHandler(
        schedulers = TestSchedulersProvider.trampoline(),
        uiActions = uiActions
    )
    val uiRenderer = FacilitySelectionUiRenderer(ui)

    testFixture = MobiusTestFixture(
        events = uiEvents.ofType(),
        defaultModel = FacilitySelectionModel(),
        update = FacilitySelectionUpdate(),
        effectHandler = effectHandler.build(),
        init = FacilitySelectionInit(),
        modelUpdateListener = uiRenderer::render
    )
    testFixture.start()
  }
}
