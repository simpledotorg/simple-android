package org.simple.clinic.drugs.selection.custom

import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.Lazy
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.drugs.search.DrugFrequency
import org.simple.clinic.drugs.search.DrugRepository
import org.simple.clinic.drugs.selection.custom.drugfrequency.country.DrugFrequencyLabel
import org.simple.clinic.facility.Facility
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequency
import org.simple.clinic.util.nullIfBlank
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.uuid.UuidGenerator

class CustomDrugEntryEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    private val prescriptionRepository: PrescriptionRepository,
    private val drugRepository: DrugRepository,
    private val currentFacility: Lazy<Facility>,
    private val uuidGenerator: UuidGenerator,
    private val drugFrequencyToLabelMap: Map<DrugFrequency?, DrugFrequencyLabel>,
    @Assisted private val viewEffectsConsumer: Consumer<CustomDrugEntryViewEffect>
) {
  @AssistedFactory
  interface Factory {
    fun create(
        viewEffectsConsumer: Consumer<CustomDrugEntryViewEffect>
    ): CustomDrugEntryEffectHandler
  }

  fun build(): ObservableTransformer<CustomDrugEntryEffect, CustomDrugEntryEvent> {
    return RxMobius
        .subtypeEffectHandler<CustomDrugEntryEffect, CustomDrugEntryEvent>()
        .addTransformer(SaveCustomDrugToPrescription::class.java, saveCustomDrugToPrescription())
        .addTransformer(UpdatePrescription::class.java, updatePrescription())
        .addTransformer(FetchPrescription::class.java, fetchPrescription())
        .addTransformer(FetchDrug::class.java, fetchDrug())
        .addTransformer(RemoveDrugFromPrescription::class.java, removeDrugFromPrescription())
        .addTransformer(LoadDrugFrequencyChoiceItems::class.java, loadDrugFrequencyChoiceItems())
        .addConsumer(CustomDrugEntryViewEffect::class.java, viewEffectsConsumer::accept)
        .build()
  }

  private fun fetchDrug(): ObservableTransformer<FetchDrug, CustomDrugEntryEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { drugRepository.drugImmediate(it.drugUuid) }
          .map(::DrugFetched)
    }
  }

  private fun updatePrescription(): ObservableTransformer<UpdatePrescription, CustomDrugEntryEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .switchMap { effect ->
            prescriptionRepository
                .softDeletePrescription(effect.prescribedDrugUuid)
                .andThen(prescriptionRepository.savePrescription(
                    uuid = uuidGenerator.v4(),
                    patientUuid = effect.patientUuid,
                    name = effect.drugName,
                    dosage = effect.dosage.nullIfBlank(),
                    rxNormCode = effect.rxNormCode,
                    isProtocolDrug = false,
                    frequency = MedicineFrequency.fromDrugFrequency(effect.frequency),
                    facility = currentFacility.get())
                ).andThen(Observable.just(CustomDrugSaved))
          }
    }
  }

  private fun fetchPrescription(): ObservableTransformer<FetchPrescription, CustomDrugEntryEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { prescriptionRepository.prescriptionImmediate(it.prescriptionUuid) }
          .map(::PrescribedDrugFetched)
    }
  }

  private fun removeDrugFromPrescription(): ObservableTransformer<RemoveDrugFromPrescription, CustomDrugEntryEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { it.drugUuid }
          .switchMap {
            prescriptionRepository
                .softDeletePrescription(it)
                .andThen(Observable.just(ExistingDrugRemoved))
          }
    }
  }

  private fun saveCustomDrugToPrescription(): ObservableTransformer<SaveCustomDrugToPrescription, CustomDrugEntryEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .switchMap { savePrescription ->
            prescriptionRepository
                .savePrescription(
                    uuid = uuidGenerator.v4(),
                    patientUuid = savePrescription.patientUuid,
                    name = savePrescription.drugName,
                    dosage = savePrescription.dosage.nullIfBlank(),
                    rxNormCode = savePrescription.rxNormCode,
                    isProtocolDrug = false,
                    frequency = MedicineFrequency.fromDrugFrequency(savePrescription.frequency),
                    facility = currentFacility.get()
                ).andThen(Observable.just(CustomDrugSaved))
          }
    }
  }

  private fun loadDrugFrequencyChoiceItems(): ObservableTransformer<LoadDrugFrequencyChoiceItems, CustomDrugEntryEvent> {
    return ObservableTransformer { effects ->
      effects
          .map { drugFrequencyToLabelMap }
          .map(::DrugFrequencyChoiceItemsLoaded)
    }
  }
}
