package org.simple.clinic.drugs.selection.entry

import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.facility.Facility
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

private typealias Ui = CustomPrescriptionEntryUi
private typealias UiChange = (Ui) -> Unit

const val DOSAGE_PLACEHOLDER = "mg"

class CustomPrescriptionEntryController @AssistedInject constructor(
    private val prescriptionRepository: PrescriptionRepository,
    @Assisted private val openAs: OpenAs
) : ObservableTransformer<UiEvent, UiChange> {

  @AssistedInject.Factory
  interface Factory {
    fun create(openAs: OpenAs): CustomPrescriptionEntryController
  }

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .replay()

    return Observable.mergeArray(
        showDefaultDosagePlaceholder(replayedEvents),
        updateSheetTitle(replayedEvents),
        toggleRemoveButton(replayedEvents),
        prefillPrescription(replayedEvents),
        removePrescription(replayedEvents),
        closeSheetWhenPrescriptionIsDeleted(replayedEvents)
    )
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

  private fun updateSheetTitle(events: Observable<UiEvent>): Observable<UiChange> {
    val openAsStream = events
        .ofType<ScreenCreated>()
        .map { openAs }

    val showEnterNewPrescription = openAsStream
        .filter { it is OpenAs.New }
        .map { { ui: Ui -> ui.showEnterNewPrescriptionTitle() } }

    val showEditPrescription = openAsStream
        .filter { it is OpenAs.Update }
        .map { { ui: Ui -> ui.showEditPrescriptionTitle() } }

    return showEnterNewPrescription.mergeWith(showEditPrescription)
  }

  private fun toggleRemoveButton(events: Observable<UiEvent>): Observable<UiChange> {
    val openAsStream = events
        .ofType<ScreenCreated>()
        .map { openAs }

    val hideRemoveButton = openAsStream
        .filter { it is OpenAs.New }
        .map { { ui: Ui -> ui.hideRemoveButton() } }

    val showRemoveButton = openAsStream
        .filter { it is OpenAs.Update }
        .map { { ui: Ui -> ui.showRemoveButton() } }

    return showRemoveButton.mergeWith(hideRemoveButton)
  }

  private fun prefillPrescription(events: Observable<UiEvent>): Observable<UiChange> {
    val openAsUpdate = events
        .ofType<ScreenCreated>()
        .filter { openAs is OpenAs.Update }
        .map { openAs as OpenAs.Update }

    return openAsUpdate
        .flatMap { prescriptionRepository.prescription(it.prescribedDrugUuid).take(1) }
        .map {
          { ui: Ui ->
            ui.setMedicineName(it.name)
            ui.setDosage(it.dosage)
          }
        }
  }

  private fun removePrescription(events: Observable<UiEvent>): Observable<UiChange> {
    val openAsUpdate = events
        .ofType<ScreenCreated>()
        .filter { openAs is OpenAs.Update }
        .map { openAs as OpenAs.Update }
        .map { it.prescribedDrugUuid }
        .take(1)

    return events
        .ofType<RemoveCustomPrescriptionClicked>()
        .withLatestFrom(openAsUpdate)
        .map { (_, prescribedDrugUuid) -> { ui: Ui -> ui.showConfirmRemoveMedicineDialog(prescribedDrugUuid) } }
  }

  private fun closeSheetWhenPrescriptionIsDeleted(events: Observable<UiEvent>): Observable<UiChange> {
    val prescribedDrugUuuids = events
        .ofType<ScreenCreated>()
        .filter { openAs is OpenAs.Update }
        .map { openAs as OpenAs.Update }
        .map { it.prescribedDrugUuid }

    return prescribedDrugUuuids
        .flatMap(prescriptionRepository::prescription)
        .filter { it.isDeleted }
        .take(1)
        .map { { ui: Ui -> ui.finish() } }
  }

  private data class SavePrescription(
      val patientUuid: UUID,
      val name: String,
      val dosage: String,
      val facility: Facility
  )
}
