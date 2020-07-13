package org.simple.clinic.home.report

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.reports.ReportsRepository
import org.simple.clinic.util.Optional
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.widgets.UiEvent
import org.simple.mobius.migration.MobiusTestFixture

class ReportsScreenLogicTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val ui = mock<ReportsUi>()
  private val uiEvents = PublishSubject.create<UiEvent>()
  private val reportsRepository = mock<ReportsRepository>()

  private lateinit var textFixture: MobiusTestFixture<ReportsModel, ReportsEvent, ReportsEffect>

  @After
  fun tearDown() {
    textFixture.dispose()
  }

  @Test
  fun `when a reports file is emitted then update the screen`() {
    // given
    val reportsContent = "Reports"
    whenever(reportsRepository.reportsContentText()).thenReturn(Observable.just(Optional.of(reportsContent)))

    // when
    setupController()

    // then
    verify(ui).showReport(reportsContent)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `screen should be updated whenever the reports file changes`() {
    // given
    val firstReport = "Reports for yesterday"
    val secondReport = "Reports for today"
    whenever(reportsRepository.reportsContentText()).thenReturn(Observable.just(
        Optional.of(firstReport),
        Optional.of(secondReport)
    ))

    // when
    setupController()

    // then
    verify(ui).showReport(firstReport)
    verify(ui).showReport(secondReport)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the reports file does not exist then screen should show no-reports view`() {
    // given
    whenever(reportsRepository.reportsContentText()).thenReturn(Observable.just(Optional.empty()))

    // when
    setupController()

    // then
    verify(ui).showNoReportsAvailable()
    verifyNoMoreInteractions(ui)
  }

  private fun setupController() {
    val effectHandler = ReportsEffectHandler(
        reportsRepository = reportsRepository,
        schedulersProvider = TestSchedulersProvider.trampoline()
    )
    val uiRenderer = ReportsUiRenderer(ui)

    textFixture = MobiusTestFixture(
        events = uiEvents.ofType(),
        defaultModel = ReportsModel.create(),
        init = ReportsInit(),
        update = ReportsUpdate(),
        effectHandler = effectHandler.build(),
        modelUpdateListener = uiRenderer::render
    )
    textFixture.start()
  }
}
