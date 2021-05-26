package org.simple.clinic.drugs.selection.dosage

import com.spotify.mobius.rx2.RxMobius
import dagger.Lazy
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.facility.Facility
import org.simple.clinic.protocol.ProtocolRepository
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.uuid.UuidGenerator
import java.util.UUID

class DosagePickerEffectHandler @AssistedInject constructor(
    private val protocolRepository: ProtocolRepository,
    private val prescriptionRepository: PrescriptionRepository,
    private val schedulers: SchedulersProvider,
    private val currentFacility: Lazy<Facility>,
    private val uuidGenerator: UuidGenerator,
    @Assisted private val uiActions: DosagePickerUiActions
) {

  @AssistedFactory
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
          .switchMap { protocolRepository.drugsByNameOrDefault(it.drugName, currentProtocolUuid()) }
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
            val patientUuid = effect.patientUuid
            val existingPrescriptionUuid = effect.prescriptionUuid
            val newPrescriptionDrug = effect.protocolDrug

            prescriptionRepository
                .softDeletePrescription(existingPrescriptionUuid)
                .andThen(prescriptionRepository.savePrescription(
                    uuid = uuidGenerator.v4(),
                    patientUuid = patientUuid,
                    drug = newPrescriptionDrug,
                    facility = currentFacility()
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
            val patientUuid = effect.patientUuid
            val newPrescriptionDrug = effect.protocolDrug

            prescriptionRepository
                .savePrescription(
                    uuid = uuidGenerator.v4(),
                    patientUuid = patientUuid,
                    drug = newPrescriptionDrug,
                    facility = currentFacility()
                )
                .andThen(Observable.just(NewPrescriptionCreated))
          }
    }
  }

  private fun currentProtocolUuid(): UUID {
    return currentFacility().protocolUuid!!
  }

  private fun currentFacility(): Facility {
    return currentFacility.get()
  }
}
