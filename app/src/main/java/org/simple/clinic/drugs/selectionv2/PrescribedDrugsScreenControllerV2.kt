package org.simple.clinic.drugs.selectionv2

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.drugs.selection.AddNewPrescriptionClicked
import org.simple.clinic.drugs.selection.CustomPrescribedDrugListItem
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
        addNewPrescription(replayedEvents),
        selectDosage(replayedEvents))
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

          val prescribedProtocolDrugs = prescribedDrugs.filter { it.isProtocolDrug }
          val isAtLeastOneCustomDrugPrescribed = prescribedDrugs.any { it.isProtocolDrug.not() }
          // Show dosage if prescriptions exist for them.
          val protocolDrugSelectionItems = protocolDrugs
              .mapIndexed { index: Int, drugAndDosages: ProtocolDrugAndDosages ->
                val matchingPrescribedDrug = prescribedProtocolDrugs.firstOrNull { it.name == drugAndDosages.drugName }
                ProtocolDrugListItem(
                    id = index,
                    drugName = drugAndDosages.drugName,
                    prescribedDrug = matchingPrescribedDrug,
                    hideDivider = isAtLeastOneCustomDrugPrescribed.not() && index == protocolDrugs.lastIndex)
              }

          val customPrescribedDrugItems = prescribedDrugs
              .filter { it.isProtocolDrug.not() }
              .sortedBy { it.updatedAt.toEpochMilli() }
              .map { prescribedDrug -> CustomPrescribedDrugListItem(prescribedDrug) }

          protocolDrugSelectionItems + customPrescribedDrugItems
        }
        .map { { ui: Ui -> ui.populateDrugsList(it) } }
  }

  private fun selectDosage(events: Observable<UiEvent>): Observable<UiChange> {
    val patientUuids = events
        .ofType<PrescribedDrugsScreenCreated>()
        .map { it.patientUuid }
        .take(1)

    return events
        .ofType<ProtocolDrugSelected>()
        .withLatestFrom(patientUuids)
        .map { (selectedDrug, patientUuid) ->
          { ui: Ui ->
            ui.showDosageSelectionSheet(
                drugName = selectedDrug.drugName,
                patientUuid = patientUuid,
                prescribedDrugUuid = selectedDrug.prescribedDrug?.uuid
            )
          }
        }
  }

  private fun addNewPrescription(events: Observable<UiEvent>): Observable<UiChange> {
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
