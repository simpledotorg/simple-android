package org.simple.clinic.drugs.selection.dosage

import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.protocol.ProtocolRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Optional
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

private typealias Ui = DosagePickerUi
private typealias UiChange = (Ui) -> Unit

class DosagePickerSheetController @AssistedInject constructor(
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository,
    private val protocolRepository: ProtocolRepository,
    private val prescriptionRepository: PrescriptionRepository,
    @Assisted private val drugName: String,
    @Assisted private val patientUuid: UUID,
    @Assisted private val existingPrescribedDrugUuid: Optional<UUID>
) : ObservableTransformer<UiEvent, UiChange> {

  @AssistedInject.Factory
  interface Factory {
    fun create(drugName: String, patientUuid: UUID, existingPrescribedDrugUuid: Optional<UUID>): DosagePickerSheetController
  }

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .replay()

    return savePrescription(replayedEvents)
  }

  private fun savePrescription(events: Observable<UiEvent>): Observable<UiChange> {
    val dosageSelected = events
        .ofType<DosageSelected>()

    val softDeleteOldPrescription = dosageSelected
        .map { existingPrescribedDrugUuid }
        .filterAndUnwrapJust()
        .flatMap { existingPrescriptionUuid ->
          prescriptionRepository
              .softDeletePrescription(existingPrescriptionUuid)
              .andThen(Observable.empty<UiChange>())
        }

    val protocolDrug = dosageSelected
        .map { it.protocolDrug }

    val currentFacilityStream = userSession
        .requireLoggedInUser()
        .switchMap { facilityRepository.currentFacility(it) }

    val savePrescription = protocolDrug
        .withLatestFrom(currentFacilityStream)
        .flatMap { (drug, currentFacility) ->
          prescriptionRepository
              .savePrescription(
                  patientUuid = patientUuid,
                  drug = drug,
                  facility = currentFacility
              )
              .andThen(Observable.just { ui: Ui -> ui.close() })
        }

    return softDeleteOldPrescription.mergeWith(savePrescription)
  }
}
