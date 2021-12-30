package org.simple.clinic.summary.nextappointment

import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.scheduler.SchedulersProvider

class NextAppointmentEffectHandler @AssistedInject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val patientRepository: PatientRepository,
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val viewEffectsConsumer: Consumer<NextAppointmentViewEffect>
) {

  @AssistedFactory
  interface Factory {
    fun create(viewEffectsConsumer: Consumer<NextAppointmentViewEffect>): NextAppointmentEffectHandler
  }

  fun build(): ObservableTransformer<NextAppointmentEffect, NextAppointmentEvent> = RxMobius
      .subtypeEffectHandler<NextAppointmentEffect, NextAppointmentEvent>()
      .addTransformer(LoadAppointment::class.java, loadAppointment())
      .addTransformer(LoadPatientAndAssignedFacility::class.java, loadPatientAndAssignedFacility())
      .addConsumer(NextAppointmentViewEffect::class.java, viewEffectsConsumer::accept)
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
