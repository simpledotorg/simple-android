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
      .addTransformer(LoadNextAppointmentPatientProfile::class.java, loadAppointment())
      .build()

  private fun loadAppointment(): ObservableTransformer<LoadNextAppointmentPatientProfile, NextAppointmentEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .flatMap { appointmentRepository.nextAppointmentPatientProfile(it.patientUuid) }
          .map { NextAppointmentPatientProfileLoaded(it) as NextAppointmentEvent }
          .onErrorReturn { NextAppointmentPatientProfileLoaded(null) }
    }
  }
}
