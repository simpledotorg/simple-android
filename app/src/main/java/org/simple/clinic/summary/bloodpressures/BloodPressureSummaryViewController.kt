package org.simple.clinic.summary.bloodpressures

import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.summary.PatientSummaryConfig
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

typealias Ui = BloodPressureSummaryUi

typealias UiChange = (Ui) -> Unit

class BloodPressureSummaryViewController @AssistedInject constructor(
    @Assisted private val patientUuid: UUID,
    private val config: PatientSummaryConfig,
    private val repository: BloodPressureRepository
) : ObservableTransformer<UiEvent, UiChange> {

  @AssistedInject.Factory
  interface Factory {
    fun create(patientUuid: UUID): BloodPressureSummaryViewController
  }

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .replay()

    return Observable.merge(
        displayBloodPressures(replayedEvents),
        openBloodPressureEntrySheet(replayedEvents),
        openBloodPressureUpdateSheet(replayedEvents)
    )
  }

  private fun displayBloodPressures(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<ScreenCreated>()
        .switchMap { repository.newestMeasurementsForPatient(patientUuid, config.numberOfBpsToDisplay) }
        .map { { ui: Ui -> ui.populateBloodPressures(it) } }
  }

  private fun openBloodPressureEntrySheet(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<NewBloodPressureClicked>()
        .map { { ui: Ui -> ui.showBloodPressureEntrySheet(patientUuid) } }
  }

  private fun openBloodPressureUpdateSheet(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<BloodPressureClicked>()
        .map { it.measurement }
        .map { clickedBp -> { ui: Ui -> ui.showBloodPressureUpdateSheet(clickedBp.uuid)} }
  }
}
