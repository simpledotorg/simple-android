package org.simple.clinic.teleconsultlog.prescription.patientinfo

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.extractIfPresent
import org.simple.clinic.util.scheduler.SchedulersProvider
import javax.inject.Inject

class TeleconsultPatientInfoEffectHandler @Inject constructor(
    private val patientRepository: PatientRepository,
    private val schedulersProvider: SchedulersProvider
) {

  fun build(): ObservableTransformer<TeleconsultPatientInfoEffect, TeleconsultPatientInfoEvent> {
    return RxMobius
        .subtypeEffectHandler<TeleconsultPatientInfoEffect, TeleconsultPatientInfoEvent>()
        .addTransformer(LoadPatientProfile::class.java, loadPatientProfile())
        .build()
  }

  private fun loadPatientProfile(): ObservableTransformer<LoadPatientProfile, TeleconsultPatientInfoEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { patientRepository.patientProfileImmediate(it.patientUuid) }
          .extractIfPresent()
          .map(::PatientProfileLoaded)
    }
  }
}
