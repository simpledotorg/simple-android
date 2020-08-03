package org.simple.clinic.facility.change

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import dagger.Lazy
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

class FacilityChangeLogicTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private val ui = mock<FacilityChangeUi>()
  private val uiActions = mock<FacilityChangeUiActions>()
  private val currentFacility = TestData.facility(uuid = UUID.fromString("6dc536d9-b460-4143-9b3b-7caedf17c0d9"))

  private lateinit var testFixture: MobiusTestFixture<FacilityChangeModel, FacilityChangeEvent, FacilityChangeEffect>

  @After
  fun tearDown() {
    testFixture.dispose()
  }

  @Test
  fun `when a new facility is selected then the confirmation sheet to change facility should open`() {
    //given
    val newFacility = TestData.facility(uuid = UUID.fromString("ce22e8b1-eba2-463f-8e91-0c237ebebf6b"))

    //when
    setupController()
    uiEvents.onNext(FacilityChangeClicked(newFacility))

    //then
    verify(uiActions).openConfirmationSheet(newFacility)
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when the same facility is selected then the sheet should close`() {
    //given
    val newFacility = currentFacility

    //when
    setupController()
    uiEvents.onNext(FacilityChangeClicked(newFacility))

    //then
    verify(uiActions).goBack()
    verifyNoMoreInteractions(ui, uiActions)
  }

  private fun setupController() {
    val uiRenderer = FacilityChangeUiRenderer(ui)
    val effectHandler = FacilityChangeEffectHandler(
        schedulers = TestSchedulersProvider.trampoline(),
        currentFacility = Lazy { currentFacility },
        uiActions = uiActions
    )

    testFixture = MobiusTestFixture(
        events = uiEvents.ofType(),
        defaultModel = FacilityChangeModel.create(),
        update = FacilityChangeUpdate(),
        effectHandler = effectHandler.build(),
        init = FacilityChangeInit(),
        modelUpdateListener = uiRenderer::render
    )
    testFixture.start()
  }
}
