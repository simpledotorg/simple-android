package org.simple.clinic.drugs.selectionv2.dosage

import io.reactivex.Completable
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
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.widgets.UiEvent
import timber.log.Timber
import javax.inject.Inject

private typealias Ui = DosagePickerSheet
private typealias UiChange = (Ui) -> Unit

class DosagePickerSheetController @Inject constructor(
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository,
    private val protocolRepository: ProtocolRepository,
    private val prescriptionRepository: PrescriptionRepository
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .compose(mergeWithDosageSelected())
        .replay()

    return Observable.mergeArray(
        displayDosageList(replayedEvents),
        savePrescription(replayedEvents))
  }

  private fun displayDosageList(events: Observable<UiEvent>): Observable<UiChange> {
    val drugName = events
        .ofType<DosagePickerSheetCreated>()
        .map { it.drugName }

    val protocolUuid = drugName
        .flatMap { userSession.requireLoggedInUser() }
        .switchMap { facilityRepository.currentFacility(it) }
        .map { it.protocolUuid }

    return Observables
        .combineLatest(drugName, protocolUuid)
        .switchMap { (drugName, protocolUuid) -> protocolRepository.drugsByNameOrDefault(drugName = drugName, protocolUuid = protocolUuid) }
        .map { dosages ->
          val dosageItems = dosages.map { DosageListItem(DosageOption.Dosage(it)) }
          dosageItems + DosageListItem(DosageOption.None)
        }
        .map { dosages -> { ui: Ui -> ui.populateDosageList(dosages) } }
  }

  private fun mergeWithDosageSelected(): ObservableTransformer<UiEvent, UiEvent> {
    return ObservableTransformer { events ->
      val dosageType = events
          .ofType<DosageItemClicked>()
          .map {
            when (it.dosage) {
              is DosageOption.Dosage -> DosageSelected(it.dosage.protocolDrug)
              is DosageOption.None -> TODO()
            }
          }
      events.mergeWith(dosageType)
    }
  }

  private fun savePrescription(events: Observable<UiEvent>): Observable<UiChange> {
    val sheetCreated = events
        .ofType<DosagePickerSheetCreated>()

    val existingPrescription = sheetCreated
        .map { it.existingPrescribedDrugUuid }

    val dosageSelected = events
        .ofType<DosageSelected>()

    val softDeleteOldPrescription = dosageSelected
        .withLatestFrom(existingPrescription)
        .firstOrError()
        .flatMapCompletable { (_, existingPrescriptionUuid) ->
          when (existingPrescriptionUuid) {
            is Just -> prescriptionRepository.softDeletePrescription(existingPrescriptionUuid.value)
            is None -> Completable.complete()
          }
        }

    val patientUuids = sheetCreated
        .map { it.patientUuid }

    val protocolDrug = dosageSelected
        .map { it.protocolDrug }

    val savePrescription = protocolDrug
        .withLatestFrom(patientUuids)
        .doOnNext { Timber.e("save prescription withLatestFrom") }
        .flatMap { (drug, patientUuid) ->
          Timber.e("save prescription flatmap")
          prescriptionRepository
              .savePrescription(
                  patientUuid = patientUuid,
                  drug = drug)
              .andThen(Observable.just { ui: Ui -> ui.finish() })
        }

    return softDeleteOldPrescription.andThen(savePrescription)
  }
}
