package org.simple.clinic.removeoverdueappointment

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.util.scheduler.SchedulersProvider

class RemoveOverdueEffectHandler(
    private val appointmentRepository: AppointmentRepository,
    private val schedulersProvider: SchedulersProvider
) {

  fun build(): ObservableTransformer<RemoveOverdueEffect, RemoveOverdueEvent> = RxMobius
      .subtypeEffectHandler<RemoveOverdueEffect, RemoveOverdueEvent>()
      .addTransformer(MarkPatientAsVisited::class.java, markPatientAsVisited())
      .build()

  private fun markPatientAsVisited(): ObservableTransformer<MarkPatientAsVisited, RemoveOverdueEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .doOnNext { appointmentRepository.markAsAlreadyVisited(it.appointmentUuid) }
          .map { PatientMarkedAsVisited }
    }
  }
}
