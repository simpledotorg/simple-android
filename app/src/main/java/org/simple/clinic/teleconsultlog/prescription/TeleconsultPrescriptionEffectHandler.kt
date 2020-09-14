package org.simple.clinic.teleconsultlog.prescription

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.scheduler.SchedulersProvider
import javax.inject.Inject

class TeleconsultPrescriptionEffectHandler @Inject constructor(
    private val patientRepository: PatientRepository,
    private val schedulersProvider: SchedulersProvider
) {

  fun build(): ObservableTransformer<TeleconsultPrescriptionEffect, TeleconsultPrescriptionEvent> {
    return RxMobius
        .subtypeEffectHandler<TeleconsultPrescriptionEffect, TeleconsultPrescriptionEvent>()
        .addTransformer(LoadPatientDetails::class.java, loadPatientDetails())
        .build()
  }

  private fun loadPatientDetails(): ObservableTransformer<LoadPatientDetails, TeleconsultPrescriptionEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { patientRepository.patientImmediate(it.patientUuid) }
          .map(::PatientDetailsLoaded)
    }
  }
}
