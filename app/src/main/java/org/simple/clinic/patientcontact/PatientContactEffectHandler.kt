package org.simple.clinic.patientcontact

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.threeten.bp.LocalDate

class PatientContactEffectHandler(
    private val patientRepository: PatientRepository,
    private val appointmentRepository: AppointmentRepository,
    private val clock: UserClock,
    private val schedulers: SchedulersProvider
) {

  fun build(): ObservableTransformer<PatientContactEffect, PatientContactEvent> {
    return RxMobius
        .subtypeEffectHandler<PatientContactEffect, PatientContactEvent>()
        .addTransformer(LoadPatient::class.java, loadPatientProfile(schedulers.io()))
        .addTransformer(LoadLatestOverdueAppointment::class.java, loadLatestOverdueAppointment(schedulers.io()))
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

  private fun loadLatestOverdueAppointment(
      scheduler: Scheduler
  ): ObservableTransformer<LoadLatestOverdueAppointment, PatientContactEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(scheduler)
          .map { appointmentRepository.latestOverdueAppointmentForPatient(it.patientUuid, LocalDate.now(clock)) }
          .map(::OverdueAppointmentLoaded)
    }
  }
}
