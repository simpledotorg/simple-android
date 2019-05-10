package org.simple.clinic.bp.entry

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.bp.entry.BloodPressureEntrySheet.ScreenType.BP_ENTRY
import org.simple.clinic.bp.entry.BloodPressureEntrySheet.ScreenType.DATE_ENTRY
import org.simple.clinic.bp.entry.BpValidator.Validation.ErrorDiastolicEmpty
import org.simple.clinic.bp.entry.BpValidator.Validation.ErrorDiastolicTooHigh
import org.simple.clinic.bp.entry.BpValidator.Validation.ErrorDiastolicTooLow
import org.simple.clinic.bp.entry.BpValidator.Validation.ErrorSystolicEmpty
import org.simple.clinic.bp.entry.BpValidator.Validation.ErrorSystolicLessThanDiastolic
import org.simple.clinic.bp.entry.BpValidator.Validation.ErrorSystolicTooHigh
import org.simple.clinic.bp.entry.BpValidator.Validation.ErrorSystolicTooLow
import org.simple.clinic.bp.entry.BpValidator.Validation.Success
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.util.UserInputDatePaddingCharacter
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result2.Invalid.DateIsInFuture
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result2.Invalid.InvalidPattern
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result2.Valid
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.OffsetTime
import org.threeten.bp.ZoneOffset.UTC
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class BloodPressureEntrySheetControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val sheet = mock<BloodPressureEntrySheet>()
  private val bloodPressureRepository = mock<BloodPressureRepository>()
  private val appointmentRepository = mock<AppointmentRepository>()
  private val dateValidator = mock<UserInputDateValidator>()
  private val bpValidator = mock<BpValidator>()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private lateinit var controller: BloodPressureEntrySheetController

  private val patientUuid = UUID.randomUUID()
  private val testUtcClock = TestUtcClock()
  private val testUserClock = TestUserClock()

  @Before
  fun setUp() {
    RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }

    whenever(dateValidator.dateInUserTimeZone()).thenReturn(LocalDate.now(testUtcClock))

    controller = BloodPressureEntrySheetController(
        bloodPressureRepository = bloodPressureRepository,
        appointmentRepository = appointmentRepository,
        dateValidator = dateValidator,
        bpValidator = bpValidator,
        userClock = testUserClock,
        inputDatePaddingCharacter = UserInputDatePaddingCharacter('0'))

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(sheet) }
  }

  @Test
  @Parameters(value = [
    "90|true",
    "120|true",
    "300|true",
    "66|false",
    "44|false"
  ])
  fun `when valid systolic value is entered, move cursor to diastolic field`(sampleSystolicBp: String, shouldMove: Boolean) {
    uiEvents.onNext(BloodPressureEntrySheetCreated(OpenAs.New(patientUuid)))
    uiEvents.onNext(BloodPressureSystolicTextChanged(sampleSystolicBp))
    uiEvents.onNext(BloodPressureSystolicTextChanged(""))
    uiEvents.onNext(BloodPressureSystolicTextChanged(sampleSystolicBp))

    when (shouldMove) {
      true -> verify(sheet, times(2)).changeFocusToDiastolic()
      false -> verify(sheet, never()).changeFocusToDiastolic()
    }
  }

  @Test
  @Parameters(value = [
    "170, 17",
    "17, 1",
    "1,",
    ","])
  fun `when backspace is pressed in diastolic field and it's text is empty, move cursor to systolic and delete last digit`(
      existingSystolic: String,
      systolicAfterBackspace: String
  ) {
    uiEvents.onNext(BloodPressureSystolicTextChanged(existingSystolic))
    uiEvents.onNext(BloodPressureDiastolicTextChanged("142"))

    uiEvents.onNext(BloodPressureDiastolicBackspaceClicked)
    uiEvents.onNext(BloodPressureDiastolicTextChanged("14"))

    uiEvents.onNext(BloodPressureDiastolicBackspaceClicked)
    uiEvents.onNext(BloodPressureDiastolicTextChanged("1"))

    uiEvents.onNext(BloodPressureDiastolicBackspaceClicked)
    uiEvents.onNext(BloodPressureDiastolicTextChanged(""))

    uiEvents.onNext(BloodPressureDiastolicBackspaceClicked)

    verify(sheet).changeFocusToSystolic()
    if (systolicAfterBackspace.isNotEmpty()) {
      verify(sheet).setSystolic(systolicAfterBackspace)
    }
  }

  @Test
  @Parameters(method = "params for bp validation errors and expected ui changes")
  fun `when BP entry is active, and BP readings are invalid then show error`(
      error: BpValidator.Validation,
      uiChangeVerification: (Ui) -> Unit
  ) {
    whenever(bpValidator.validate(any(), any())).thenReturn(error)

    uiEvents.onNext(BloodPressureScreenChanged(BP_ENTRY))
    uiEvents.onNext(BloodPressureEntrySheetCreated(OpenAs.New(patientUuid)))
    uiEvents.onNext(BloodPressureSystolicTextChanged("-"))
    uiEvents.onNext(BloodPressureDiastolicTextChanged("-"))
    uiEvents.onNext(BloodPressureSaveClicked)

    verify(bloodPressureRepository, never()).saveMeasurement(any(), any(), any(), any())
    verify(bloodPressureRepository, never()).updateMeasurement(any())

    uiChangeVerification(sheet)
  }

  @Suppress("unused")
  fun `params for bp validation errors and expected ui changes`(): List<Any> {
    return listOf(
        listOf<Any>(ErrorSystolicEmpty, { ui: Ui -> verify(ui).showSystolicEmptyError() }),
        listOf<Any>(ErrorDiastolicEmpty, { ui: Ui -> verify(ui).showDiastolicEmptyError() }),
        listOf<Any>(ErrorSystolicTooHigh, { ui: Ui -> verify(ui).showSystolicHighError() }),
        listOf<Any>(ErrorSystolicTooLow, { ui: Ui -> verify(ui).showSystolicLowError() }),
        listOf<Any>(ErrorDiastolicTooHigh, { ui: Ui -> verify(ui).showDiastolicHighError() }),
        listOf<Any>(ErrorDiastolicTooLow, { ui: Ui -> verify(ui).showDiastolicLowError() }),
        listOf<Any>(ErrorSystolicLessThanDiastolic, { ui: Ui -> verify(ui).showSystolicLessThanDiastolicError() })
    )
  }

  @Test
  fun `when systolic or diastolic values change, hide any error message`() {
    uiEvents.onNext(BloodPressureEntrySheetCreated(OpenAs.New(patientUuid)))
    uiEvents.onNext(BloodPressureSystolicTextChanged("12"))
    uiEvents.onNext(BloodPressureSystolicTextChanged("120"))
    uiEvents.onNext(BloodPressureSystolicTextChanged("130"))
    uiEvents.onNext(BloodPressureDiastolicTextChanged("90"))
    uiEvents.onNext(BloodPressureDiastolicTextChanged("99"))

    verify(sheet, times(5)).hideBpErrorMessage()
  }

  @Test
  @Parameters(value = [
    ",",
    "1,1"
  ])
  fun `when save is clicked, BP entry is active, but input is invalid then blood pressure measurement should not be saved`(
      systolic: String,
      diastolic: String
  ) {
    uiEvents.onNext(BloodPressureEntrySheetCreated(OpenAs.New(patientUuid)))
    uiEvents.onNext(BloodPressureSystolicTextChanged(systolic))
    uiEvents.onNext(BloodPressureDiastolicTextChanged(diastolic))
    uiEvents.onNext(BloodPressureSaveClicked)

    verify(bloodPressureRepository, never()).saveMeasurement(any(), any(), any(), any())
    verify(sheet, never()).setBpSavedResultAndFinish()
  }

  @Test
  @Parameters(method = "params for checking valid BP input")
  fun `when BP entry is active, BP readings are valid and save is clicked then date entry should be shown`(openAs: OpenAs) {
    whenever(bpValidator.validate(any(), any())).thenReturn(Success(142, 80))

    if (openAs is OpenAs.Update) {
      whenever(bloodPressureRepository.measurement(any())).thenReturn(Observable.never())
    }

    uiEvents.onNext(BloodPressureEntrySheetCreated(openAs))
    uiEvents.onNext(BloodPressureScreenChanged(BP_ENTRY))
    uiEvents.onNext(BloodPressureSystolicTextChanged("142"))
    uiEvents.onNext(BloodPressureDiastolicTextChanged("80"))
    uiEvents.onNext(BloodPressureSaveClicked)

    verify(bloodPressureRepository, never()).saveMeasurement(any(), any(), any(), any())
    verify(bloodPressureRepository, never()).updateMeasurement(any())
    verify(sheet).showDateEntryScreen()
  }

  @Test
  @Parameters(method = "params for OpenAs and bp validation errors")
  fun `when BP entry is active, BP readings are invalid and save is clicked then date entry should not be shown`(
      openAs: OpenAs,
      error: BpValidator.Validation
  ) {
    whenever(bpValidator.validate(any(), any())).thenReturn(error)
    whenever(bloodPressureRepository.measurement(any())).thenReturn(Observable.never())

    uiEvents.run {
      onNext(BloodPressureEntrySheetCreated(openAs = openAs))
      onNext(BloodPressureScreenChanged(BP_ENTRY))
      onNext(BloodPressureSystolicTextChanged("-"))
      onNext(BloodPressureDiastolicTextChanged("-"))
      onNext(BloodPressureSaveClicked)
    }

    verify(sheet, never()).showDateEntryScreen()
  }

  @Suppress("Unused")
  private fun `params for checking valid BP input`(): List<Any> {
    return listOf(OpenAs.New(patientUuid), OpenAs.Update(UUID.randomUUID()))
  }

  @Test
  @Parameters(method = "params for prefilling bp measurements")
  fun `when screen is opened to update a blood pressure, the blood pressure must be prefilled`(
      openAs: OpenAs,
      bloodPressureMeasurement: BloodPressureMeasurement?
  ) {
    if (openAs is OpenAs.Update) {
      whenever(bloodPressureRepository.measurement(any())).thenReturn(Observable.just(bloodPressureMeasurement!!))
    }

    uiEvents.onNext(BloodPressureEntrySheetCreated(openAs))

    if (openAs is OpenAs.Update) {
      val verify = verify(sheet)
      verify.setSystolic(bloodPressureMeasurement!!.systolic.toString())
      verify.setDiastolic(bloodPressureMeasurement.diastolic.toString())
    } else {
      val verify = verify(sheet, never())
      verify.setSystolic(any())
      verify.setDiastolic(any())
    }
  }

  @Suppress("Unused")
  private fun `params for prefilling bp measurements`(): List<List<Any?>> {
    val bpUuid = UUID.randomUUID()
    return listOf(
        listOf(OpenAs.New(patientUuid), null),
        listOf(OpenAs.Update(bpUuid), PatientMocker.bp(uuid = bpUuid, patientUuid = patientUuid))
    )
  }

  @Test
  @Parameters(method = "params for showing remove button")
  fun `the remove BP button must be shown when the sheet is opened for update`(
      openAs: OpenAs,
      shouldShowRemoveBpButton: Boolean
  ) {
    whenever(bloodPressureRepository.measurement(any())).thenReturn(Observable.just(PatientMocker.bp()))

    uiEvents.onNext(BloodPressureEntrySheetCreated(openAs))

    if (shouldShowRemoveBpButton) {
      verify(sheet).showRemoveBpButton()
    } else {
      verify(sheet).hideRemoveBpButton()
    }
  }

  @Suppress("Unused")
  private fun `params for showing remove button`(): List<List<Any>> {
    return listOf(
        listOf(OpenAs.New(UUID.randomUUID()), false),
        listOf(OpenAs.Update(UUID.randomUUID()), true))
  }

  @Test
  @Parameters(method = "params for setting the title of the sheet")
  fun `the correct title should be shown when the sheet is opened`(
      openAs: OpenAs,
      showEntryTitle: Boolean
  ) {
    whenever(bloodPressureRepository.measurement(any())).thenReturn(Observable.just(PatientMocker.bp()))

    uiEvents.onNext(BloodPressureEntrySheetCreated(openAs))

    if (showEntryTitle) {
      verify(sheet).showEnterNewBloodPressureTitle()
    } else {
      verify(sheet).showEditBloodPressureTitle()
    }
  }

  @Suppress("Unused")
  private fun `params for setting the title of the sheet`(): List<List<Any>> {
    return listOf(
        listOf(OpenAs.New(UUID.randomUUID()), true),
        listOf(OpenAs.Update(UUID.randomUUID()), false))
  }

  @Test
  fun `when the remove button is clicked, the confirmation alert must be shown`() {
    val bloodPressure = PatientMocker.bp()
    whenever(bloodPressureRepository.measurement(any())).thenReturn(Observable.just(bloodPressure))

    uiEvents.onNext(BloodPressureEntrySheetCreated(openAs = OpenAs.Update(bloodPressure.uuid)))
    uiEvents.onNext(BloodPressureRemoveClicked)

    verify(sheet).showConfirmRemoveBloodPressureDialog(bloodPressure.uuid)
  }

  @Test
  fun `when a blood pressure being edited is removed, the sheet should be closed`() {
    val bloodPressure = PatientMocker.bp()
    val bloodPressureSubject = BehaviorSubject.createDefault<BloodPressureMeasurement>(bloodPressure)
    whenever(bloodPressureRepository.measurement(bloodPressure.uuid)).thenReturn(bloodPressureSubject)

    uiEvents.onNext(BloodPressureEntrySheetCreated(openAs = OpenAs.Update(bpUuid = bloodPressure.uuid)))
    verify(sheet, never()).setBpSavedResultAndFinish()

    bloodPressureSubject.onNext(bloodPressure.copy(deletedAt = Instant.now()))
    verify(sheet).setBpSavedResultAndFinish()
  }

  @Test
  @Suppress("IMPLICIT_CAST_TO_ANY")
  @Parameters(method = "params for checking valid date input")
  fun `when save is clicked, date entry is active, but input is invalid then BP measurement should not be saved`(
      openAs: OpenAs,
      result: UserInputDateValidator.Result2
  ) {
    whenever(bloodPressureRepository.measurement(any())).thenReturn(Observable.never())
    whenever(dateValidator.validate2(any(), any())).thenReturn(result)

    uiEvents.onNext(BloodPressureEntrySheetCreated(openAs))
    uiEvents.onNext(BloodPressureScreenChanged(DATE_ENTRY))
    uiEvents.onNext(BloodPressureDayChanged("1"))
    uiEvents.onNext(BloodPressureMonthChanged("4"))
    uiEvents.onNext(BloodPressureYearChanged("9"))
    uiEvents.onNext(BloodPressureSaveClicked)

    when (openAs) {
      is OpenAs.New -> verify(bloodPressureRepository, never()).saveMeasurement(any(), any(), any(), any())
      is OpenAs.Update -> verify(bloodPressureRepository, never()).updateMeasurement(any())
      else -> throw AssertionError()
    }

    verify(sheet, never()).setBpSavedResultAndFinish()
    verify(dateValidator).validate2("01/04/1909")
  }

  @Suppress("Unused")
  private fun `params for checking valid date input`(): List<Any> {
    return listOf(
        listOf(OpenAs.New(patientUuid), InvalidPattern),
        listOf(OpenAs.New(patientUuid), DateIsInFuture),
        listOf(OpenAs.Update(UUID.randomUUID()), InvalidPattern),
        listOf(OpenAs.Update(UUID.randomUUID()), DateIsInFuture))
  }

  @Test
  fun `when save is clicked for a new BP, date entry is active and input is valid then a BP measurement should be saved`() {
    val inputDate = LocalDate.of(1990, 2, 13)
    whenever(bloodPressureRepository.saveMeasurement(any(), any(), any(), any())).thenReturn(Single.just(PatientMocker.bp()))
    whenever(appointmentRepository.markAppointmentsCreatedBeforeTodayAsVisited(patientUuid)).thenReturn(Completable.complete())
    whenever(bpValidator.validate(any(), any()))
        .thenReturn(ErrorSystolicEmpty)
        .thenReturn(Success(130, 110))

    whenever(dateValidator.validate2(any(), any()))
        .thenReturn(InvalidPattern)
        .thenReturn(Valid(inputDate))

    uiEvents.run {
      onNext(BloodPressureEntrySheetCreated(openAs = OpenAs.New(patientUuid)))
      onNext(BloodPressureScreenChanged(BP_ENTRY))
      onNext(BloodPressureSystolicTextChanged("invalid"))
      onNext(BloodPressureDiastolicTextChanged("invalid"))
      onNext(BloodPressureSystolicTextChanged("130"))
      onNext(BloodPressureDiastolicTextChanged("110"))
      onNext(BloodPressureSaveClicked)
      onNext(BloodPressureScreenChanged(DATE_ENTRY))
      onNext(BloodPressureDayChanged("13"))
      onNext(BloodPressureMonthChanged("02"))
      onNext(BloodPressureYearChanged("90"))
      onNext(BloodPressureMonthChanged("invalid"))
      onNext(BloodPressureSaveClicked)
    }

    verify(bloodPressureRepository, never()).updateMeasurement(any())

    val entryDateAsInstant = inputDate.atTime(OffsetTime.now(testUtcClock)).toInstant()
    verify(bloodPressureRepository).saveMeasurement(
        patientUuid,
        systolic = 130,
        diastolic = 110,
        recordedAt = entryDateAsInstant)
    verify(appointmentRepository).markAppointmentsCreatedBeforeTodayAsVisited(patientUuid)
    verify(sheet).setBpSavedResultAndFinish()
  }

  @Test
  fun `when save is clicked while updating a BP, date entry is active and input is valid then the updated BP measurement should be saved`() {
    val oldCreatedAt = LocalDate.of(1990, 1, 13).atTime(OffsetTime.now(testUtcClock)).toInstant()
    val existingBp = PatientMocker.bp(systolic = 9000, diastolic = 8999, createdAt = oldCreatedAt, updatedAt = oldCreatedAt, recordedAt = oldCreatedAt)

    val newInputDate = LocalDate.of(1991, 2, 14)
    whenever(dateValidator.validate2(any(), any())).thenReturn(Valid(newInputDate))
    whenever(bpValidator.validate(any(), any())).thenReturn(Success(120, 110))
    whenever(bloodPressureRepository.saveMeasurement(any(), any(), any(), any())).thenReturn(Single.just(PatientMocker.bp()))
    whenever(bloodPressureRepository.measurement(existingBp.uuid)).thenReturn(Observable.just(existingBp))
    whenever(bloodPressureRepository.updateMeasurement(any())).thenReturn(Completable.complete())

    uiEvents.run {
      onNext(BloodPressureEntrySheetCreated(openAs = OpenAs.Update(existingBp.uuid)))
      onNext(BloodPressureScreenChanged(BP_ENTRY))
      onNext(BloodPressureSystolicTextChanged("120"))
      onNext(BloodPressureDiastolicTextChanged("110"))
      onNext(BloodPressureSaveClicked)
      onNext(BloodPressureScreenChanged(DATE_ENTRY))
      onNext(BloodPressureDayChanged("14"))
      onNext(BloodPressureMonthChanged("02"))
      onNext(BloodPressureYearChanged("91"))
      onNext(BloodPressureSaveClicked)
    }

    val newInputDateAsInstant = newInputDate.atTime(OffsetTime.now(testUtcClock)).toInstant()
    val updatedBp = existingBp.copy(systolic = 120, diastolic = 110, updatedAt = oldCreatedAt, recordedAt = newInputDateAsInstant)
    verify(bloodPressureRepository).updateMeasurement(updatedBp)

    verify(bloodPressureRepository, never()).saveMeasurement(any(), any(), any(), any())
    verify(appointmentRepository, never()).markAppointmentsCreatedBeforeTodayAsVisited(any())
    verify(sheet).setBpSavedResultAndFinish()
  }

  @Test
  @Suppress("IMPLICIT_CAST_TO_ANY")
  @Parameters(method = "params for showing date validation errors")
  fun `when save is clicked, date entry is active and input is invalid then validation errors should be shown`(
      openAs: OpenAs,
      errorResult: UserInputDateValidator.Result2,
      uiChangeVerification: UiChange
  ) {
    whenever(dateValidator.validate2(any(), any())).thenReturn(errorResult)
    whenever(bloodPressureRepository.measurement(any())).thenReturn(Observable.never())

    uiEvents.run {
      onNext(BloodPressureEntrySheetCreated(openAs = openAs))
      onNext(BloodPressureScreenChanged(DATE_ENTRY))
      onNext(BloodPressureDayChanged("14"))
      onNext(BloodPressureMonthChanged("02"))
      onNext(BloodPressureYearChanged("1991"))
      onNext(BloodPressureSaveClicked)
    }

    verify(bloodPressureRepository, never()).saveMeasurement(any(), any(), any(), any())
    verify(bloodPressureRepository, never()).updateMeasurement(any())
    verify(sheet, never()).setBpSavedResultAndFinish()

    uiChangeVerification(sheet)
  }

  @Suppress("unused")
  fun `params for showing date validation errors`(): List<Any> {
    val existingBpUuid = UUID.randomUUID()
    return listOf(
        listOf(OpenAs.New(patientUuid), InvalidPattern, { ui: Ui -> verify(ui).showInvalidDateError() }),
        listOf(OpenAs.Update(existingBpUuid), InvalidPattern, { ui: Ui -> verify(ui).showInvalidDateError() }),
        listOf(OpenAs.New(patientUuid), DateIsInFuture, { ui: Ui -> verify(ui).showDateIsInFutureError() }),
        listOf(OpenAs.Update(existingBpUuid), DateIsInFuture, { ui: Ui -> verify(ui).showDateIsInFutureError() }))
  }

  @Test
  fun `when date values change, hide any error message`() {
    uiEvents.run {
      onNext(BloodPressureDayChanged("14"))
      onNext(BloodPressureMonthChanged("02"))
      onNext(BloodPressureYearChanged("1991"))
    }

    verify(sheet).hideDateErrorMessage()
  }

  @Test
  @Parameters(method = "params for OpenAs types")
  fun `when BP entry is active, BP readings are valid and next arrow is pressed then date entry should be shown`(
      openAs: OpenAs
  ) {
    whenever(bpValidator.validate(any(), any())).thenReturn(Success(120, 110))
    whenever(bloodPressureRepository.measurement(any())).thenReturn(Observable.never())

    uiEvents.run {
      onNext(BloodPressureEntrySheetCreated(openAs = openAs))
      onNext(BloodPressureScreenChanged(BP_ENTRY))
      onNext(BloodPressureSystolicTextChanged("120"))
      onNext(BloodPressureDiastolicTextChanged("110"))
      onNext(BloodPressureNextArrowClicked)
    }

    verify(sheet).showDateEntryScreen()
  }

  @Test
  @Parameters(method = "params for OpenAs and bp validation errors")
  fun `when BP entry is active, BP readings are invalid and next arrow is pressed then date entry should not be shown`(
      openAs: OpenAs,
      error: BpValidator.Validation
  ) {
    whenever(bpValidator.validate(any(), any())).thenReturn(error)
    whenever(bloodPressureRepository.measurement(any())).thenReturn(Observable.never())

    uiEvents.run {
      onNext(BloodPressureEntrySheetCreated(openAs = openAs))
      onNext(BloodPressureScreenChanged(BP_ENTRY))
      onNext(BloodPressureSystolicTextChanged("-"))
      onNext(BloodPressureDiastolicTextChanged("-"))
      onNext(BloodPressureNextArrowClicked)
    }

    verify(sheet, never()).showDateEntryScreen()
  }

  @Suppress("unused")
  fun `params for OpenAs and bp validation errors`(): List<Any> {
    val bpUuid = UUID.randomUUID()
    return listOf(
        listOf(OpenAs.New(patientUuid), ErrorSystolicEmpty),
        listOf(OpenAs.New(patientUuid), ErrorDiastolicEmpty),
        listOf(OpenAs.New(patientUuid), ErrorSystolicTooHigh),
        listOf(OpenAs.New(patientUuid), ErrorSystolicTooLow),
        listOf(OpenAs.New(patientUuid), ErrorDiastolicTooHigh),
        listOf(OpenAs.New(patientUuid), ErrorDiastolicTooLow),
        listOf(OpenAs.New(patientUuid), ErrorSystolicLessThanDiastolic),
        listOf(OpenAs.Update(bpUuid), ErrorSystolicEmpty),
        listOf(OpenAs.Update(bpUuid), ErrorDiastolicEmpty),
        listOf(OpenAs.Update(bpUuid), ErrorSystolicTooHigh),
        listOf(OpenAs.Update(bpUuid), ErrorSystolicTooLow),
        listOf(OpenAs.Update(bpUuid), ErrorDiastolicTooHigh),
        listOf(OpenAs.Update(bpUuid), ErrorDiastolicTooLow),
        listOf(OpenAs.Update(bpUuid), ErrorSystolicLessThanDiastolic))
  }

  @Test
  @Parameters(method = "params for OpenAs types")
  fun `when date entry is active and previous arrow is pressed then BP entry should be shown`(
      openAs: OpenAs
  ) {
    uiEvents.run {
      onNext(BloodPressureScreenChanged(DATE_ENTRY))
      onNext(BloodPressurePreviousArrowClicked)
    }

    verify(sheet).showBpEntryScreen()
  }

  @Test
  fun `when date entry is active and back is pressed then BP entry should be shown`() {
    uiEvents.run {
      onNext(BloodPressureScreenChanged(DATE_ENTRY))
      onNext(BloodPressureBackPressed)
    }

    verify(sheet).showBpEntryScreen()
    verify(sheet, never()).finish()
  }

  @Test
  fun `when BP entry is active and back is pressed then the sheet should be closed`() {
    uiEvents.run {
      onNext(BloodPressureScreenChanged(BP_ENTRY))
      onNext(BloodPressureBackPressed)
    }

    verify(sheet).finish()
  }

  @Test
  fun `when screen is opened for a new BP, then the date should be prefilled with the current date`() {
    val currentDate = LocalDate.of(2018, 4, 23)
    testUserClock.advanceBy(Duration.ofSeconds(currentDate.atStartOfDay().toEpochSecond(UTC)))

    uiEvents.onNext(BloodPressureEntrySheetCreated(OpenAs.New(patientUuid)))

    verify(sheet).setDate(
        dayOfMonth = "23",
        month = "04",
        twoDigitYear = "18")
  }

  @Test
  fun `when screen is opened for updating an existing BP, then the date should be prefilled with the BP's existing date`() {
    val recordedAtDate = LocalDate.of(2018, 4, 23)
    val recordedAtDateAsInstant = recordedAtDate.atStartOfDay().toInstant(UTC)
    val existingBp = PatientMocker.bp(recordedAt = recordedAtDateAsInstant)

    whenever(bloodPressureRepository.measurement(existingBp.uuid)).thenReturn(Observable.just(existingBp, existingBp))

    uiEvents.onNext(BloodPressureEntrySheetCreated(OpenAs.Update(existingBp.uuid)))

    verify(sheet, times(1)).setDate(
        dayOfMonth = "23",
        month = "04",
        twoDigitYear = "18")
  }

  @Suppress("Unused")
  private fun `params for OpenAs types`(): List<Any> {
    return listOf(OpenAs.New(patientUuid), OpenAs.Update(UUID.randomUUID()))
  }

  @Test
  fun `while BP readings are invalid, next arrow should remain disabled`() {
    whenever(bpValidator.validate(any(), any()))
        .thenReturn(ErrorDiastolicEmpty)
        .thenReturn(ErrorDiastolicTooHigh)
        .thenReturn(Success(systolic = 0, diastolic = 0))
        .thenReturn(ErrorSystolicLessThanDiastolic)

    uiEvents.run {
      onNext(BloodPressureScreenChanged(BP_ENTRY))
      onNext(BloodPressureSystolicTextChanged("-"))
      onNext(BloodPressureDiastolicTextChanged("-"))
      onNext(BloodPressureSystolicTextChanged("-"))
      onNext(BloodPressureDiastolicTextChanged("-"))
      onNext(BloodPressureDiastolicTextChanged("-"))
    }

    verify(sheet, times(2)).setNextArrowIconEnabled(false)
    verify(sheet, times(1)).setNextArrowIconEnabled(true)
  }

  @Test
  @Parameters(method = "params for OpenAs types")
  fun `when save is clicked, user input is complete, but date entry is not active then BP should not be saved`(
      openAs: OpenAs
  ) {
    val newInputDate = LocalDate.of(1991, 2, 14)
    whenever(dateValidator.validate2(any(), any())).thenReturn(Valid(newInputDate))
    whenever(bpValidator.validate(any(), any())).thenReturn(Success(120, 110))
    whenever(bloodPressureRepository.saveMeasurement(any(), any(), any(), any())).thenReturn(Single.just(PatientMocker.bp()))
    whenever(bloodPressureRepository.updateMeasurement(any())).thenReturn(Completable.complete())

    if (openAs is OpenAs.Update) {
      val oldCreatedAt = LocalDate.of(1990, 1, 13).atTime(OffsetTime.now(testUtcClock)).toInstant()
      val existingBp = PatientMocker.bp(
          uuid = openAs.bpUuid,
          systolic = 9000,
          diastolic = 8999,
          createdAt = oldCreatedAt,
          updatedAt = oldCreatedAt)
      whenever(bloodPressureRepository.measurement(existingBp.uuid)).thenReturn(Observable.just(existingBp))
    }

    uiEvents.run {
      onNext(BloodPressureEntrySheetCreated(openAs = openAs))
      onNext(BloodPressureScreenChanged(BP_ENTRY))
      onNext(BloodPressureSystolicTextChanged("120"))
      onNext(BloodPressureDiastolicTextChanged("110"))
      onNext(BloodPressureSaveClicked)
      onNext(BloodPressureScreenChanged(DATE_ENTRY))
      onNext(BloodPressureDayChanged("14"))
      onNext(BloodPressureMonthChanged("02"))
      onNext(BloodPressureYearChanged("91"))
      onNext(BloodPressureScreenChanged(BP_ENTRY))
      onNext(BloodPressureSaveClicked)
    }

    verify(bloodPressureRepository, never()).updateMeasurement(any())
    verify(bloodPressureRepository, never()).saveMeasurement(any(), any(), any(), any())
    verify(appointmentRepository, never()).markAppointmentsCreatedBeforeTodayAsVisited(any())
    verify(sheet, never()).setBpSavedResultAndFinish()
  }
}
