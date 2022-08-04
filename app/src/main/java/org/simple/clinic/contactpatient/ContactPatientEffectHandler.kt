package org.simple.clinic.contactpatient

import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.Lazy
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import org.simple.clinic.facility.Facility
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.overdue.callresult.CallResultRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.phone.Dialer
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.scheduler.SchedulersProvider
import java.time.LocalDate

class ContactPatientEffectHandler @AssistedInject constructor(
    private val patientRepository: PatientRepository,
    private val appointmentRepository: AppointmentRepository,
    private val createReminderForAppointment: CreateReminderForAppointment,
    private val recordPatientAgreedToVisit: RecordPatientAgreedToVisit,
    private val userClock: UserClock,
    private val schedulers: SchedulersProvider,
    private val currentFacility: Lazy<Facility>,
    private val callResultRepository: CallResultRepository,
    @Assisted private val uiActions: ContactPatientUiActions,
    @Assisted private val viewEffectsConsumer: Consumer<ContactPatientViewEffect>
) {

  @AssistedFactory
  interface Factory {
    fun create(
        uiActions: ContactPatientUiActions,
        viewEffectsConsumer: Consumer<ContactPatientViewEffect>
    ): ContactPatientEffectHandler
  }

  fun build(): ObservableTransformer<ContactPatientEffect, ContactPatientEvent> {
    return RxMobius
        .subtypeEffectHandler<ContactPatientEffect, ContactPatientEvent>()
        .addTransformer(LoadContactPatientProfile::class.java, loadContactPatientProfile(schedulers.io()))
        .addTransformer(LoadLatestOverdueAppointment::class.java, loadLatestOverdueAppointment(schedulers.io()))
        .addAction(CloseScreen::class.java, uiActions::closeSheet, schedulers.ui())
        .addTransformer(MarkPatientAsAgreedToVisit::class.java, markPatientAsAgreedToVisit(schedulers.io()))
        .addConsumer(ShowManualDatePicker::class.java, { uiActions.showManualDatePicker(it.preselectedDate, it.datePickerBounds) }, schedulers.ui())
        .addTransformer(SetReminderForAppointment::class.java, setReminderForAppointment(schedulers.io()))
        .addConsumer(OpenRemoveOverdueAppointmentScreen::class.java, ::openRemoveOverdueAppointmentScreen, schedulers.ui())
        .addTransformer(LoadCurrentFacility::class.java, loadCurrentFacility())
        .addTransformer(LoadCallResultForAppointment::class.java, loadCallResultForAppointment())
        .addConsumer(ContactPatientViewEffect::class.java, viewEffectsConsumer::accept)
        .build()
  }

  private fun loadCallResultForAppointment(): ObservableTransformer<LoadCallResultForAppointment, ContactPatientEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulers.io())
          .map { callResultRepository.callResultForAppointment(it.appointmentId) }
          .map(::CallResultForAppointmentLoaded)
    }
  }

  private fun loadCurrentFacility(): ObservableTransformer<LoadCurrentFacility, ContactPatientEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulers.io())
          .map { currentFacility.get() }
          .map(::CurrentFacilityLoaded)
    }
  }

  private fun openRemoveOverdueAppointmentScreen(effect: OpenRemoveOverdueAppointmentScreen) {
    uiActions.openRemoveOverdueAppointmentScreen(effect.appointment)
  }

  private fun loadContactPatientProfile(
      scheduler: Scheduler
  ): ObservableTransformer<LoadContactPatientProfile, ContactPatientEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(scheduler)
          .map { patientRepository.contactPatientProfileImmediate(it.patientUuid) }
          .map { it.withoutDeletedPhoneNumbers() }
          .map(::PatientProfileLoaded)
    }
  }

  private fun loadLatestOverdueAppointment(
      scheduler: Scheduler
  ): ObservableTransformer<LoadLatestOverdueAppointment, ContactPatientEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(scheduler)
          .map { appointmentRepository.latestOverdueAppointmentForPatient(it.patientUuid, LocalDate.now(userClock)) }
          .map(::OverdueAppointmentLoaded)
    }
  }

  private fun markPatientAsAgreedToVisit(
      scheduler: Scheduler
  ): ObservableTransformer<MarkPatientAsAgreedToVisit, ContactPatientEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(scheduler)
          .doOnNext { recordPatientAgreedToVisit.execute(it.appointment) }
          .map { PatientMarkedAsAgreedToVisit }
    }
  }

  private fun setReminderForAppointment(
      scheduler: Scheduler
  ): ObservableTransformer<SetReminderForAppointment, ContactPatientEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(scheduler)
          .doOnNext { (appointment, reminderDate) -> createReminderForAppointment.execute(appointment, reminderDate) }
          .map { ReminderSetForAppointment }
    }
  }
}
