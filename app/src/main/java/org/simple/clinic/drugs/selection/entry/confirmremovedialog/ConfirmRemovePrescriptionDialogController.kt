package org.simple.clinic.drugs.selection.entry.confirmremovedialog

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.bp.entry.confirmremovebloodpressure.ConfirmRemovePrescriptionDialogCreated
import org.simple.clinic.bp.entry.confirmremovebloodpressure.RemovePrescriptionClicked
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = ConfirmRemovePrescriptionDialog
typealias UiChange = (Ui) -> Unit

class ConfirmRemovePrescriptionDialogController @Inject constructor(
    private val prescriptionRepository: PrescriptionRepository
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .replay()
    return removePrescription(replayedEvents)
  }

  private fun removePrescription(events: Observable<UiEvent>): Observable<UiChange> {
    val prescribedDrugUuids = events
        .ofType<ConfirmRemovePrescriptionDialogCreated>()
        .map { it.prescribedDrugUuid }

    return events
        .ofType<RemovePrescriptionClicked>()
        .withLatestFrom(prescribedDrugUuids) { _, uuid -> uuid }
        .flatMap {
          prescriptionRepository
              .softDeletePrescription(it)
              .andThen(Observable.just({ ui: Ui -> ui.dismiss() }))
        }
  }

}
