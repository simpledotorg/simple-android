package org.simple.clinic.drugs.selection.custom

import com.spotify.mobius.rx2.RxMobius
import dagger.Lazy
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.drugs.search.DrugFrequency
import org.simple.clinic.facility.Facility
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequency
import org.simple.clinic.util.nullIfBlank
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.uuid.UuidGenerator
import java.util.UUID

class CustomDrugEntryEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    private val prescriptionRepository: PrescriptionRepository,
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
        .addTransformer(SaveCustomDrugToPrescription::class.java, saveCustomDrugToPrescription())
        .build()
  }

  private fun saveCustomDrugToPrescription(): ObservableTransformer<SaveCustomDrugToPrescription, CustomDrugEntryEvent>? {
    return ObservableTransformer { effects ->
      effects
          .flatMap { savePrescription ->
            savePrescription(
                patientUuid = savePrescription.patientUuid,
                drugName = savePrescription.drugName,
                dosage = savePrescription.dosage,
                rxNormCode = savePrescription.rxNormCode,
                frequency = savePrescription.frequency
            ).andThen(Observable.just(CustomDrugSaved))
          }
    }
  }

  private fun savePrescription(
      patientUuid: UUID,
      drugName: String,
      dosage: String?,
      rxNormCode: String?,
      frequency: DrugFrequency?
  ): Completable {
    val currentFacility = currentFacility.get()

    return prescriptionRepository
        .savePrescription(
            uuid = uuidGenerator.v4(),
            patientUuid = patientUuid,
            name = drugName,
            dosage = dosage.nullIfBlank(),
            rxNormCode = rxNormCode,
            isProtocolDrug = false,
            frequency = MedicineFrequency.fromDrugFrequencyToMedicineFrequency(frequency),
            facility = currentFacility
        )
  }
}
