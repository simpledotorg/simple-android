package org.simple.clinic.drugs.selection

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.drugs.selection.entry.CustomPrescribedDrugListItem
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.protocol.ProtocolDrugAndDosages
import org.simple.clinic.protocol.ProtocolRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = PrescribedDrugScreen
typealias UiChange = (Ui) -> Unit

class PrescribedDrugsScreenController @Inject constructor(
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository,
    private val protocolRepository: ProtocolRepository,
    private val prescriptionRepository: PrescriptionRepository
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .replay()

    return Observable.mergeArray(
        handleDoneClicks(replayedEvents),
        populateDrugsList(replayedEvents),
        openNewCustomPrescription(replayedEvents),
        openDosagePicker(replayedEvents),
        openUpdateCustomPrescription(replayedEvents))
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

          val customDrugs = prescribedDrugs
              .filter { it.isProtocolDrug.not() }
          val customPrescribedDrugItems = customDrugs
              .sortedBy { it.updatedAt.toEpochMilli() }
              .mapIndexed { index, prescribedDrug -> CustomPrescribedDrugListItem(prescribedDrug, index == customDrugs.lastIndex) }

          protocolDrugSelectionItems + customPrescribedDrugItems
        }
        .map { { ui: Ui -> ui.populateDrugsList(it) } }
  }

  private fun openDosagePicker(events: Observable<UiEvent>): Observable<UiChange> {
    val patientUuids = events
        .ofType<PrescribedDrugsScreenCreated>()
        .map { it.patientUuid }
        .take(1)

    return events
        .ofType<ProtocolDrugClicked>()
        .withLatestFrom(patientUuids)
        .map { (selectedDrug, patientUuid) ->
          { ui: Ui ->
            ui.showDosageSelectionSheet(
                drugName = selectedDrug.drugName,
                patientUuid = patientUuid,
                prescribedDrugUuid = selectedDrug.prescriptionForProtocolDrug?.uuid
            )
          }
        }
  }

  private fun openNewCustomPrescription(events: Observable<UiEvent>): Observable<UiChange> {
    val patientUuids = events
        .ofType<PrescribedDrugsScreenCreated>()
        .map { it.patientUuid }
        .take(1)

    return events.ofType<AddNewPrescriptionClicked>()
        .withLatestFrom(patientUuids) { _, patientUuid -> patientUuid }
        .map { patientUuid -> { ui: Ui -> ui.showNewPrescriptionEntrySheet(patientUuid) } }
  }

  private fun openUpdateCustomPrescription(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<CustomPrescriptionClicked>()
        .map { { ui: Ui -> ui.showUpdateCustomPrescriptionSheet(it.prescribedDrug) } }
  }

  private fun handleDoneClicks(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<PrescribedDrugsDoneClicked>()
        .map { { ui: Ui -> ui.goBackToPatientSummary() } }
  }
}
