package org.simple.clinic.teleconsultlog.shareprescription

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.scheduler.SchedulersProvider

class TeleconsultSharePrescriptionEffectHandler constructor(
    private val schedulersProvider: SchedulersProvider,
    private val patientRepository: PatientRepository
) {
  fun build(): ObservableTransformer<TeleconsultSharePrescriptionEffect, TeleconsultSharePrescriptionEvent> {
    return RxMobius
        .subtypeEffectHandler<TeleconsultSharePrescriptionEffect, TeleconsultSharePrescriptionEvent>()
        .addTransformer(LoadPatientDetails::class.java, loadPatientDetails())
        .build()
  }

  private fun loadPatientDetails(): ObservableTransformer<LoadPatientDetails, TeleconsultSharePrescriptionEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { patientRepository.patientImmediate(it.patientUuid) }
          .map(::PatientDetailsLoaded)
    }
  }
}
