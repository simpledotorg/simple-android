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
        toggleRemoveButton(replayedEvents),
        prefillPrescription(replayedEvents),
        removePrescription(replayedEvents),
        closeSheetWhenPrescriptionIsDeleted(replayedEvents)
    )
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
