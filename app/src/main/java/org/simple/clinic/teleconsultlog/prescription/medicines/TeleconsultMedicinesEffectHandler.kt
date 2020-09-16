package org.simple.clinic.teleconsultlog.prescription.medicines

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.util.scheduler.SchedulersProvider

class TeleconsultMedicinesEffectHandler @AssistedInject constructor(
    private val prescriptionRepository: PrescriptionRepository,
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val uiActions: TeleconsultMedicinesUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: TeleconsultMedicinesUiActions): TeleconsultMedicinesEffectHandler
  }

  fun build(): ObservableTransformer<TeleconsultMedicinesEffect, TeleconsultMedicinesEvent> {
    return RxMobius
        .subtypeEffectHandler<TeleconsultMedicinesEffect, TeleconsultMedicinesEvent>()
        .addTransformer(LoadPatientMedicines::class.java, loadPatientMedicines())
        .addConsumer(OpenEditMedicines::class.java, { uiActions.openEditMedicines(it.patientUuid) }, schedulersProvider.ui())
        .addConsumer(OpenDrugDurationSheet::class.java, { uiActions.openDrugDurationSheet(it.prescription) }, schedulersProvider.ui())
        .addConsumer(OpenDrugFrequencySheet::class.java, { uiActions.openDrugFrequencySheet(it.prescription) }, schedulersProvider.ui())
        .addConsumer(UpdateDrugDuration::class.java, { updateDrugDuration(it) }, schedulersProvider.io())
        .addConsumer(UpdateDrugFrequency::class.java, { updateDrugFrequency(it) }, schedulersProvider.io())
        .build()
  }

  private fun updateDrugFrequency(updateDrugFrequency: UpdateDrugFrequency) {
    prescriptionRepository.updateDrugFrequency(
        id = updateDrugFrequency.prescribedDrugUuid,
        drugFrequency = updateDrugFrequency.drugFrequency
    )
  }

  private fun updateDrugDuration(updateDrugDuration: UpdateDrugDuration) {
    prescriptionRepository.updateDrugDuration(
        id = updateDrugDuration.prescribedDrugUuid,
        duration = updateDrugDuration.duration
    )
  }

  private fun loadPatientMedicines(): ObservableTransformer<LoadPatientMedicines, TeleconsultMedicinesEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { prescriptionRepository.newestPrescriptionsForPatientImmediate(it.patientUuid) }
          .map(::PatientMedicinesLoaded)
    }
  }
}
