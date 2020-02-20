package org.simple.clinic.summary

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.zipWith
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.overdue.Appointment
import org.simple.clinic.overdue.AppointmentCancelReason
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.summary.AppointmentSheetOpenedFrom.BACK_CLICK
import org.simple.clinic.summary.AppointmentSheetOpenedFrom.DONE_CLICK
import org.simple.clinic.summary.OpenIntention.LinkIdWithPatient
import org.simple.clinic.summary.OpenIntention.ViewExistingPatient
import org.simple.clinic.summary.OpenIntention.ViewNewPatient
import org.simple.clinic.summary.addphone.MissingPhoneReminderRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.util.scheduler.SchedulersProvider
import java.util.UUID

class PatientSummaryEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    private val patientRepository: PatientRepository,
    private val bloodPressureRepository: BloodPressureRepository,
    private val appointmentRepository: AppointmentRepository,
    private val missingPhoneReminderRepository: MissingPhoneReminderRepository,
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository,
    @Assisted private val uiActions: PatientSummaryUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: PatientSummaryUiActions): PatientSummaryEffectHandler
  }

  fun build(): ObservableTransformer<PatientSummaryEffect, PatientSummaryEvent> {
    return RxMobius
        .subtypeEffectHandler<PatientSummaryEffect, PatientSummaryEvent>()
        .addTransformer(LoadPatientSummaryProfile::class.java, loadPatientSummaryProfile(schedulersProvider.io()))
        .addTransformer(HandleBackClick::class.java, handleBackClick(
            backgroundWorkScheduler = schedulersProvider.io(),
            uiWorkScheduler = schedulersProvider.ui()
        ))
        .addTransformer(HandleDoneClick::class.java, handleDoneClick(
            backgroundWorkScheduler = schedulersProvider.io(),
            uiWorkScheduler = schedulersProvider.ui()
        ))
        .addTransformer(LoadCurrentFacility::class.java, loadCurrentFacility(schedulersProvider.io()))
        .addConsumer(HandleEditClick::class.java, { uiActions.showEditPatientScreen(it.patientSummaryProfile) }, schedulersProvider.ui())
        .addAction(HandleLinkIdCancelled::class.java, { uiActions.goToPreviousScreen() }, schedulersProvider.ui())
        .addAction(GoBackToPreviousScreen::class.java, { uiActions.goToPreviousScreen() }, schedulersProvider.ui())
        .addAction(GoToHomeScreen::class.java, { uiActions.goToHomeScreen() }, schedulersProvider.ui())
        .addTransformer(CheckForInvalidPhone::class.java, checkForInvalidPhone(schedulersProvider.io(), schedulersProvider.ui()))
        .addTransformer(FetchHasShownMissingPhoneReminder::class.java, fetchHasShownMissingPhoneReminder(schedulersProvider.io()))
        .addTransformer(MarkReminderAsShown::class.java, markReminderAsShown(schedulersProvider.io()))
        .addConsumer(ShowAddPhonePopup::class.java, { uiActions.showAddPhoneDialog(it.patientUuid) }, schedulersProvider.ui())
        .addTransformer(ShowLinkIdWithPatientView::class.java, showLinkIdWithPatientView(schedulersProvider.ui()))
        .addAction(HideLinkIdWithPatientView::class.java, { uiActions.hideLinkIdWithPatientView() }, schedulersProvider.ui())
        .build()
  }

  // TODO(vs): 2020-01-15 Revisit after Mobius migration
  private fun loadPatientSummaryProfile(scheduler: Scheduler): ObservableTransformer<LoadPatientSummaryProfile, PatientSummaryEvent> {
    return ObservableTransformer { effects ->
      effects.flatMap { fetchPatientSummaryProfile ->
        val patientUuid = fetchPatientSummaryProfile.patientUuid

        val sharedPatients = patientRepository.patient(patientUuid)
            .subscribeOn(scheduler)
            .map {
              // We do not expect the patient to get deleted while this screen is already open.
              (it as Just).value
            }
            .replay(1)
            .refCount()

        val addresses = sharedPatients
            .flatMap { patient -> patientRepository.address(patient.addressUuid).subscribeOn(scheduler) }
            .map { (it as Just).value }

        val latestPhoneNumberStream = patientRepository.phoneNumber(patientUuid).subscribeOn(scheduler)
        val latestBpPassportStream = patientRepository.bpPassportForPatient(patientUuid).subscribeOn(scheduler)
        val bangladeshNationalIdStream = patientRepository.bangladeshNationalIdForPatient(patientUuid).subscribeOn(scheduler)

        Observables
            .combineLatest(sharedPatients, addresses, latestPhoneNumberStream, latestBpPassportStream, bangladeshNationalIdStream) { patient, address, phoneNumber, bpPassport, bangladeshNationalId ->
              PatientSummaryProfile(patient, address, phoneNumber.toNullable(), bpPassport.toNullable(), bangladeshNationalId.toNullable())
            }
            .take(1)
            .map(::PatientSummaryProfileLoaded)
            .cast<PatientSummaryEvent>()
      }
    }
  }

  // TODO(vs): 2020-01-15 Revisit after Mobius migration
  private fun handleBackClick(
      backgroundWorkScheduler: Scheduler,
      uiWorkScheduler: Scheduler
  ): ObservableTransformer<HandleBackClick, PatientSummaryEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(backgroundWorkScheduler)
          .map { handleBackClick ->
            val (patientUuid, screenCreatedTime, openIntention) = handleBackClick

            val hasPatientDataChangedSinceScreenCreated = patientRepository.hasPatientDataChangedSince(patientUuid, screenCreatedTime)
            val noBloodPressuresRecordedForPatient = doesNotHaveBloodPressures(patientUuid)

            val shouldShowScheduleAppointmentSheet = if (noBloodPressuresRecordedForPatient) false else hasPatientDataChangedSinceScreenCreated

            Triple(shouldShowScheduleAppointmentSheet, patientUuid, openIntention)
          }
          .observeOn(uiWorkScheduler)
          .doOnNext { (showScheduleAppointmentSheet, patientUuid, openIntention) ->
            if (showScheduleAppointmentSheet) {
              uiActions.showScheduleAppointmentSheet(patientUuid, BACK_CLICK)
            } else {
              when (openIntention) {
                ViewExistingPatient -> uiActions.goToPreviousScreen()
                is LinkIdWithPatient, ViewNewPatient -> uiActions.goToHomeScreen()
              }
            }
          }
          .flatMap { Observable.empty<PatientSummaryEvent>() }
    }
  }

  // TODO(vs): 2020-01-16 Revisit after Mobius migration
  private fun handleDoneClick(
      backgroundWorkScheduler: Scheduler,
      uiWorkScheduler: Scheduler
  ): ObservableTransformer<HandleDoneClick, PatientSummaryEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(backgroundWorkScheduler)
          .map { handleDoneClick ->
            val patientUuid = handleDoneClick.patientUuid

            val shouldShowScheduleAppointmentSheet = !doesNotHaveBloodPressures(patientUuid)

            shouldShowScheduleAppointmentSheet to patientUuid
          }
          .observeOn(uiWorkScheduler)
          .doOnNext { (shouldShowScheduleAppointmentSheet, patientUuid) ->
            if (shouldShowScheduleAppointmentSheet) {
              uiActions.showScheduleAppointmentSheet(patientUuid, DONE_CLICK)
            } else {
              uiActions.goToHomeScreen()
            }
          }
          .flatMap { Observable.empty<PatientSummaryEvent>() }
    }
  }

  // TODO(vs): 2020-02-19 Revisit after Mobius migration
  private fun checkForInvalidPhone(
      backgroundWorkScheduler: Scheduler,
      uiWorkScheduler: Scheduler
  ): ObservableTransformer<CheckForInvalidPhone, PatientSummaryEvent> {
    return ObservableTransformer { effects ->
      effects
          .flatMap { checkForInvalidPhone ->
            hasInvalidPhone(checkForInvalidPhone.patientUuid)
                .subscribeOn(backgroundWorkScheduler)
                .take(1)
                .observeOn(uiWorkScheduler)
                .doOnNext { isPhoneInvalid ->
                  if (isPhoneInvalid) {
                    uiActions.showUpdatePhoneDialog(checkForInvalidPhone.patientUuid)
                  }
                }
                .flatMapSingle { Single.just(CompletedCheckForInvalidPhone) }
          }
    }
  }

  // TODO(vs): 2020-02-19 Revisit after Mobius migration
  private fun fetchHasShownMissingPhoneReminder(
      scheduler: Scheduler
  ): ObservableTransformer<FetchHasShownMissingPhoneReminder, PatientSummaryEvent> {
    return ObservableTransformer { effects ->
      effects
          .flatMap { effect ->
            isMissingPhoneAndHasShownReminder(effect.patientUuid)
                .subscribeOn(scheduler)
                .take(1)
                .map(::FetchedHasShownMissingReminder)
          }
    }
  }

  // TODO(vs): 2020-02-19 Revisit after Mobius migration
  private fun markReminderAsShown(
      scheduler: Scheduler
  ): ObservableTransformer<MarkReminderAsShown, PatientSummaryEvent> {
    return ObservableTransformer { effects ->
      effects
          .flatMap { effect ->
            missingPhoneReminderRepository
                .markReminderAsShownFor(effect.patientUuid)
                .subscribeOn(scheduler)
                .andThen(Observable.empty<PatientSummaryEvent>())
          }
    }
  }

  private fun showLinkIdWithPatientView(
      scheduler: Scheduler
  ): ObservableTransformer<ShowLinkIdWithPatientView, PatientSummaryEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(scheduler)
          .doOnNext { uiActions.showLinkIdWithPatientView(it.patientUuid, it.identifier) }
          .map { LinkIdWithPatientSheetShown }
    }
  }

  private fun loadCurrentFacility(scheduler: Scheduler): ObservableTransformer<LoadCurrentFacility, PatientSummaryEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(scheduler)
          .flatMap {
            val user = userSession.loggedInUserImmediate()
            requireNotNull(user)

            facilityRepository
                .currentFacility(user)
                .take(1)
          }
          .map(::CurrentFacilityLoaded)
    }
  }

  private fun doesNotHaveBloodPressures(patientUuid: UUID): Boolean {
    return bloodPressureRepository.bloodPressureCountImmediate(patientUuid) == 0
  }

  // TODO(vs): 2020-02-18 Revisit after Mobius migration
  private fun hasInvalidPhone(patientUuid: UUID): Observable<Boolean> {
    return patientRepository.phoneNumber(patientUuid)
        .filterAndUnwrapJust()
        .zipWith(lastCancelledAppointmentWithInvalidPhone(patientUuid))
        .map { (number, appointment) -> appointment.updatedAt > number.updatedAt }
  }

  // TODO(vs): 2020-02-18 Revisit after Mobius migration
  private fun lastCancelledAppointmentWithInvalidPhone(patientUuid: UUID): Observable<Appointment> {
    return appointmentRepository
        .lastCreatedAppointmentForPatient(patientUuid)
        .filterAndUnwrapJust()
        .filter { it.status == Appointment.Status.Cancelled && it.cancelReason == AppointmentCancelReason.InvalidPhoneNumber }
  }

  private fun isMissingPhoneAndHasShownReminder(patientUuid: UUID): Observable<Boolean> {
    return patientRepository
        .phoneNumber(patientUuid)
        .zipWith(hasShownReminderForMissingPhone(patientUuid))
        .map { (number, reminderShown) -> number is None && reminderShown }
  }

  private fun hasShownReminderForMissingPhone(patientUuid: UUID): Observable<Boolean> {
    return missingPhoneReminderRepository
        .hasShownReminderFor(patientUuid)
        .toObservable()
  }
}
