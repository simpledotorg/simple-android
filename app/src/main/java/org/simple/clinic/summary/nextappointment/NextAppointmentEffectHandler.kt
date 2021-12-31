package org.simple.clinic.summary.nextappointment

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.util.scheduler.SchedulersProvider
import javax.inject.Inject

class NextAppointmentEffectHandler @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val schedulersProvider: SchedulersProvider
) {

  fun build(): ObservableTransformer<NextAppointmentEffect, NextAppointmentEvent> = RxMobius
      .subtypeEffectHandler<NextAppointmentEffect, NextAppointmentEvent>()
      .addTransformer(LoadNextAppointmentPatientProfile::class.java, loadNextAppointmentPatientProfile())
      .build()

  private fun loadNextAppointmentPatientProfile(): ObservableTransformer<LoadNextAppointmentPatientProfile, NextAppointmentEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map {
            val patientProfile = appointmentRepository.nextAppointmentPatientProfile(it.patientUuid)
            NextAppointmentPatientProfileLoaded(patientProfile)
          }
    }
  }
}
