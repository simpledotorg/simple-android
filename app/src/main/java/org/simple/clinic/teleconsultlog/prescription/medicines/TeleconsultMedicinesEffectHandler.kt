package org.simple.clinic.teleconsultlog.prescription.medicines

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.util.scheduler.SchedulersProvider
import javax.inject.Inject

class TeleconsultMedicinesEffectHandler @Inject constructor(
    private val prescriptionRepository: PrescriptionRepository,
    private val schedulersProvider: SchedulersProvider
) {

  fun build(): ObservableTransformer<TeleconsultMedicinesEffect, TeleconsultMedicinesEvent> {
    return RxMobius
        .subtypeEffectHandler<TeleconsultMedicinesEffect, TeleconsultMedicinesEvent>()
        .addTransformer(LoadPatientMedicines::class.java, loadPatientMedicines())
        .build()
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
