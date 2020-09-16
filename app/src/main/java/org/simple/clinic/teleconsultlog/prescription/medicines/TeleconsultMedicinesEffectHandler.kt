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
