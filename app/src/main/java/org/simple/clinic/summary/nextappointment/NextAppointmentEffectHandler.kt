package org.simple.clinic.summary.nextappointment

import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.util.scheduler.SchedulersProvider

class NextAppointmentEffectHandler @AssistedInject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val uiActions: NextAppointmentUiActions
) {

  @AssistedFactory
  interface Factory {
    fun create(uiActions: NextAppointmentUiActions): NextAppointmentEffectHandler
  }

  fun build(): ObservableTransformer<NextAppointmentEffect, NextAppointmentEvent> = RxMobius
      .subtypeEffectHandler<NextAppointmentEffect, NextAppointmentEvent>()
      .addTransformer(LoadNextAppointmentPatientProfile::class.java, loadAppointment())
      .addConsumer(OpenScheduleAppointmentSheet::class.java, ::openScheduleAppointmentSheet, schedulersProvider.ui())
      .build()

  private fun openScheduleAppointmentSheet(effect: OpenScheduleAppointmentSheet) {
    uiActions.openScheduleAppointmentSheet(effect.patientUuid)
  }

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
