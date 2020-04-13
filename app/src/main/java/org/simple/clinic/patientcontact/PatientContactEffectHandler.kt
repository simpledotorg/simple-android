package org.simple.clinic.patientcontact

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.phone.Dialer
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.threeten.bp.LocalDate

class PatientContactEffectHandler @AssistedInject constructor(
    private val patientRepository: PatientRepository,
    private val appointmentRepository: AppointmentRepository,
    private val clock: UserClock,
    private val schedulers: SchedulersProvider,
    @Assisted private val uiActions: PatientContactUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: PatientContactUiActions): PatientContactEffectHandler
  }

  fun build(): ObservableTransformer<PatientContactEffect, PatientContactEvent> {
    return RxMobius
        .subtypeEffectHandler<PatientContactEffect, PatientContactEvent>()
        .addTransformer(LoadPatientProfile::class.java, loadPatientProfile(schedulers.io()))
        .addTransformer(LoadLatestOverdueAppointment::class.java, loadLatestOverdueAppointment(schedulers.io()))
        .addConsumer(DirectCallWithAutomaticDialer::class.java, { uiActions.directlyCallPatient(it.patientPhoneNumber, Dialer.Automatic) }, schedulers.ui())
        .addConsumer(DirectCallWithManualDialer::class.java, { uiActions.directlyCallPatient(it.patientPhoneNumber, Dialer.Manual) }, schedulers.ui())
        .addConsumer(MaskedCallWithAutomaticDialer::class.java, { uiActions.maskedCallPatient(it.patientPhoneNumber, it.proxyPhoneNumber, Dialer.Automatic) }, schedulers.ui())
        .addConsumer(MaskedCallWithManualDialer::class.java, { uiActions.maskedCallPatient(it.patientPhoneNumber, it.proxyPhoneNumber, Dialer.Manual) }, schedulers.ui())
        .build()
  }

  private fun loadPatientProfile(
      scheduler: Scheduler
  ): ObservableTransformer<LoadPatientProfile, PatientContactEvent> {
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
