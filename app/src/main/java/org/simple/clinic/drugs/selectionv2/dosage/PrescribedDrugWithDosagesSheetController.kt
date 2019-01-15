package org.simple.clinic.drugs.selectionv2.dosage

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.protocol.ProtocolRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

private typealias Ui = PrescribedDrugWithDosagesSheet
private typealias UiChange = (Ui) -> Unit

class PrescribedDrugWithDosagesSheetController @Inject constructor(
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository,
    private val protocolRepository: ProtocolRepository,
    val prescriptionRepository: PrescriptionRepository
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .compose(handleDosageType())
        .replay()

    return Observable.mergeArray(
        displayDosageList(replayedEvents),
        savePrescription(replayedEvents))
  }

  private fun displayDosageList(events: Observable<UiEvent>): Observable<UiChange> {
    val drugName = events
        .ofType<PrescribedDrugsWithDosagesSheetCreated>()
        .map { it.drugName }

    val protocolUuid = drugName
        .flatMap { userSession.requireLoggedInUser() }
        .switchMap { facilityRepository.currentFacility(it) }
        .map { it.protocolUuid }

    return Observables
        .combineLatest(drugName, protocolUuid)
        .switchMap { (drugName, protocolUuid) -> protocolRepository.dosagesForDrug(drugName = drugName, protocolUuid = protocolUuid) }
        .map { dosages ->
          val dosageItems = dosages.map { PrescribedDosageListItem(DosageType.Dosage(it)) }
          dosageItems + PrescribedDosageListItem(DosageType.None)
        }
        .map { dosages -> { ui: Ui -> ui.populateDosageList(dosages) } }
  }

  private fun handleDosageType(): ObservableTransformer<UiEvent, UiEvent> {
    return ObservableTransformer { events ->
      val dosageType = events
          .ofType<DosageItemClicked>()
          .map {
            when (it.dosage) {
              is DosageType.Dosage -> DosageSelected(it.dosage.dosage)
              is DosageType.None -> NoneSelected
            }
          }
      events.mergeWith(dosageType)
    }
  }

  private fun savePrescription(events: Observable<UiEvent>): Observable<UiChange> {
    val patientUuids = events
        .ofType<PrescribedDrugsWithDosagesSheetCreated>()
        .map { it.patientUuid }

    val drugName = events
        .ofType<PrescribedDrugsWithDosagesSheetCreated>()
        .map { it.drugName }

    val protocolUuid = events
        .ofType<PrescribedDrugsWithDosagesSheetCreated>()
        .flatMap { userSession.requireLoggedInUser() }
        .switchMap { facilityRepository.currentFacility(it) }
        .map { it.protocolUuid }

    val dosage = events
        .ofType<DosageSelected>()
        .map { it.dosage }

    data class PrescribedDrug(val name: String, val dosage: String, val rxNormCode: String?)

    val drug = Observables
        .combineLatest(drugName, dosage, protocolUuid) { name, dosage, protocolUuid ->
          val rxNormCode = protocolRepository.drugByNameAndDosage(name, dosage, protocolUuid)?.rxNormCode
          PrescribedDrug(name = name, dosage = dosage, rxNormCode = rxNormCode)
        }

    return drug
        .withLatestFrom(patientUuids)
        .flatMap { (drug, patientUuid) ->
          prescriptionRepository
              .savePrescription(
                  patientUuid = patientUuid,
                  name = drug.name,
                  dosage = drug.dosage,
                  rxNormCode = drug.rxNormCode,
                  isProtocolDrug = true)
              .andThen(Observable.never<UiChange>())
        }
  }
}
