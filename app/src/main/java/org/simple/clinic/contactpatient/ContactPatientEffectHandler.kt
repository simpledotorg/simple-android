package org.simple.clinic.contactpatient

import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import org.simple.clinic.overdue.AppointmentCancelReason
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.phone.Dialer
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.util.scheduler.SchedulersProvider
import java.time.LocalDate

class ContactPatientEffectHandler @AssistedInject constructor(
    private val patientRepository: PatientRepository,
    private val appointmentRepository: AppointmentRepository,
    private val clock: UserClock,
    private val schedulers: SchedulersProvider,
    @Assisted private val uiActions: ContactPatientUiActions
) {

  @AssistedFactory
  interface Factory {
    fun create(uiActions: ContactPatientUiActions): ContactPatientEffectHandler
  }

  fun build(): ObservableTransformer<ContactPatientEffect, ContactPatientEvent> {
    return RxMobius
        .subtypeEffectHandler<ContactPatientEffect, ContactPatientEvent>()
        .addTransformer(LoadPatientProfile::class.java, loadPatientProfile(schedulers.io()))
        .addTransformer(LoadLatestOverdueAppointment::class.java, loadLatestOverdueAppointment(schedulers.io()))
        .addConsumer(DirectCallWithAutomaticDialer::class.java, { uiActions.directlyCallPatient(it.patientPhoneNumber, Dialer.Automatic) }, schedulers.ui())
        .addConsumer(DirectCallWithManualDialer::class.java, { uiActions.directlyCallPatient(it.patientPhoneNumber, Dialer.Manual) }, schedulers.ui())
        .addConsumer(MaskedCallWithAutomaticDialer::class.java, { uiActions.maskedCallPatient(it.patientPhoneNumber, it.proxyPhoneNumber, Dialer.Automatic) }, schedulers.ui())
        .addConsumer(MaskedCallWithManualDialer::class.java, { uiActions.maskedCallPatient(it.patientPhoneNumber, it.proxyPhoneNumber, Dialer.Manual) }, schedulers.ui())
        .addAction(CloseScreen::class.java, uiActions::closeSheet, schedulers.ui())
        .addTransformer(MarkPatientAsAgreedToVisit::class.java, markPatientAsAgreedToVisit(schedulers.io()))
        .addConsumer(ShowManualDatePicker::class.java, { uiActions.showManualDatePicker(it.preselectedDate, it.datePickerBounds) }, schedulers.ui())
        .addTransformer(SetReminderForAppointment::class.java, setReminderForAppointment(schedulers.io()))
        .addTransformer(MarkPatientAsVisited::class.java, markPatientAsVisited(schedulers.io()))
        .addTransformer(MarkPatientAsDead::class.java, markPatientAsDead(schedulers.io()))
        .addTransformer(CancelAppointment::class.java, cancelAppointment(schedulers.io()))
        .build()
  }

  private fun loadPatientProfile(
      scheduler: Scheduler
  ): ObservableTransformer<LoadPatientProfile, ContactPatientEvent> {
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
  ): ObservableTransformer<LoadLatestOverdueAppointment, ContactPatientEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(scheduler)
          .map { appointmentRepository.latestOverdueAppointmentForPatient(it.patientUuid, LocalDate.now(clock)) }
          .map(::OverdueAppointmentLoaded)
    }
  }

  private fun markPatientAsAgreedToVisit(
      scheduler: Scheduler
  ): ObservableTransformer<MarkPatientAsAgreedToVisit, ContactPatientEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(scheduler)
          .doOnNext { appointmentRepository.markAsAgreedToVisit(it.appointmentUuid, clock) }
          .map { PatientMarkedAsAgreedToVisit }
    }
  }

  private fun setReminderForAppointment(
      scheduler: Scheduler
  ): ObservableTransformer<SetReminderForAppointment, ContactPatientEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(scheduler)
          .doOnNext { (appointmentUuid, reminderDate) -> appointmentRepository.createReminder(appointmentUuid, reminderDate) }
          .map { ReminderSetForAppointment }
    }
  }

  private fun markPatientAsVisited(
      scheduler: Scheduler
  ): ObservableTransformer<MarkPatientAsVisited, ContactPatientEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(scheduler)
          .doOnNext { appointmentRepository.markAsAlreadyVisited(it.appointmentUuid) }
          .map { PatientMarkedAsVisited }
    }
  }

  private fun markPatientAsDead(
      scheduler: Scheduler
  ): ObservableTransformer<MarkPatientAsDead, ContactPatientEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(scheduler)
          .doOnNext { patientRepository.updatePatientStatusToDead(it.patientUuid) }
          .doOnNext { appointmentRepository.cancelWithReason(it.appointmentUuid, AppointmentCancelReason.Dead) }
          .map { PatientMarkedAsDead }
    }
  }

  private fun cancelAppointment(
      scheduler: Scheduler
  ): ObservableTransformer<CancelAppointment, ContactPatientEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(scheduler)
          .doOnNext { appointmentRepository.cancelWithReason(it.appointmentUuid, it.reason) }
          .map { AppointmentMarkedAsCancelled }
    }
  }
}
