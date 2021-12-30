package org.simple.clinic.summary.nextappointment

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.scheduler.SchedulersProvider
import javax.inject.Inject

class NextAppointmentEffectHandler @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val patientRepository: PatientRepository,
    private val schedulersProvider: SchedulersProvider
) {

  fun build(): ObservableTransformer<NextAppointmentEffect, NextAppointmentEvent> = RxMobius
      .subtypeEffectHandler<NextAppointmentEffect, NextAppointmentEvent>()
      .addTransformer(LoadAppointment::class.java, loadAppointment())
      .addTransformer(LoadPatientAndAssignedFacility::class.java, loadPatientAndAssignedFacility())
      .build()

  private fun loadPatientAndAssignedFacility(): ObservableTransformer<LoadPatientAndAssignedFacility, NextAppointmentEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .flatMap { patientRepository.patientAndAssignedFacility(it.patientUuid) }
          .map(::PatientAndAssignedFacilityLoaded)
    }
  }

  private fun loadAppointment(): ObservableTransformer<LoadAppointment, NextAppointmentEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .flatMap { appointmentRepository.latestAppointmentForPatient(it.patientUuid) }
          .map { AppointmentLoaded(it) as NextAppointmentEvent }
          .onErrorReturn { AppointmentLoaded(null) }
    }
  }
}
