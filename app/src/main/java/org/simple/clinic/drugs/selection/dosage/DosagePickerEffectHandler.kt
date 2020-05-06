package org.simple.clinic.drugs.selection.dosage

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.protocol.ProtocolRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.scheduler.SchedulersProvider
import java.util.UUID

class DosagePickerEffectHandler @AssistedInject constructor(
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository,
    private val protocolRepository: ProtocolRepository,
    private val prescriptionRepository: PrescriptionRepository,
    private val schedulers: SchedulersProvider,
    @Assisted private val uiActions: DosagePickerUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: DosagePickerUiActions): DosagePickerEffectHandler
  }

  fun build(): ObservableTransformer<DosagePickerEffect, DosagePickerEvent> {
    return RxMobius
        .subtypeEffectHandler<DosagePickerEffect, DosagePickerEvent>()
        .addTransformer(LoadProtocolDrugsByName::class.java, loadProtocolDrugs())
        .addTransformer(DeleteExistingPrescription::class.java, deleteExistingPrescription())
        .addAction(CloseScreen::class.java, uiActions::close, schedulers.ui())
        .addTransformer(ChangeExistingPrescription::class.java, changeExistingPrescription())
        .addTransformer(CreateNewPrescription::class.java, createNewPrescription())
        .build()
  }

  private fun loadProtocolDrugs(): ObservableTransformer<LoadProtocolDrugsByName, DosagePickerEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulers.io())
          .map { it.drugName }
          .switchMap { drugName ->
            // This is nasty, fix in the final clean up phase by making
            // a blocking call for the protocol UUID
            currentProtocolUuid().map { drugName to it }
          }
          .switchMap { (drugName, protocolUuid) -> protocolRepository.drugsByNameOrDefault(drugName, protocolUuid) }
          .map(::DrugsLoaded)
    }
  }

  private fun deleteExistingPrescription(): ObservableTransformer<DeleteExistingPrescription, DosagePickerEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulers.io())
          .map { it.prescriptionUuid }
          .flatMap {
            prescriptionRepository
                .softDeletePrescription(it)
                .andThen(Observable.just(ExistingPrescriptionDeleted as DosagePickerEvent))
          }
    }
  }

  private fun changeExistingPrescription(): ObservableTransformer<ChangeExistingPrescription, DosagePickerEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulers.io())
          .flatMap { effect ->
            // This is nasty, fix in the final clean up phase by making
            // a blocking call for the current facility
            currentFacility().map { effect to it }
          }
          .flatMap { (effect, currentFacility) ->
            val patientUuid = effect.patientUuid
            val existingPrescriptionUuid = effect.prescriptionUuid
            val newPrescriptionDrug = effect.protocolDrug

            prescriptionRepository
                .softDeletePrescription(existingPrescriptionUuid)
                .andThen(prescriptionRepository.savePrescription(
                    patientUuid = patientUuid,
                    drug = newPrescriptionDrug,
                    facility = currentFacility
                ))
                .andThen(Observable.just(ExistingPrescriptionChanged))
          }
    }
  }

  private fun createNewPrescription(): ObservableTransformer<CreateNewPrescription, DosagePickerEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulers.io())
          .flatMap { effect ->
            // This is nasty, fix in the final clean up phase by making
            // a blocking call for the current facility
            currentFacility().map { effect to it }
          }
          .flatMap { (effect, currentFacility) ->
            val patientUuid = effect.patientUuid
            val newPrescriptionDrug = effect.protocolDrug

            prescriptionRepository
                .savePrescription(
                    patientUuid = patientUuid,
                    drug = newPrescriptionDrug,
                    facility = currentFacility
                )
                .andThen(Observable.just(NewPrescriptionCreated))
          }
    }
  }

  private fun currentProtocolUuid(): Observable<UUID> {
    return userSession
        .requireLoggedInUser()
        .switchMap(facilityRepository::currentFacility)
        .map { it.protocolUuid }
  }

  private fun currentFacility(): Observable<Facility> {
    return userSession
        .requireLoggedInUser()
        .switchMap(facilityRepository::currentFacility)
  }
}
