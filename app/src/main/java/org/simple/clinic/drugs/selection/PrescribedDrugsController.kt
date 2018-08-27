package org.simple.clinic.drugs.selection

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.drugs.selection.ProtocolDrugSelectionListItem.DosageOption
import org.simple.clinic.protocol.ProtocolRepository
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = PrescribedDrugsScreen
typealias UiChange = (Ui) -> Unit
typealias DrugName = String
typealias DrugDosage = String

class PrescribedDrugsEntryController @Inject constructor(
    private val protocolRepository: ProtocolRepository,
    private val prescriptionRepository: PrescriptionRepository
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.compose(ReportAnalyticsEvents()).replay(1).refCount()

    return Observable.mergeArray(
        handleDoneClicks(replayedEvents),
        populateDrugsList(replayedEvents),
        savePrescriptions(replayedEvents),
        selectPrescription(replayedEvents),
        unselectPrescriptions(replayedEvents),
        showConfirmDeletePrescriptionDialog(replayedEvents))
  }

  private fun populateDrugsList(events: Observable<UiEvent>): Observable<UiChange> {
    val patientUuid = events
        .ofType<PrescribedDrugsScreenCreated>()
        .map { it.patientUuid }
        .take(1)

    // Flat-mapping with patientUuid is not required, but is helpful in
    // tests to block execution until an event is emitted. In this case,
    // PrescribedDrugsScreenCreated is that event.
    val protocolDrugs = patientUuid
        .flatMap { protocolRepository.currentProtocol() }
        .map { it.drugs }

    val prescribedDrugs = patientUuid
        .flatMap { prescriptionRepository.newestPrescriptionsForPatient(it) }

    return Observables
        .combineLatest(protocolDrugs, prescribedDrugs)
        .map { (protocolDrugs, prescribedDrugs) ->
          val protocolPrescribedDrugsMap = HashMap<Pair<DrugName, DrugDosage>, PrescribedDrug>(prescribedDrugs.size)
          prescribedDrugs
              .filter { it.isProtocolDrug }
              .forEach { protocolPrescribedDrugsMap[it.name to it.dosage!!] = it }

          // Select protocol drugs if prescriptions exist for them.
          val protocolDrugSelectionItems = protocolDrugs
              .mapIndexed { index, drug ->
                val dosage1 = drug.dosages[0]
                val dosage2 = drug.dosages[1]
                val isDosage1Selected = protocolPrescribedDrugsMap.contains(drug.name to dosage1)
                val isDosage2Selected = protocolPrescribedDrugsMap.contains(drug.name to dosage2)

                ProtocolDrugSelectionListItem(
                    id = index,
                    drug = drug,
                    option1 = when {
                      isDosage1Selected -> DosageOption.Selected(dosage = dosage1, prescription = protocolPrescribedDrugsMap[drug.name to dosage1]!!)
                      else -> DosageOption.Unselected(dosage = dosage1)
                    },
                    option2 = when {
                      isDosage2Selected -> DosageOption.Selected(dosage = dosage2, prescription = protocolPrescribedDrugsMap[drug.name to dosage2]!!)
                      else -> DosageOption.Unselected(dosage = dosage2)
                    })
              }

          val customPrescribedDrugItems = prescribedDrugs
              .filter { it.isProtocolDrug.not() }
              .sortedBy { it.updatedAt.toEpochMilli() }
              .map { CustomPrescribedDrugListItem(it) }

          protocolDrugSelectionItems + customPrescribedDrugItems
        }
        .map { { ui: Ui -> ui.populateDrugsList(it) } }
  }

  private fun savePrescriptions(events: Observable<UiEvent>): Observable<UiChange> {
    val patientUuids = events
        .ofType<PrescribedDrugsScreenCreated>()
        .map { it.patientUuid }
        .take(1)

    return events.ofType<ProtocolDrugDosageSelected>()
        .withLatestFrom(patientUuids)
        .flatMap { (selectedEvent, patientUuid) ->
          prescriptionRepository
              .savePrescription(patientUuid, selectedEvent.drug, selectedEvent.dosage)
              .andThen(Observable.never<UiChange>())
        }
  }

  private fun unselectPrescriptions(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<ProtocolDrugDosageUnselected>()
        .map { it.prescription }
        .flatMap {
          prescriptionRepository
              .softDeletePrescription(it.uuid)
              .andThen(Observable.never<UiChange>())
        }
  }

  private fun selectPrescription(events: Observable<UiEvent>): Observable<UiChange> {
    val patientUuids = events
        .ofType<PrescribedDrugsScreenCreated>()
        .map { it.patientUuid }
        .take(1)

    return events.ofType<AddNewPrescriptionClicked>()
        .withLatestFrom(patientUuids) { _, patientUuid -> patientUuid }
        .map { patientUuid -> { ui: Ui -> ui.showNewPrescriptionEntrySheet(patientUuid) } }
  }

  private fun showConfirmDeletePrescriptionDialog(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<DeleteCustomPrescriptionClicked>()
        .map { it.prescription }
        .map { { ui: Ui -> ui.showDeleteConfirmationDialog(prescription = it) } }
  }

  private fun handleDoneClicks(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<PrescribedDrugsDoneClicked>()
        .map { { ui: Ui -> ui.goBackToPatientSummary() } }
  }
}
