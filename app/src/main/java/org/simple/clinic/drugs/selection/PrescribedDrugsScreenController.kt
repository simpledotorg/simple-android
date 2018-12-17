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
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.protocolv2.ProtocolDrugAndDosages
import org.simple.clinic.protocolv2.ProtocolRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = PrescribedDrugsScreen
typealias UiChange = (Ui) -> Unit
typealias DrugName = String
typealias DrugDosage = String

class PrescribedDrugsScreenController @Inject constructor(
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository,
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
    val protocolDrugsStream = patientUuid
        .flatMap { userSession.requireLoggedInUser() }
        .switchMap { facilityRepository.currentFacility(it) }
        .switchMap { protocolRepository.drugsForProtocolOrDefault(it.protocolUuid) }

    val prescribedDrugsStream = patientUuid
        .flatMap { prescriptionRepository.newestPrescriptionsForPatient(it) }

    return Observables
        .combineLatest(protocolDrugsStream, prescribedDrugsStream)
        .map { (protocolDrugs, prescribedDrugs) ->
          val protocolPrescribedDrugsMap = HashMap<Pair<DrugName, DrugDosage>, PrescribedDrug>(prescribedDrugs.size)
          prescribedDrugs
              .filter { it.isProtocolDrug }
              .forEach { protocolPrescribedDrugsMap[it.name to it.dosage!!] = it }

          // Select protocol drugs if prescriptions exist for them.
          val protocolDrugSelectionItems = protocolDrugs
              .mapIndexed { index: Int, drugAndDosages: ProtocolDrugAndDosages ->
                val drug1 = drugAndDosages.drugs[0]
                val drug2 = drugAndDosages.drugs[1]

                val isDosage1Prescribed = protocolPrescribedDrugsMap.contains(drug1.name to drug1.dosage)
                val isDosage2Prescribed = protocolPrescribedDrugsMap.contains(drug2.name to drug2.dosage)

                ProtocolDrugSelectionListItem(
                    id = index,
                    drugName = drugAndDosages.drugName,
                    option1 = when {
                      isDosage1Prescribed -> DosageOption.Selected(drug1, prescription = protocolPrescribedDrugsMap[drug1.name to drug1.dosage]!!)
                      else -> DosageOption.Unselected(drug1)
                    },
                    option2 = when {
                      isDosage2Prescribed -> DosageOption.Selected(drug2, prescription = protocolPrescribedDrugsMap[drug2.name to drug2.dosage]!!)
                      else -> DosageOption.Unselected(drug2)
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
              .savePrescription(patientUuid, selectedEvent.drug)
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
