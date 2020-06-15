package org.simple.clinic.home.report

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.reports.ReportsRepository
import org.simple.clinic.util.extractIfPresent
import org.simple.clinic.util.filterNotPresent
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = ReportsScreen
typealias UiChange = (Ui) -> Unit

class ReportsScreenController @Inject constructor(
    private val reportsRepository: ReportsRepository
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): Observable<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .replay()

    val reports = replayedEvents
        .ofType<ScreenCreated>()
        .flatMap { reportsRepository.reportsContentText() }
        .replay()
        .refCount()

    val showReports = reports
        .extractIfPresent()
        .map { helpContent -> { ui: Ui -> ui.showReport(helpContent) } }

    val showReportNotPresent = reports
        .filterNotPresent()
        .map { Ui::showNoReportsAvailable }

    return showReports.mergeWith(showReportNotPresent)
  }
}
