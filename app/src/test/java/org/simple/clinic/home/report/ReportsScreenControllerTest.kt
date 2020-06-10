package org.simple.clinic.home.report

import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.reports.ReportsRepository
import org.simple.clinic.util.None
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.toOptional
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import java.io.File
import java.net.URI

class ReportsScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val screen = mock<ReportsScreen>()

  private val uiEvents = PublishSubject.create<UiEvent>()

  private val reportsRepository: ReportsRepository = mock()
  private val controller = ReportsScreenController(reportsRepository)

  @Before
  fun setUp() {
    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when a reports file is emitted then update the screen`() {
    val file: File = mock()
    val uri = URI("")

    whenever(file.toURI()).thenReturn(uri)
    whenever(reportsRepository.reportsFile()).thenReturn(Observable.just(file.toOptional()))

    uiEvents.onNext(ScreenCreated())

    verify(screen).showReport(uri)
  }

  @Test
  fun `screen should be updated whenever the reports file changes`() {
    val file1: File = mock()
    val file2: File = mock()
    val uri1 = URI("uri1")
    val uri2 = URI("uri2")

    whenever(file1.toURI()).thenReturn(uri1)
    whenever(file2.toURI()).thenReturn(uri2)
    whenever(reportsRepository.reportsFile()).thenReturn(Observable.just(
        file1.toOptional(),
        file2.toOptional()
    ))

    uiEvents.onNext(ScreenCreated())

    val inorder = inOrder(screen)
    inorder.verify(screen).showReport(uri1)
    inorder.verify(screen).showReport(uri2)
  }

  @Test
  fun `when the reports file does not exist then screen should show no-reports view`() {
    whenever(reportsRepository.reportsFile()).thenReturn(Observable.just(None()))

    uiEvents.onNext(ScreenCreated())

    verify(screen).showNoReportsAvailable()
  }
}
