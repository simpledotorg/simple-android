package org.simple.clinic.drugs.selectionv2

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.drugs.selection.AddNewPrescriptionClicked
import org.simple.clinic.drugs.selection.PrescribedDrugsDoneClicked
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.protocol.ProtocolDrugAndDosages
import org.simple.clinic.protocol.ProtocolRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = PrescribedDrugScreenV2
typealias UiChange = (Ui) -> Unit

class PrescribedDrugsScreenControllerV2 @Inject constructor(
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
        selectPrescription(replayedEvents),
        selectDosage(replayedEvents))
  }

  private fun populateDrugsList(events: Observable<UiEvent>): Observable<UiChange> {
    val patientUuid = events
        .ofType<org.simple.clinic.drugs.selectionv2.PrescribedDrugsScreenCreated>()
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

          val prescribedProtocolDrugs = prescribedDrugs.filter { it.isProtocolDrug }
          // Select protocol drugs if prescriptions exist for them.
          val protocolDrugSelectionItems = protocolDrugs
              .mapIndexed { index: Int, drugAndDosages: ProtocolDrugAndDosages ->
                val prescribedDrugDosage =
                    prescribedProtocolDrugs
                        .firstOrNull { it.name == drugAndDosages.drugName }

                ProtocolDrugListItem(
                    id = index,
                    drugName = drugAndDosages.drugName,
                    dosage = prescribedDrugDosage?.dosage)
              }

          val customPrescribedDrugItems = prescribedDrugs
              .filter { it.isProtocolDrug.not() }
              .sortedBy { it.updatedAt.toEpochMilli() }
              .mapIndexed { index: Int, drugAndDosages: PrescribedDrug -> ProtocolDrugListItem(index + protocolDrugSelectionItems.size, drugAndDosages.name, drugAndDosages.dosage) }

          protocolDrugSelectionItems + customPrescribedDrugItems
        }
        .map { { ui: Ui -> ui.populateDrugsList(it) } }
  }

  private fun selectDosage(events: Observable<UiEvent>): Observable<UiChange> {
    val patientUuids = events
        .ofType<PrescribedDrugsScreenCreated>()
        .map { it.patientUuid }
        .take(1)

    val drugSelected = events
        .ofType<ProtocolDrugSelected>()
        .map { it.drugName }

    return drugSelected
        .withLatestFrom(patientUuids)
        .map { (drugSelected, patientUuid) -> { ui: Ui -> ui.showDosageSelectionSheet(drugName = drugSelected, patientUuid = patientUuid) } }
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

  private fun handleDoneClicks(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<PrescribedDrugsDoneClicked>()
        .map { { ui: Ui -> ui.goBackToPatientSummary() } }
  }
}
