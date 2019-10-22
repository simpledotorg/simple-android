package org.simple.clinic.bp.entry

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.rxkotlin.zipWith
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.bp.entry.BpValidator.Validation.ErrorDiastolicEmpty
import org.simple.clinic.bp.entry.BpValidator.Validation.ErrorDiastolicTooHigh
import org.simple.clinic.bp.entry.BpValidator.Validation.ErrorDiastolicTooLow
import org.simple.clinic.bp.entry.BpValidator.Validation.ErrorSystolicEmpty
import org.simple.clinic.bp.entry.BpValidator.Validation.ErrorSystolicLessThanDiastolic
import org.simple.clinic.bp.entry.BpValidator.Validation.ErrorSystolicTooHigh
import org.simple.clinic.bp.entry.BpValidator.Validation.ErrorSystolicTooLow
import org.simple.clinic.bp.entry.BpValidator.Validation.Success
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UserInputDatePaddingCharacter
import org.simple.clinic.util.exhaustive
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.util.toLocalDateAtZone
import org.simple.clinic.util.toUtcInstant
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Invalid.DateIsInFuture
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Invalid.InvalidPattern
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Valid
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate

object BloodPressureEntryEffectHandler {
  fun create(
      ui: BloodPressureEntryUi,
      userSession: UserSession,
      facilityRepository: FacilityRepository,
      patientRepository: PatientRepository,
      bloodPressureRepository: BloodPressureRepository,
      appointmentsRepository: AppointmentRepository,
      userClock: UserClock,
      inputDatePaddingCharacter: UserInputDatePaddingCharacter,
      schedulersProvider: SchedulersProvider
  ): ObservableTransformer<BloodPressureEntryEffect, BloodPressureEntryEvent> {
    val createNewBpEntryTransformer = createNewBpEntryTransformer(
        userSession,
        facilityRepository,
        patientRepository,
        bloodPressureRepository,
        appointmentsRepository,
        userClock,
        schedulersProvider.io()
    )

    val updateBpEntryTransformer = updateBpEntryTransformer(
        userSession,
        facilityRepository,
        patientRepository,
        bloodPressureRepository,
        userClock
    )

    return RxMobius
        .subtypeEffectHandler<BloodPressureEntryEffect, BloodPressureEntryEvent>()
        .addConsumer(PrefillDate::class.java, { prefillDate(ui, it.date, userClock, inputDatePaddingCharacter) }, schedulersProvider.ui())
        .addAction(HideBpErrorMessage::class.java, ui::hideBpErrorMessage, schedulersProvider.ui())
        .addAction(ChangeFocusToDiastolic::class.java, ui::changeFocusToDiastolic, schedulersProvider.ui())
        .addAction(ChangeFocusToSystolic::class.java, ui::changeFocusToSystolic, schedulersProvider.ui())
        .addConsumer(SetSystolic::class.java, { ui.setSystolic(it.systolic) }, schedulersProvider.ui())
        .addTransformer(FetchBloodPressureMeasurement::class.java, fetchBloodPressureMeasurement(bloodPressureRepository, schedulersProvider.io()))
        .addConsumer(SetDiastolic::class.java, { ui.setDiastolic(it.diastolic) }, schedulersProvider.ui())
        .addConsumer(ShowConfirmRemoveBloodPressureDialog::class.java, { ui.showConfirmRemoveBloodPressureDialog(it.bpUuid) }, schedulersProvider.ui())
        .addAction(Dismiss::class.java, ui::dismiss, schedulersProvider.ui())
        .addAction(HideDateErrorMessage::class.java, ui::hideDateErrorMessage, schedulersProvider.ui())
        .addConsumer(ShowBpValidationError::class.java, { showBpValidationError(ui, it) }, schedulersProvider.ui())
        .addAction(ShowDateEntryScreen::class.java, ui::showDateEntryScreen, schedulersProvider.ui())
        .addConsumer(ShowBpEntryScreen::class.java, { showBpEntryScreen(ui, it.date) }, schedulersProvider.ui())
        .addConsumer(ShowDateValidationError::class.java, { showDateValidationError(ui, it.result) }, schedulersProvider.ui())
        .addTransformer(CreateNewBpEntry::class.java, createNewBpEntryTransformer)
        .addAction(SetBpSavedResultAndFinish::class.java, ui::setBpSavedResultAndFinish, schedulersProvider.ui())
        .addTransformer(UpdateBpEntry::class.java, updateBpEntryTransformer)
        .build()
  }

  private fun prefillDate(
      ui: BloodPressureEntryUi,
      instant: Instant?,
      userClock: UserClock,
      paddingCharacter: UserInputDatePaddingCharacter
  ) {
    val prefillInstant = instant ?: Instant.now(userClock)
    val date = prefillInstant.toLocalDateAtZone(userClock.zone)
    val dayString = date.dayOfMonth.toString().padStart(length = 2, padChar = paddingCharacter.value)
    val monthString = date.monthValue.toString().padStart(length = 2, padChar = paddingCharacter.value)
    val yearString = date.year.toString().substring(startIndex = 2, endIndex = 4)
    ui.setDateOnInputFields(dayString, monthString, yearString)
    ui.showDateOnDateButton(date)
  }

  private fun fetchBloodPressureMeasurement(
      bloodPressureRepository: BloodPressureRepository,
      scheduler: Scheduler
  ): ObservableTransformer<FetchBloodPressureMeasurement, BloodPressureEntryEvent> {
    return ObservableTransformer { bloodPressureMeasurements ->
      bloodPressureMeasurements
          .flatMap { bloodPressureRepository.measurement(it.bpUuid).subscribeOn(scheduler).take(1) }
          .map(::BloodPressureMeasurementFetched)
    }
  }

  private fun showBpValidationError(
      ui: BloodPressureEntryUi,
      validationError: ShowBpValidationError
  ) {
    when (validationError.result) {
      is ErrorSystolicLessThanDiastolic -> ui.showSystolicLessThanDiastolicError()
      is ErrorSystolicTooHigh -> ui.showSystolicHighError()
      is ErrorSystolicTooLow -> ui.showSystolicLowError()
      is ErrorDiastolicTooHigh -> ui.showDiastolicHighError()
      is ErrorDiastolicTooLow -> ui.showDiastolicLowError()
      is ErrorSystolicEmpty -> ui.showSystolicEmptyError()
      is ErrorDiastolicEmpty -> ui.showDiastolicEmptyError()
      is Success -> { /* Nothing to do here. */
      }
    }.exhaustive()
  }

  private fun showBpEntryScreen(ui: BloodPressureEntryUi, localDate: LocalDate) {
    with(ui) {
      showBpEntryScreen()
      showDateOnDateButton(localDate)
    }
  }

  private fun showDateValidationError(
      ui: BloodPressureEntryUi,
      result: Result
  ) {
    when (result) {
      is InvalidPattern -> ui.showInvalidDateError()
      is DateIsInFuture -> ui.showDateIsInFutureError()
      is Valid -> throw IllegalStateException("Date validation error cannot be $result")
    }.exhaustive()
  }

  private fun createNewBpEntryTransformer(
      userSession: UserSession,
      facilityRepository: FacilityRepository,
      patientRepository: PatientRepository,
      bloodPressureRepository: BloodPressureRepository,
      appointmentsRepository: AppointmentRepository,
      userClock: UserClock,
      scheduler: Scheduler
  ): ObservableTransformer<CreateNewBpEntry, BloodPressureEntryEvent> {
    return ObservableTransformer { createNewBpEntries ->
      createNewBpEntries
          .subscribeOn(scheduler)
          .flatMap { createNewBpEntry -> getUserAndCurrentFacility(userSession, facilityRepository).map { createNewBpEntry to it } }
          .flatMapSingle { (createNewBpEntry, userFacilityPair) ->
            createNewBpEntry(userFacilityPair, createNewBpEntry, bloodPressureRepository, userClock)
                .flatMap { bloodPressureMeasurement ->
                  appointmentsRepository
                      .markAppointmentsCreatedBeforeTodayAsVisited(bloodPressureMeasurement.patientUuid)
                      .andThen(patientRepository.compareAndUpdateRecordedAt(bloodPressureMeasurement.patientUuid, createNewBpEntry.date.toUtcInstant(userClock)))
                      .toSingleDefault(bloodPressureMeasurement)
                }
          }
          .map { BloodPressureSaved(false) } // TODO(rj) Revisit this and fix (1. Analytics, 2. Prefill date information)
    }
  }

  private fun updateBpEntryTransformer(
      userSession: UserSession,
      facilityRepository: FacilityRepository,
      patientRepository: PatientRepository,
      bloodPressureRepository: BloodPressureRepository,
      userClock: UserClock
  ): ObservableTransformer<UpdateBpEntry, BloodPressureEntryEvent> {
    return ObservableTransformer { updateBpEntries ->
      updateBpEntries
          .flatMapSingle { updateBpEntry ->
            val userFacilityObservable = userSession.requireLoggedInUser()
                .flatMap { user ->
                  facilityRepository
                      .currentFacility(user)
                      .map { user to it }
                }
                .singleOrError()

            bloodPressureRepository
                .measurement(updateBpEntry.bpUuid)
                .firstOrError()
                .zipWith(userFacilityObservable)
                .map { (existingBp, userFacilityPair) ->
                  val (user, facility) = userFacilityPair
                  existingBp.copy(
                      systolic = updateBpEntry.systolic,
                      diastolic = updateBpEntry.diastolic,
                      recordedAt = updateBpEntry.date.toUtcInstant(userClock),
                      userUuid = user.uuid,
                      facilityUuid = facility.uuid
                  )
                }
          }
          .flatMapSingle {
            bloodPressureRepository.updateMeasurement(it)
                .andThen(patientRepository.compareAndUpdateRecordedAt(it.patientUuid, it.recordedAt))
                .toSingleDefault(BloodPressureSaved(false) as BloodPressureEntryEvent)
          }
    }
  }

  private fun getUserAndCurrentFacility(
      userSession: UserSession,
      facilityRepository: FacilityRepository
  ): Observable<Pair<User, Facility>> {
    return userSession
        .requireLoggedInUser()
        .flatMap { user -> facilityRepository.currentFacility(user).map { user to it } }
  }

  private fun createNewBpEntry(
      userFacilityPair: Pair<User, Facility>,
      createNewBpEntry: CreateNewBpEntry,
      bloodPressureRepository: BloodPressureRepository,
      userClock: UserClock
  ): Single<BloodPressureMeasurement> {
    val (user, currentFacility) = userFacilityPair
    val (patientUuid, systolic, diastolic, date) = createNewBpEntry
    return bloodPressureRepository.saveMeasurement(patientUuid, systolic, diastolic, user, currentFacility, date.toUtcInstant(userClock))
  }
}
