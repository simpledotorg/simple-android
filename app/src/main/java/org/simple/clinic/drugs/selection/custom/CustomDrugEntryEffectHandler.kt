package org.simple.clinic.drugs.selection.custom

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
    @Assisted private val uiActions: CustomDrugEntrySheetUiActions
) {
  @AssistedFactory
  interface Factory {
    fun create(
        uiActions: CustomDrugEntrySheetUiActions
    ): CustomDrugEntryEffectHandler
  }

  fun build(): ObservableTransformer<CustomDrugEntryEffect, CustomDrugEntryEvent> {
    return RxMobius
        .subtypeEffectHandler<CustomDrugEntryEffect, CustomDrugEntryEvent>()
        .addConsumer(ShowEditFrequencyDialog::class.java, { uiActions.showEditFrequencyDialog(it.frequency) }, schedulersProvider.ui())
        .addConsumer(SetDrugFrequency::class.java, { uiActions.setDrugFrequency(it.frequency) }, schedulersProvider.ui())
        .addConsumer(SetDrugDosage::class.java, { uiActions.setDrugDosage(it.dosage) }, schedulersProvider.ui())
        .addConsumer(SetSheetTitle::class.java, ::setSheetTitle, schedulersProvider.ui())
        .addTransformer(SaveCustomDrugToPrescription::class.java, saveCustomDrugToPrescription())
        .addTransformer(UpdatePrescription::class.java, updatePrescription())
        .addAction(CloseSheetAndGoToEditMedicineScreen::class.java, uiActions::closeSheetAndGoToEditMedicineScreen, schedulersProvider.ui())
        .addTransformer(FetchPrescription::class.java, fetchPrescription())
        .addTransformer(FetchDrug::class.java, fetchDrug())
        .addTransformer(RemoveDrugFromPrescription::class.java, removeDrugFromPrescription())
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

  private fun setSheetTitle(setSheetTitle: SetSheetTitle) {
    val sheetTitle = constructSheetTitle(setSheetTitle.name, setSheetTitle.dosage.nullIfBlank(), setSheetTitle.frequency)

    uiActions.setSheetTitle(sheetTitle)
  }

  private fun constructSheetTitle(
      name: String?,
      dosage: String?,
      frequency: DrugFrequency?
  ): String {
    return listOfNotNull(name, dosage, frequency?.toString()).joinToString()
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
                    dosage = effect.dosage,
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
}
