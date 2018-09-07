package org.simple.clinic.drugs.selection.entry

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.util.nullIfBlank
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

private typealias Ui = CustomPrescriptionEntrySheet
private typealias UiChange = (Ui) -> Unit
const val DOSAGE_PLACEHOLDER = "mg"

class CustomPrescriptionEntryController @Inject constructor(
    private val prescriptionRepository: PrescriptionRepository
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.compose(ReportAnalyticsEvents()).replay().refCount()

    return Observable.mergeArray(
        toggleSaveButton(replayedEvents),
        savePrescriptionsAndDismiss(replayedEvents),
        showDefaultDosagePlaceholder(replayedEvents))
  }

  private fun toggleSaveButton(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<CustomPrescriptionDrugNameTextChanged>()
        .map { it.name.isNotBlank() }
        .distinctUntilChanged()
        .map { canBeSaved -> { ui: Ui -> ui.setSaveButtonEnabled(canBeSaved) } }
  }

  private fun savePrescriptionsAndDismiss(events: Observable<UiEvent>): Observable<UiChange> {
    val patientUuids = events
        .ofType<CustomPrescriptionSheetCreated>()
        .map { it.patientUuid }
        .take(1)

    val nameChanges = events
        .ofType<CustomPrescriptionDrugNameTextChanged>()
        .map { it.name }

    val dosageChanges = events
        .ofType<CustomPrescriptionDrugDosageTextChanged>()
        .map { it.dosage }

    val saveClicks = events
        .ofType<SaveCustomPrescriptionClicked>()

    return Observables
        .combineLatest(saveClicks, patientUuids, nameChanges, dosageChanges) { _, uuid, name, dosage -> Triple(uuid, name, dosage) }
        .flatMap { (patientUuid, name, dosage) ->
          prescriptionRepository
              .savePrescription(patientUuid, name, dosage.nullIfBlank(), rxNormCode = null, isProtocolDrug = false)
              .andThen(Observable.just({ ui: Ui -> ui.finish() }))
        }
  }

  /**
   * The dosage field shows a default text as "mg". When it is focused, the cursor will
   * by default be moved to the end. This will force the user to either move the cursor
   * to the end manually or delete everything and essentially making the placeholder
   * useless. As a workaround, we move the cursor to the starting again.
   */
  private fun showDefaultDosagePlaceholder(events: Observable<UiEvent>): Observable<UiChange> {
    val dosageTextChanges = events
        .ofType<CustomPrescriptionDrugDosageTextChanged>()
        .map { it.dosage }

    val dosageFocusChanges = events
        .ofType<CustomPrescriptionDrugDosageFocusChanged>()
        .map { it.hasFocus }

    val setPlaceholder = Observables.combineLatest(dosageFocusChanges, dosageTextChanges)
        .filter { (hasFocus, text) -> hasFocus && text.isBlank() }
        .take(1)
        .map { { ui: Ui -> ui.setDrugDosageText(DOSAGE_PLACEHOLDER) } }

    val resetPlaceholder = Observables.combineLatest(dosageFocusChanges, dosageTextChanges)
        .filter { (hasFocus, text) -> !hasFocus && text.trim() == DOSAGE_PLACEHOLDER }
        .map { { ui: Ui -> ui.setDrugDosageText("") } }

    val moveCursorToStart = Observables.combineLatest(dosageFocusChanges, dosageTextChanges)
        .filter { (hasFocus, text) -> hasFocus && text.trim() == DOSAGE_PLACEHOLDER }
        .map { { ui: Ui -> ui.moveDrugDosageCursorToBeginning() } }

    return Observable.merge(setPlaceholder, resetPlaceholder, moveCursorToStart)
  }
}
