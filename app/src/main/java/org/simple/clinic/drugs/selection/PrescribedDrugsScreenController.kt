package org.simple.clinic.drugs.selection

import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.drugs.AddNewPrescriptionClicked
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.drugs.ProtocolDrugClicked
import org.simple.clinic.drugs.selection.entry.CustomPrescribedDrugListItem
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.protocol.ProtocolDrugAndDosages
import org.simple.clinic.protocol.ProtocolRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

typealias Ui = PrescribedDrugUi
typealias UiChange = (Ui) -> Unit

class PrescribedDrugsScreenController @AssistedInject constructor(
    @Assisted private val patientUuid: UUID,
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository,
    private val protocolRepository: ProtocolRepository,
    private val prescriptionRepository: PrescriptionRepository
) : ObservableTransformer<UiEvent, UiChange> {

  @AssistedInject.Factory
  interface Factory {
    fun create(patientUuid: UUID): PrescribedDrugsScreenController
  }

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .replay()

    return Observable.mergeArray(
        handleDoneClicks(replayedEvents),
        populateDrugsList(replayedEvents),
        openUpdateCustomPrescription(replayedEvents))
  }

  private fun populateDrugsList(events: Observable<UiEvent>): Observable<UiChange> {
    val screenCreates = events
        .ofType<ScreenCreated>()
        .take(1)

    val protocolDrugsStream = screenCreates
        .flatMap { userSession.requireLoggedInUser() }
        .switchMap { facilityRepository.currentFacility(it) }
        .switchMap { protocolRepository.drugsForProtocolOrDefault(it.protocolUuid) }

    val prescribedDrugsStream = screenCreates
        .flatMap { prescriptionRepository.newestPrescriptionsForPatient(patientUuid) }

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
