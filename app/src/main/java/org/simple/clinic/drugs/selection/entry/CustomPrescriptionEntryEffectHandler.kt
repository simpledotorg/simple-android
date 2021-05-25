package org.simple.clinic.drugs.selection.entry

import com.spotify.mobius.rx2.RxMobius
import dagger.Lazy
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.facility.Facility
import org.simple.clinic.util.nullIfBlank
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.uuid.UuidGenerator
import java.util.UUID

class CustomPrescriptionEntryEffectHandler @AssistedInject constructor(
    @Assisted private val uiActions: CustomPrescriptionEntryUiActions,
    private val schedulersProvider: SchedulersProvider,
    private val prescriptionRepository: PrescriptionRepository,
    private val currentFacility: Lazy<Facility>,
    private val uuidGenerator: UuidGenerator
) {

  @AssistedFactory
  interface Factory {
    fun create(uiActions: CustomPrescriptionEntryUiActions): CustomPrescriptionEntryEffectHandler
  }

  fun build()
      : ObservableTransformer<CustomPrescriptionEntryEffect, CustomPrescriptionEntryEvent> {
    return RxMobius
        .subtypeEffectHandler<CustomPrescriptionEntryEffect, CustomPrescriptionEntryEvent>()
        .addTransformer(SaveCustomPrescription::class.java, saveNewPrescription())
        .addTransformer(UpdatePrescription::class.java, updatePrescription())
        .addTransformer(FetchPrescription::class.java, fetchPrescription())
        .addConsumer(SetMedicineName::class.java, { uiActions.setMedicineName(it.drugName) }, schedulersProvider.ui())
        .addConsumer(SetDosage::class.java, { uiActions.setDosage(it.dosage) }, schedulersProvider.ui())
        .addConsumer(
            ShowConfirmRemoveMedicineDialog::class.java,
            { uiActions.showConfirmRemoveMedicineDialog(it.prescriptionUuid) },
            schedulersProvider.ui()
        )
        .addAction(CloseSheet::class.java, uiActions::finish, schedulersProvider.ui())
        .build()
  }

  private fun updatePrescription(): ObservableTransformer<UpdatePrescription, CustomPrescriptionEntryEvent> {
    return ObservableTransformer { effects ->
      effects
          .flatMap { effect ->
            prescriptionRepository
                .softDeletePrescription(effect.prescriptionUuid)
                .andThen(savePrescription(
                    uuid = uuidGenerator.v4(),
                    patientUuid = effect.patientUuid,
                    drugName = effect.drugName,
                    dosage = effect.dosage
                ))
                .andThen(Observable.just(CustomPrescriptionSaved))
          }
    }
  }

  private fun saveNewPrescription(): ObservableTransformer<SaveCustomPrescription, CustomPrescriptionEntryEvent> {
    return ObservableTransformer { effects ->
      effects
          .flatMap { savePrescription ->
            savePrescription(
                uuid = uuidGenerator.v4(),
                patientUuid = savePrescription.patientUuid,
                drugName = savePrescription.drugName,
                dosage = savePrescription.dosage
            ).andThen(Observable.just(CustomPrescriptionSaved))
          }
    }
  }

  private fun savePrescription(
      uuid: UUID,
      patientUuid: UUID,
      drugName: String,
      dosage: String?
  ): Completable {
    val currentFacility = currentFacility.get()

    return prescriptionRepository
        .savePrescription(
            uuid = uuid,
            patientUuid = patientUuid,
            name = drugName,
            dosage = dosage.nullIfBlank(),
            rxNormCode = null,
            isProtocolDrug = false,
            facility = currentFacility
        )
  }

  private fun fetchPrescription(): ObservableTransformer<FetchPrescription, CustomPrescriptionEntryEvent> {
    return ObservableTransformer { effects ->
      effects
          // FIXME: The logic to close sheet after a prescription is deleted is dependant on this prescription stream
          //  being reactive. So we have to use the deprecated method here. This should be fixed.
          //  This is being tracked here: https://www.pivotaltracker.com/story/show/172737790
          .flatMap { prescriptionRepository.prescription(it.prescriptionUuid) }
          .map(::CustomPrescriptionFetched)
    }
  }
}
