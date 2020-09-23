package org.simple.clinic.bp.entry

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.rxkotlin.cast
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.bp.ValidationResult
import org.simple.clinic.bp.ValidationResult.ErrorDiastolicEmpty
import org.simple.clinic.bp.ValidationResult.ErrorDiastolicTooHigh
import org.simple.clinic.bp.ValidationResult.ErrorDiastolicTooLow
import org.simple.clinic.bp.ValidationResult.ErrorSystolicEmpty
import org.simple.clinic.bp.ValidationResult.ErrorSystolicLessThanDiastolic
import org.simple.clinic.bp.ValidationResult.ErrorSystolicTooHigh
import org.simple.clinic.bp.ValidationResult.ErrorSystolicTooLow
import org.simple.clinic.bp.entry.PrefillDate.PrefillSpecificDate
import org.simple.clinic.facility.Facility
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.user.User
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.exhaustive
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.util.toLocalDateAtZone
import org.simple.clinic.util.toUtcInstant
import org.simple.clinic.uuid.UuidGenerator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Invalid.DateIsInFuture
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Invalid.InvalidPattern
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class BloodPressureEntryEffectHandler @AssistedInject constructor(
    @Assisted private val ui: BloodPressureEntryUi,
    private val patientRepository: PatientRepository,
    private val bloodPressureRepository: BloodPressureRepository,
    private val appointmentsRepository: AppointmentRepository,
    private val userClock: UserClock,
    private val schedulersProvider: SchedulersProvider,
    private val uuidGenerator: UuidGenerator,
    private val currentUser: Lazy<User>,
    private val currentFacility: Lazy<Facility>
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(ui: BloodPressureEntryUi): BloodPressureEntryEffectHandler
  }

  private val reportAnalyticsEvents = ReportAnalyticsEvents()

  fun build(): ObservableTransformer<BloodPressureEntryEffect, BloodPressureEntryEvent> {
    return RxMobius
        .subtypeEffectHandler<BloodPressureEntryEffect, BloodPressureEntryEvent>()
        .addTransformer(PrefillDate::class.java, prefillDate(schedulersProvider.ui()))
        .addAction(HideBpErrorMessage::class.java, ui::hideBpErrorMessage, schedulersProvider.ui())
        .addAction(ChangeFocusToDiastolic::class.java, ui::changeFocusToDiastolic, schedulersProvider.ui())
        .addAction(ChangeFocusToSystolic::class.java, ui::changeFocusToSystolic, schedulersProvider.ui())
        .addConsumer(SetSystolic::class.java, { ui.setSystolic(it.systolic) }, schedulersProvider.ui())
        .addTransformer(FetchBloodPressureMeasurement::class.java, fetchBloodPressureMeasurement(schedulersProvider.io()))
        .addConsumer(SetDiastolic::class.java, { ui.setDiastolic(it.diastolic) }, schedulersProvider.ui())
        .addConsumer(ShowConfirmRemoveBloodPressureDialog::class.java, { ui.showConfirmRemoveBloodPressureDialog(it.bpUuid) }, schedulersProvider.ui())
        .addAction(Dismiss::class.java, ui::dismiss, schedulersProvider.ui())
        .addAction(HideDateErrorMessage::class.java, ui::hideDateErrorMessage, schedulersProvider.ui())
        .addConsumer(ShowBpValidationError::class.java, { showBpValidationError(it.result) }, schedulersProvider.ui())
        .addAction(ShowDateEntryScreen::class.java, ui::showDateEntryScreen, schedulersProvider.ui())
        .addConsumer(ShowBpEntryScreen::class.java, { showBpEntryScreen(it.date) }, schedulersProvider.ui())
        .addConsumer(ShowDateValidationError::class.java, { showDateValidationError(it.result) }, schedulersProvider.ui())
        .addTransformer(CreateNewBpEntry::class.java, createNewBpEntryTransformer())
        .addAction(SetBpSavedResultAndFinish::class.java, ui::setBpSavedResultAndFinish, schedulersProvider.ui())
        .addTransformer(UpdateBpEntry::class.java, updateBpEntryTransformer())
        .build()
  }

  private fun prefillDate(scheduler: Scheduler): ObservableTransformer<PrefillDate, BloodPressureEntryEvent> {
    return ObservableTransformer { prefillDates ->
      prefillDates
          .map(::convertToLocalDate)
          .observeOn(scheduler)
          .doOnNext { setDateOnInputFields(it) }
          .doOnNext { ui.showDateOnDateButton(it) }
          .map { DatePrefilled(it) }
    }
  }

  private fun convertToLocalDate(prefillDate: PrefillDate): LocalDate {
    val instant = if (prefillDate is PrefillSpecificDate) prefillDate.date else Instant.now(userClock)
    return instant.toLocalDateAtZone(userClock.zone)
  }

  private fun setDateOnInputFields(dateToSet: LocalDate) {
    ui.setDateOnInputFields(
        dateToSet.dayOfMonth.toString(),
        dateToSet.monthValue.toString(),
        dateToSet.year.toString()
    )
  }

  private fun fetchBloodPressureMeasurement(
      scheduler: Scheduler
  ): ObservableTransformer<FetchBloodPressureMeasurement, BloodPressureEntryEvent> {
    return ObservableTransformer { bloodPressureMeasurements ->
      bloodPressureMeasurements
          .flatMapSingle { getExistingBloodPressureMeasurement(it.bpUuid).subscribeOn(scheduler) }
          .map { BloodPressureMeasurementFetched(it.reading.systolic, it.reading.diastolic, it.recordedAt) }
    }
  }

  private fun showBpValidationError(bpValidation: ValidationResult) {
    when (bpValidation) {
      is ErrorSystolicLessThanDiastolic -> ui.showSystolicLessThanDiastolicError()
      is ErrorSystolicTooHigh -> ui.showSystolicHighError()
      is ErrorSystolicTooLow -> ui.showSystolicLowError()
      is ErrorDiastolicTooHigh -> ui.showDiastolicHighError()
      is ErrorDiastolicTooLow -> ui.showDiastolicLowError()
      is ErrorSystolicEmpty -> ui.showSystolicEmptyError()
      is ErrorDiastolicEmpty -> ui.showDiastolicEmptyError()
      is ValidationResult.Valid -> {
        /* Nothing to do here. */
      }
    }.exhaustive()
  }

  private fun showBpEntryScreen(entryDate: LocalDate) {
    with(ui) {
      showBpEntryScreen()
      showDateOnDateButton(entryDate)
    }
  }

  private fun showDateValidationError(result: Result) {
    when (result) {
      is InvalidPattern -> ui.showInvalidDateError()
      is DateIsInFuture -> ui.showDateIsInFutureError()
      is Result.Valid -> throw IllegalStateException("Date validation error cannot be $result")
    }.exhaustive()
  }

  private fun createNewBpEntryTransformer(): ObservableTransformer<CreateNewBpEntry, BloodPressureEntryEvent> {
    return ObservableTransformer { createNewBpEntries ->
      createNewBpEntries
          .observeOn(schedulersProvider.io())
          .flatMapSingle { createNewBpEntry ->
            val user = currentUser.get()
            val facility = currentFacility.get()

            Single
                .fromCallable { storeNewBloodPressureMeasurement(user, facility, createNewBpEntry) }
                .flatMap { updateAppointmentsAsVisited(createNewBpEntry, it) }
          }
          .compose(reportAnalyticsEvents)
          .cast()
    }
  }

  private fun updateAppointmentsAsVisited(
      createNewBpEntry: CreateNewBpEntry,
      bloodPressureMeasurement: BloodPressureMeasurement
  ): Single<BloodPressureSaved> {
    val entryDate = createNewBpEntry.userEnteredDate.toUtcInstant(userClock)
    val compareAndUpdateRecordedAt = patientRepository
        .compareAndUpdateRecordedAt(bloodPressureMeasurement.patientUuid, entryDate)

    return appointmentsRepository
        .markAppointmentsCreatedBeforeTodayAsVisited(bloodPressureMeasurement.patientUuid)
        .andThen(compareAndUpdateRecordedAt)
        .toSingleDefault(BloodPressureSaved(createNewBpEntry.wasDateChanged))
  }

  private fun updateBpEntryTransformer(): ObservableTransformer<UpdateBpEntry, BloodPressureEntryEvent> {
    return ObservableTransformer { updateBpEntries ->
      updateBpEntries
          .flatMapSingle { updateBpEntry ->
            getUpdatedBloodPressureMeasurement(updateBpEntry)
                .map { bloodPressureMeasurement -> bloodPressureMeasurement to updateBpEntry.wasDateChanged }
          }
          .flatMapSingle { (bloodPressureMeasurement, wasDateChanged) ->
            storeUpdateBloodPressureMeasurement(bloodPressureMeasurement)
                .toSingleDefault(BloodPressureSaved(wasDateChanged))
          }
          .compose(reportAnalyticsEvents)
          .cast()
    }
  }

  private fun getUpdatedBloodPressureMeasurement(
      updateBpEntry: UpdateBpEntry
  ): Single<BloodPressureMeasurement> {
    return getExistingBloodPressureMeasurement(updateBpEntry.bpUuid)
        .map { existingBloodPressureMeasurement ->
          val user = currentUser.get()
          val facility = currentFacility.get()
          updateBloodPressureMeasurementValues(existingBloodPressureMeasurement, user.uuid, facility.uuid, updateBpEntry)
        }
  }

  private fun storeUpdateBloodPressureMeasurement(
      bloodPressureMeasurement: BloodPressureMeasurement
  ): Completable {
    val compareAndUpdateRecordedAt = patientRepository
        .compareAndUpdateRecordedAt(bloodPressureMeasurement.patientUuid, bloodPressureMeasurement.recordedAt)

    return bloodPressureRepository
        .updateMeasurement(bloodPressureMeasurement)
        .andThen(compareAndUpdateRecordedAt)
  }

  private fun updateBloodPressureMeasurementValues(
      existingMeasurement: BloodPressureMeasurement,
      userUuid: UUID,
      facilityUuid: UUID,
      updateBpEntry: UpdateBpEntry
  ): BloodPressureMeasurement {
    val (_, reading, parsedDateFromForm, _) = updateBpEntry

    return existingMeasurement.copy(
        userUuid = userUuid,
        facilityUuid = facilityUuid,
        reading = reading,
        recordedAt = parsedDateFromForm.toUtcInstant(userClock)
    )
  }

  private fun storeNewBloodPressureMeasurement(
      user: User,
      currentFacility: Facility,
      entry: CreateNewBpEntry
  ): BloodPressureMeasurement {
    val (patientUuid, reading, date, _) = entry
    return bloodPressureRepository.saveMeasurementBlocking(
        patientUuid = patientUuid,
        reading = reading,
        loggedInUser = user,
        currentFacility = currentFacility,
        recordedAt = date.toUtcInstant(userClock),
        uuid = uuidGenerator.v4())
  }

  private fun getExistingBloodPressureMeasurement(bpUuid: UUID): Single<BloodPressureMeasurement> =
      bloodPressureRepository.measurement(bpUuid).firstOrError()
}
