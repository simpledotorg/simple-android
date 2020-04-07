package org.simple.clinic.patientcontact

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.util.scheduler.SchedulersProvider

class PatientContactEffectHandler(
    private val patientRepository: PatientRepository,
    private val schedulers: SchedulersProvider
) {

  fun build(): ObservableTransformer<PatientContactEffect, PatientContactEvent> {
    return RxMobius
        .subtypeEffectHandler<PatientContactEffect, PatientContactEvent>()
        .addTransformer(LoadPatient::class.java, loadPatientProfile(schedulers.io()))
        .build()
  }

  private fun loadPatientProfile(
      scheduler: Scheduler
  ): ObservableTransformer<LoadPatient, PatientContactEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(scheduler)
          .map { patientRepository.patientProfileImmediate(it.patientUuid) }
          .filterAndUnwrapJust()
          .map { it.withoutDeletedPhoneNumbers().withoutDeletedBusinessIds() }
          .map(::PatientProfileLoaded)
    }
  }
}
