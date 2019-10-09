package org.simple.clinic.bp.entry

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.reset
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
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
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.util.UserInputDatePaddingCharacter
import org.simple.clinic.util.toLocalDateAtZone
import org.simple.clinic.util.toUtcInstant
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Invalid.DateIsInFuture
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Invalid.InvalidPattern
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Valid
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneOffset.UTC
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class BloodPressureEntrySheetControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val ui = mock<BloodPressureEntryUi>()
  private val bloodPressureRepository = mock<BloodPressureRepository>()
  private val appointmentRepository = mock<AppointmentRepository>()
  private val patientRepository = mock<PatientRepository>()
  private val dateValidator = mock<UserInputDateValidator>()
  private val bpValidator = mock<BpValidator>()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private val patientUuid = UUID.fromString("79145baf-7a5c-4442-ab30-2da564a32944")

  private val testUtcClock = TestUtcClock()
  private val testUserClock = TestUserClock()
  private val userSession = mock<UserSession>()

  private val facilityRepository = mock<FacilityRepository>()
  private val user = PatientMocker.loggedInUser(uuid = UUID.fromString("1367a583-12b1-48c6-ae9d-fb34f9aac449"))

  private val facility = PatientMocker.facility(uuid = UUID.fromString("2a70f82e-92c6-4fce-b60e-6f083a8e725b"))
  private val userSubject = PublishSubject.create<User>()

  private val controller = BloodPressureEntrySheetController(
      bloodPressureRepository = bloodPressureRepository,
      appointmentRepository = appointmentRepository,
      patientRepository = patientRepository,
      dateValidator = dateValidator,
      bpValidator = bpValidator,
      userClock = testUserClock,
      inputDatePaddingCharacter = UserInputDatePaddingCharacter('0'),
      userSession = userSession,
      facilityRepository = facilityRepository)

  @Before
  fun setUp() {
    RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }

    whenever(dateValidator.dateInUserTimeZone()).doReturn(LocalDate.now(testUtcClock))
    whenever(userSession.requireLoggedInUser()).doReturn(userSubject)
    whenever(facilityRepository.currentFacility(user)).doReturn(Observable.just(facility))

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(ui) }

    userSubject.onNext(user)
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
    uiEvents.onNext(SheetCreated(OpenAs.New(patientUuid)))
    uiEvents.onNext(SystolicChanged(sampleSystolicBp))
    uiEvents.onNext(SystolicChanged(""))
    uiEvents.onNext(SystolicChanged(sampleSystolicBp))

    when (shouldMove) {
      true -> verify(ui, times(2)).changeFocusToDiastolic()
      false -> verify(ui, never()).changeFocusToDiastolic()
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
    uiEvents.onNext(SystolicChanged(existingSystolic))
    uiEvents.onNext(DiastolicChanged("142"))

    uiEvents.onNext(DiastolicBackspaceClicked)
    uiEvents.onNext(DiastolicChanged("14"))

    uiEvents.onNext(DiastolicBackspaceClicked)
    uiEvents.onNext(DiastolicChanged("1"))

    uiEvents.onNext(DiastolicBackspaceClicked)
    uiEvents.onNext(DiastolicChanged(""))

    uiEvents.onNext(DiastolicBackspaceClicked)

    verify(ui).changeFocusToSystolic()
    if (systolicAfterBackspace.isNotEmpty()) {
      verify(ui).setSystolic(systolicAfterBackspace)
    }
  }

  @Test
  @Parameters(method = "params for bp validation errors and expected ui changes")
  fun `when BP entry is active, and BP readings are invalid then show error`(
      error: BpValidator.Validation,
      uiChangeVerification: (Ui) -> Unit
  ) {
    whenever(bpValidator.validate(any(), any())).doReturn(error)

    uiEvents.onNext(ScreenChanged(BP_ENTRY))
    uiEvents.onNext(SheetCreated(OpenAs.New(patientUuid)))
    uiEvents.onNext(SystolicChanged("-"))
    uiEvents.onNext(DiastolicChanged("-"))
    uiEvents.onNext(SaveClicked)

    verify(bloodPressureRepository, never()).saveMeasurement(any(), any(), any(), any(), any(), any())
    verify(bloodPressureRepository, never()).updateMeasurement(any())

    uiChangeVerification(ui)
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
    uiEvents.onNext(SheetCreated(OpenAs.New(patientUuid)))
    uiEvents.onNext(SystolicChanged("12"))
    uiEvents.onNext(SystolicChanged("120"))
    uiEvents.onNext(SystolicChanged("130"))
    uiEvents.onNext(DiastolicChanged("90"))
    uiEvents.onNext(DiastolicChanged("99"))

    verify(ui, times(5)).hideBpErrorMessage()
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
    uiEvents.onNext(SheetCreated(OpenAs.New(patientUuid)))
    uiEvents.onNext(SystolicChanged(systolic))
    uiEvents.onNext(DiastolicChanged(diastolic))
    uiEvents.onNext(SaveClicked)

    verify(bloodPressureRepository, never()).saveMeasurement(any(), any(), any(), any(), any(), any())
    verify(ui, never()).setBpSavedResultAndFinish()
  }

  @Test
  @Parameters(method = "params for OpenAs and bp validation errors")
  fun `when BP entry is active, BP readings are invalid and save is clicked then date entry should not be shown`(
      openAs: OpenAs,
      error: BpValidator.Validation
  ) {
    whenever(bpValidator.validate(any(), any())).doReturn(error)
    whenever(bloodPressureRepository.measurement(any())).doReturn(Observable.never())

    uiEvents.run {
      onNext(SheetCreated(openAs = openAs))
      onNext(ScreenChanged(BP_ENTRY))
      onNext(SystolicChanged("-"))
      onNext(DiastolicChanged("-"))
      onNext(SaveClicked)
    }

    verify(ui, never()).showDateEntryScreen()
  }

  @Test
  @Parameters(method = "params for prefilling bp measurements")
  fun `when screen is opened to update a blood pressure, the blood pressure must be prefilled`(
      openAs: OpenAs,
      bloodPressureMeasurement: BloodPressureMeasurement?
  ) {
    if (openAs is OpenAs.Update) {
      whenever(bloodPressureRepository.measurement(any())).doReturn(Observable.just(bloodPressureMeasurement!!))
    }

    uiEvents.onNext(SheetCreated(openAs))

    if (openAs is OpenAs.Update) {
      val verify = verify(ui)
      verify.setSystolic(bloodPressureMeasurement!!.systolic.toString())
      verify.setDiastolic(bloodPressureMeasurement.diastolic.toString())
    } else {
      val verify = verify(ui, never())
      verify.setSystolic(any())
      verify.setDiastolic(any())
    }
  }

  @Suppress("Unused")
  private fun `params for prefilling bp measurements`(): List<List<Any?>> {
    val bpUuid = UUID.fromString("8ddd7237-f331-4d34-9ce6-a3cd0c4c8133")
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
    whenever(bloodPressureRepository.measurement(any())).doReturn(Observable.just(PatientMocker.bp()))

    uiEvents.onNext(SheetCreated(openAs))

    if (shouldShowRemoveBpButton) {
      verify(ui).showRemoveBpButton()
    } else {
      verify(ui).hideRemoveBpButton()
    }
  }

  @Suppress("Unused")
  private fun `params for showing remove button`(): List<List<Any>> {
    return listOf(
        listOf(OpenAs.New(UUID.fromString("3127284e-8db4-4359-9386-57b8837573e9")), false),
        listOf(OpenAs.Update(UUID.fromString("baac5893-3670-4d9c-a5ff-12405cbb1ad5")), true))
  }

  @Test
  @Parameters(method = "params for setting the title of the sheet")
  fun `the correct title should be shown when the sheet is opened`(
      openAs: OpenAs,
      showEntryTitle: Boolean
  ) {
    whenever(bloodPressureRepository.measurement(any())).doReturn(Observable.just(PatientMocker.bp()))

    uiEvents.onNext(SheetCreated(openAs))

    if (showEntryTitle) {
      verify(ui).showEnterNewBloodPressureTitle()
    } else {
      verify(ui).showEditBloodPressureTitle()
    }
  }

  @Suppress("Unused")
  private fun `params for setting the title of the sheet`(): List<List<Any>> {
    return listOf(
        listOf(OpenAs.New(UUID.fromString("1526b550-c7a1-440a-a916-043f959bc6c5")), true),
        listOf(OpenAs.Update(UUID.fromString("66f89f1a-4d13-4491-970d-0d09c9ce4043")), false))
  }

  @Test
  fun `when the remove button is clicked, the confirmation alert must be shown`() {
    val bloodPressure = PatientMocker.bp()
    whenever(bloodPressureRepository.measurement(any())).doReturn(Observable.just(bloodPressure))

    uiEvents.onNext(SheetCreated(openAs = OpenAs.Update(bloodPressure.uuid)))
    uiEvents.onNext(RemoveClicked)

    verify(ui).showConfirmRemoveBloodPressureDialog(bloodPressure.uuid)
  }

  @Test
  fun `when a blood pressure being edited is removed, the sheet should be closed`() {
    val bloodPressure = PatientMocker.bp()
    val bloodPressureSubject = BehaviorSubject.createDefault<BloodPressureMeasurement>(bloodPressure)
    whenever(bloodPressureRepository.measurement(bloodPressure.uuid)).doReturn(bloodPressureSubject)

    uiEvents.onNext(SheetCreated(openAs = OpenAs.Update(bpUuid = bloodPressure.uuid)))
    verify(ui, never()).setBpSavedResultAndFinish()

    bloodPressureSubject.onNext(bloodPressure.copy(deletedAt = Instant.now()))
    verify(ui).setBpSavedResultAndFinish()
  }

  @Test
  @Suppress("IMPLICIT_CAST_TO_ANY")
  @Parameters(method = "params for checking valid date input")
  fun `when save is clicked, date entry is active, but input is invalid then BP measurement should not be saved`(
      openAs: OpenAs,
      result: UserInputDateValidator.Result
  ) {
    whenever(bloodPressureRepository.measurement(any())).doReturn(Observable.never())
    whenever(dateValidator.validate(any(), any())).doReturn(result)

    uiEvents.onNext(SheetCreated(openAs))
    uiEvents.onNext(ScreenChanged(DATE_ENTRY))
    uiEvents.onNext(DayChanged("1"))
    uiEvents.onNext(MonthChanged("4"))
    uiEvents.onNext(YearChanged("9"))
    uiEvents.onNext(SaveClicked)

    when (openAs) {
      is OpenAs.New -> verify(bloodPressureRepository, never()).saveMeasurement(any(), any(), any(), any(), any(), any())
      is OpenAs.Update -> verify(bloodPressureRepository, never()).updateMeasurement(any())
      else -> throw AssertionError()
    }

    verify(ui, never()).setBpSavedResultAndFinish()
    verify(dateValidator).validate("01/04/1909")
  }

  @Suppress("Unused")
  private fun `params for checking valid date input`(): List<Any> {
    return listOf(
        listOf(OpenAs.New(patientUuid), InvalidPattern),
        listOf(OpenAs.New(patientUuid), DateIsInFuture),
        listOf(OpenAs.Update(UUID.fromString("f6f27cad-8b82-461e-8b1e-e14c2ac63832")), InvalidPattern),
        listOf(OpenAs.Update(UUID.fromString("3b082f5e-8e0e-4aa7-a0cb-2d99ab020a30")), DateIsInFuture))
  }

  @Test
  fun `when save is clicked for a new BP, date entry is active and input is valid then a BP measurement should be saved`() {
    val inputDate = LocalDate.of(1990, 2, 13)
    whenever(patientRepository.compareAndUpdateRecordedAt(any(), any())).doReturn(Completable.complete())
    whenever(bloodPressureRepository.saveMeasurement(any(), any(), any(), any(), any(), any()))
        .doReturn(Single.just(PatientMocker.bp()))
    whenever(appointmentRepository.markAppointmentsCreatedBeforeTodayAsVisited(patientUuid)).doReturn(Completable.complete())
    whenever(bpValidator.validate(any(), any()))
        .doReturn(ErrorSystolicEmpty)
        .doReturn(Success(130, 110))

    whenever(dateValidator.validate(any(), any()))
        .doReturn(InvalidPattern)
        .doReturn(Valid(inputDate))

    uiEvents.run {
      onNext(SheetCreated(openAs = OpenAs.New(patientUuid)))
      onNext(ScreenChanged(BP_ENTRY))
      onNext(SystolicChanged("invalid"))
      onNext(DiastolicChanged("invalid"))
      onNext(SystolicChanged("130"))
      onNext(DiastolicChanged("110"))
      onNext(SaveClicked)
      onNext(ScreenChanged(DATE_ENTRY))
      onNext(DayChanged("13"))
      onNext(MonthChanged("02"))
      onNext(YearChanged("90"))
      onNext(MonthChanged("invalid"))
      onNext(SaveClicked)
    }

    verify(bloodPressureRepository, never()).updateMeasurement(any())

    val entryDateAsInstant = inputDate.toUtcInstant(testUserClock)
    verify(bloodPressureRepository).saveMeasurement(
        patientUuid,
        systolic = 130,
        diastolic = 110,
        loggedInUser = user,
        currentFacility = facility,
        recordedAt = entryDateAsInstant)
    verify(appointmentRepository).markAppointmentsCreatedBeforeTodayAsVisited(patientUuid)
    verify(patientRepository).compareAndUpdateRecordedAt(patientUuid, entryDateAsInstant)
    verify(ui).setBpSavedResultAndFinish()
  }

  @Test
  fun `when save is clicked while updating a BP, date entry is active and input is valid then the updated BP measurement should be saved`() {
    val oldCreatedAt = LocalDate.of(1990, 1, 13).toUtcInstant(testUserClock)
    val existingBp = PatientMocker.bp(
        systolic = 9000,
        diastolic = 8999,
        createdAt = oldCreatedAt,
        updatedAt = oldCreatedAt,
        recordedAt = oldCreatedAt
    )

    val newInputDate = LocalDate.of(1991, 2, 14)
    whenever(dateValidator.validate(any(), any())).doReturn(Valid(newInputDate))
    whenever(bpValidator.validate(any(), any())).doReturn(Success(120, 110))
    whenever(bloodPressureRepository.saveMeasurement(any(), any(), any(), any(), any(), any())).doReturn(Single.just(PatientMocker.bp()))
    whenever(bloodPressureRepository.measurement(existingBp.uuid)).doReturn(Observable.just(existingBp))
    whenever(bloodPressureRepository.updateMeasurement(any())).doReturn(Completable.complete())
    whenever(patientRepository.compareAndUpdateRecordedAt(any(), any())).doReturn(Completable.complete())

    uiEvents.run {
      onNext(SheetCreated(openAs = OpenAs.Update(existingBp.uuid)))
      onNext(ScreenChanged(BP_ENTRY))
      onNext(SystolicChanged("120"))
      onNext(DiastolicChanged("110"))
      onNext(SaveClicked)
    }

    val newUserUuid = UUID.fromString("a726d885-b12d-40b7-9fe1-5391f3fc0f88")
    val newFacilityUuid = UUID.fromString("d51f48fd-40b6-4daf-beae-ba3d9f4c24cd")
    val newUser = user.copy(uuid = newUserUuid)

    whenever(facilityRepository.currentFacility(newUser)).doReturn(Observable.just(facility.copy(uuid = newFacilityUuid)))

    userSubject.onNext(newUser)

    uiEvents.run {
      onNext(ScreenChanged(DATE_ENTRY))
      onNext(DayChanged("14"))
      onNext(MonthChanged("02"))
      onNext(YearChanged("91"))
      onNext(SaveClicked)
    }

    val newInputDateAsInstant = newInputDate.toUtcInstant(testUserClock)
    val updatedBp = existingBp.copy(
        systolic = 120,
        diastolic = 110,
        updatedAt = oldCreatedAt,
        recordedAt = newInputDateAsInstant,
        userUuid = newUserUuid,
        facilityUuid = newFacilityUuid
    )
    verify(bloodPressureRepository).updateMeasurement(updatedBp)
    verify(patientRepository).compareAndUpdateRecordedAt(updatedBp.patientUuid, updatedBp.recordedAt)

    verify(bloodPressureRepository, never()).saveMeasurement(any(), any(), any(), any(), any(), any())
    verify(appointmentRepository, never()).markAppointmentsCreatedBeforeTodayAsVisited(any())
    verify(ui).setBpSavedResultAndFinish()
  }

  @Test
  @Suppress("IMPLICIT_CAST_TO_ANY")
  @Parameters(method = "params for showing date validation errors")
  fun `when save is clicked, date entry is active and input is invalid then validation errors should be shown`(
      openAs: OpenAs,
      errorResult: UserInputDateValidator.Result,
      uiChangeVerification: UiChange
  ) {
    whenever(dateValidator.validate(any(), any())).doReturn(errorResult)
    whenever(bloodPressureRepository.measurement(any())).doReturn(Observable.never())

    uiEvents.run {
      onNext(SheetCreated(openAs = openAs))
      onNext(ScreenChanged(DATE_ENTRY))
      onNext(DayChanged("14"))
      onNext(MonthChanged("02"))
      onNext(YearChanged("1991"))
      onNext(SaveClicked)
    }

    verify(bloodPressureRepository, never()).saveMeasurement(any(), any(), any(), any(), any(), any())
    verify(bloodPressureRepository, never()).updateMeasurement(any())
    verify(ui, never()).setBpSavedResultAndFinish()

    uiChangeVerification(ui)
  }

  @Suppress("unused")
  fun `params for showing date validation errors`(): List<Any> {
    val existingBpUuid = UUID.fromString("2c4eccbb-d1bc-4c7c-b1ec-60a13acfeea4")
    return listOf(
        listOf(OpenAs.New(patientUuid), InvalidPattern, { ui: Ui -> verify(ui).showInvalidDateError() }),
        listOf(OpenAs.Update(existingBpUuid), InvalidPattern, { ui: Ui -> verify(ui).showInvalidDateError() }),
        listOf(OpenAs.New(patientUuid), DateIsInFuture, { ui: Ui -> verify(ui).showDateIsInFutureError() }),
        listOf(OpenAs.Update(existingBpUuid), DateIsInFuture, { ui: Ui -> verify(ui).showDateIsInFutureError() }))
  }

  @Test
  fun `when date values change, hide any error message`() {
    uiEvents.run {
      onNext(DayChanged("14"))
      onNext(MonthChanged("02"))
      onNext(YearChanged("1991"))
    }

    verify(ui).hideDateErrorMessage()
  }

  @Test
  @Parameters(method = "params for OpenAs types")
  fun `when BP entry is active, BP readings are valid and next arrow is pressed then date entry should be shown`(
      openAs: OpenAs
  ) {
    whenever(bpValidator.validate(any(), any())).doReturn(Success(120, 110))
    whenever(bloodPressureRepository.measurement(any())).doReturn(Observable.never())

    uiEvents.run {
      onNext(SheetCreated(openAs = openAs))
      onNext(ScreenChanged(BP_ENTRY))
      onNext(SystolicChanged("120"))
      onNext(DiastolicChanged("110"))
      onNext(BloodPressureDateClicked)
    }

    verify(ui).showDateEntryScreen()
  }

  @Test
  @Parameters(method = "params for OpenAs and bp validation errors")
  fun `when BP entry is active, BP readings are invalid and next arrow is pressed then date entry should not be shown`(
      openAs: OpenAs,
      error: BpValidator.Validation
  ) {
    whenever(bpValidator.validate(any(), any())).doReturn(error)
    whenever(bloodPressureRepository.measurement(any())).doReturn(Observable.never())

    uiEvents.run {
      onNext(SheetCreated(openAs = openAs))
      onNext(ScreenChanged(BP_ENTRY))
      onNext(SystolicChanged("-"))
      onNext(DiastolicChanged("-"))
      onNext(BloodPressureDateClicked)
    }

    verify(ui, never()).showDateEntryScreen()
  }

  @Suppress("unused")
  fun `params for OpenAs and bp validation errors`(): List<Any> {
    val bpUuid = UUID.fromString("99fed5e5-19a8-4ece-9d07-6beab70ee77c")
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
  fun `when BP entry is active and back is pressed then the sheet should be closed`() {
    uiEvents.run {
      onNext(ScreenChanged(BP_ENTRY))
      onNext(BackPressed)
    }

    verify(ui).dismiss()
  }

  @Test
  fun `when screen is opened for a new BP, then the date should be prefilled with the current date`() {
    val currentDate = LocalDate.of(2018, 4, 23)
    testUserClock.advanceBy(Duration.ofSeconds(currentDate.atStartOfDay().toEpochSecond(UTC)))

    uiEvents.onNext(SheetCreated(OpenAs.New(patientUuid)))

    verify(ui).setDate(
        dayOfMonth = "23",
        month = "04",
        twoDigitYear = "18")
  }

  @Test
  fun `when screen is opened for updating an existing BP, then the date should be prefilled with the BP's recorded date`() {
    val recordedAtDate = LocalDate.of(2018, 4, 23)
    val recordedAtDateAsInstant = recordedAtDate.atStartOfDay().toInstant(UTC)
    val existingBp = PatientMocker.bp(recordedAt = recordedAtDateAsInstant)

    whenever(bloodPressureRepository.measurement(existingBp.uuid)).doReturn(Observable.just(existingBp, existingBp))

    uiEvents.onNext(SheetCreated(OpenAs.Update(existingBp.uuid)))

    verify(ui, times(1)).setDate(
        dayOfMonth = "23",
        month = "04",
        twoDigitYear = "18")
  }

  @Suppress("Unused")
  private fun `params for OpenAs types`(): List<Any> {
    return listOf(OpenAs.New(patientUuid), OpenAs.Update(UUID.fromString("d9d0050a-7fa1-4293-868a-7640ebc93cd8")))
  }

  @Test
  fun `whenever the BP sheet is shown to add a new BP, then show today's date`() {
    val today = LocalDate.now(testUserClock)

    uiEvents.onNext(SheetCreated(OpenAs.New(patientUuid)))
    uiEvents.onNext(ScreenChanged(BP_ENTRY))

    verify(ui).showDate(today)

    verify(ui).setDate(
        today.dayOfMonth.toString().padStart(2, '0'),
        today.month.value.toString().padStart(2, '0'),
        today.year.toString().takeLast(2)
    )
    verify(ui).hideRemoveBpButton()
    verify(ui).showEnterNewBloodPressureTitle()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `whenever the BP sheet is shown to update an existing BP, then show the BP date`() {
    val bp = PatientMocker.bp(patientUuid = patientUuid)
    val recordedDate = bp.recordedAt.toLocalDateAtZone(testUserClock.zone)
    whenever(bloodPressureRepository.measurement(any())).doReturn(Observable.just(bp))

    uiEvents.onNext(SheetCreated(OpenAs.Update(bpUuid = bp.uuid)))
    uiEvents.onNext(ScreenChanged(BP_ENTRY))

    verify(ui).showDate(recordedDate)

    verify(ui).setDate(
        recordedDate.dayOfMonth.toString().padStart(2, '0'),
        recordedDate.month.value.toString().padStart(2, '0'),
        recordedDate.year.toString().takeLast(2)
    )
    verify(ui).setSystolic(bp.systolic.toString())
    verify(ui).setDiastolic(bp.diastolic.toString())
    verify(ui).showRemoveBpButton()
    verify(ui).showEditBloodPressureTitle()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `whenever the BP sheet has an invalid date (new BP) and show BP button is pressed, then show date validation errors`() {
    val systolic = 120.toString()
    val diastolic = 110.toString()

    whenever(bpValidator.validate(systolic, diastolic)).doReturn(Success(systolic.toInt(), diastolic.toInt()))
    whenever(dateValidator.validate(eq("01/24/1991"), any())).doReturn(InvalidPattern)

    with(uiEvents) {
      onNext(SheetCreated(OpenAs.New(patientUuid)))
      onNext(ScreenChanged(BP_ENTRY))
      onNext(SystolicChanged(systolic))
      onNext(DiastolicChanged(diastolic))
      onNext(BloodPressureDateClicked)
      onNext(ScreenChanged(DATE_ENTRY))
      onNext(DayChanged("1"))
      onNext(MonthChanged("24"))
      onNext(YearChanged("91"))

      reset(ui)
      onNext(ShowBpClicked)
    }

    verify(ui).showInvalidDateError()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `whenever the BP sheet has an invalid date (new BP) and back key is pressed, then show date validation errors`() {
    val systolic = 120.toString()
    val diastolic = 110.toString()

    whenever(bpValidator.validate(systolic, diastolic)).doReturn(Success(systolic.toInt(), diastolic.toInt()))
    whenever(dateValidator.validate(eq("01/24/1991"), any())).doReturn(InvalidPattern)

    with(uiEvents) {
      onNext(SheetCreated(OpenAs.New(patientUuid)))
      onNext(ScreenChanged(BP_ENTRY))
      onNext(SystolicChanged(systolic))
      onNext(DiastolicChanged(diastolic))
      onNext(BloodPressureDateClicked)
      onNext(ScreenChanged(DATE_ENTRY))
      onNext(DayChanged("1"))
      onNext(MonthChanged("24"))
      onNext(YearChanged("91"))

      reset(ui)
      onNext(BackPressed)
    }

    verify(ui).showInvalidDateError()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the update BP sheet has an invalid date and show BP button is pressed, then show date validation errors`() {
    val systolic = 120.toString()
    val diastolic = 110.toString()

    whenever(bpValidator.validate(systolic, diastolic)).doReturn(Success(systolic.toInt(), diastolic.toInt()))
    whenever(dateValidator.validate(eq("01/24/1991"), any())).doReturn(InvalidPattern)

    val bp = PatientMocker.bp(patientUuid = patientUuid)
    whenever(bloodPressureRepository.measurement(any())).doReturn(Observable.just(bp))

    with(uiEvents) {
      onNext(SheetCreated(OpenAs.Update(bp.uuid)))
      onNext(ScreenChanged(BP_ENTRY))
      onNext(SystolicChanged(systolic))
      onNext(DiastolicChanged(diastolic))
      onNext(BloodPressureDateClicked)
      onNext(ScreenChanged(DATE_ENTRY))
      onNext(DayChanged("1"))
      onNext(MonthChanged("24"))
      onNext(YearChanged("91"))

      reset(ui)
      onNext(ShowBpClicked)
    }

    verify(ui).showInvalidDateError()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the update BP sheet has an invalid date and back key is pressed, then show date validation errors`() {
    val systolic = 120.toString()
    val diastolic = 110.toString()

    whenever(bpValidator.validate(systolic, diastolic)).doReturn(Success(systolic.toInt(), diastolic.toInt()))
    whenever(dateValidator.validate(eq("01/24/1991"), any())).doReturn(InvalidPattern)

    val bp = PatientMocker.bp(patientUuid = patientUuid)
    whenever(bloodPressureRepository.measurement(any())).doReturn(Observable.just(bp))

    with(uiEvents) {
      onNext(SheetCreated(OpenAs.Update(bp.uuid)))
      onNext(ScreenChanged(BP_ENTRY))
      onNext(SystolicChanged(systolic))
      onNext(DiastolicChanged(diastolic))
      onNext(BloodPressureDateClicked)
      onNext(ScreenChanged(DATE_ENTRY))
      onNext(DayChanged("1"))
      onNext(MonthChanged("24"))
      onNext(YearChanged("91"))

      reset(ui)
      onNext(BackPressed)
    }

    verify(ui).showInvalidDateError()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the data entry sheet changes date and back button is pressed, then update date button in BP entry`() {
    val systolic = 120.toString()
    val diastolic = 110.toString()
    val localDate = LocalDate.of(2016, 5, 10)

    whenever(bpValidator.validate(systolic, diastolic)).doReturn(Success(systolic.toInt(), diastolic.toInt()))
    whenever(dateValidator.validate(eq("10/05/1916"), any())).doReturn(Valid(localDate))

    with(uiEvents) {
      onNext(SheetCreated(OpenAs.New(patientUuid)))
      onNext(ScreenChanged(BP_ENTRY))
      onNext(SystolicChanged(systolic))
      onNext(DiastolicChanged(diastolic))
      onNext(BloodPressureDateClicked)
      onNext(ScreenChanged(DATE_ENTRY))
      onNext(DayChanged("10"))
      onNext(MonthChanged("5"))
      onNext(YearChanged("16"))

      reset(ui)
      onNext(BackPressed)
    }

    verify(ui).showDate(localDate)
    verify(ui).showBpEntryScreen()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the data entry sheet changes date and show BP button is pressed, then update date button in BP entry`() {
    val systolic = 120.toString()
    val diastolic = 110.toString()
    val localDate = LocalDate.of(2016, 5, 10)

    whenever(bpValidator.validate(systolic, diastolic)).doReturn(Success(systolic.toInt(), diastolic.toInt()))
    whenever(dateValidator.validate(eq("10/05/1916"), any())).doReturn(Valid(localDate))

    with(uiEvents) {
      onNext(SheetCreated(OpenAs.New(patientUuid)))
      onNext(ScreenChanged(BP_ENTRY))
      onNext(SystolicChanged(systolic))
      onNext(DiastolicChanged(diastolic))
      onNext(BloodPressureDateClicked)
      onNext(ScreenChanged(DATE_ENTRY))
      onNext(DayChanged("10"))
      onNext(MonthChanged("5"))
      onNext(YearChanged("16"))

      reset(ui)
      onNext(ShowBpClicked)
    }

    verify(ui).showDate(localDate)
    verify(ui).showBpEntryScreen()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when done button is clicked in new BP entry, then save BP with entered date immediately`() {
    val systolic = 120.toString()
    val diastolic = 110.toString()
    val inputDate = LocalDate.of(2016, 5, 10)

    whenever(bpValidator.validate(systolic, diastolic)).doReturn(Success(systolic.toInt(), diastolic.toInt()))
    whenever(dateValidator.validate(eq("01/02/1916"), any())).doReturn(Valid(inputDate))
    whenever(bloodPressureRepository.saveMeasurement(any(), any(), any(), any(), any(), any()))
        .doReturn(Single.just(PatientMocker.bp(patientUuid = patientUuid)))
    whenever(appointmentRepository.markAppointmentsCreatedBeforeTodayAsVisited(patientUuid)).doReturn(Completable.complete())
    whenever(patientRepository.compareAndUpdateRecordedAt(any(), any())).doReturn(Completable.complete())

    with(uiEvents) {
      onNext(SheetCreated(OpenAs.New(patientUuid)))
      onNext(ScreenChanged(BP_ENTRY))
      onNext(SystolicChanged(systolic))
      onNext(DiastolicChanged(diastolic))
      onNext(DayChanged("1"))
      onNext(MonthChanged("02"))
      onNext(YearChanged("16"))

      reset(ui)
      onNext(SaveClicked)
    }

    val entryDateAsInstant = inputDate.toUtcInstant(testUserClock)
    verify(bloodPressureRepository).saveMeasurement(
        patientUuid,
        systolic = systolic.toInt(),
        diastolic = diastolic.toInt(),
        loggedInUser = user,
        currentFacility = facility,
        recordedAt = entryDateAsInstant
    )
    verify(appointmentRepository).markAppointmentsCreatedBeforeTodayAsVisited(patientUuid)
    verify(patientRepository).compareAndUpdateRecordedAt(patientUuid, entryDateAsInstant)
    verify(ui).setBpSavedResultAndFinish()
    verify(ui).showDate(inputDate)

    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when done button is clicked in update BP entry, then save BP with entered date immediately`() {
    val systolic = 120.toString()
    val diastolic = 110.toString()
    val createdAt = LocalDate.of(1990, 1, 13).toUtcInstant(testUserClock)
    val existingBp = PatientMocker.bp(
        patientUuid = patientUuid,
        systolic = 9000,
        diastolic = 8999,
        createdAt = createdAt,
        updatedAt = createdAt,
        recordedAt = createdAt
    )

    val newInputDate = LocalDate.of(1991, 2, 14)

    whenever(bpValidator.validate(systolic, diastolic)).doReturn(Success(systolic.toInt(), diastolic.toInt()))
    whenever(dateValidator.validate(eq("01/02/1916"), any())).doReturn(Valid(newInputDate))
    whenever(appointmentRepository.markAppointmentsCreatedBeforeTodayAsVisited(patientUuid)).doReturn(Completable.complete())
    whenever(bloodPressureRepository.measurement(any())).doReturn(Observable.just(existingBp))

    whenever(bloodPressureRepository.updateMeasurement(any())).doReturn(Completable.complete())
    whenever(patientRepository.compareAndUpdateRecordedAt(any(), any())).doReturn(Completable.complete())

    val newUserUuid = UUID.fromString("a726d885-b12d-40b7-9fe1-5391f3fc0f88")
    val newFacilityUuid = UUID.fromString("d51f48fd-40b6-4daf-beae-ba3d9f4c24cd")
    val newUser = user.copy(uuid = newUserUuid)

    whenever(facilityRepository.currentFacility(newUser)).doReturn(Observable.just(facility.copy(uuid = newFacilityUuid)))

    userSubject.onNext(newUser)

    with(uiEvents) {
      onNext(SheetCreated(OpenAs.Update(existingBp.uuid)))
      onNext(ScreenChanged(BP_ENTRY))
      onNext(SystolicChanged(systolic))
      onNext(DiastolicChanged(diastolic))
      onNext(DayChanged("1"))
      onNext(MonthChanged("02"))
      onNext(YearChanged("16"))

      reset(ui)
      onNext(SaveClicked)
    }

    val entryDateAsInstant = newInputDate.toUtcInstant(testUserClock)
    val newInputDateAsInstant = newInputDate.toUtcInstant(testUserClock)
    val updatedBp = existingBp.copy(
        systolic = systolic.toInt(),
        diastolic = diastolic.toInt(),
        updatedAt = createdAt,
        recordedAt = newInputDateAsInstant,
        userUuid = newUserUuid,
        facilityUuid = newFacilityUuid
    )
    verify(bloodPressureRepository).updateMeasurement(updatedBp)

    verify(bloodPressureRepository, never()).saveMeasurement(any(), any(), any(), any(), any(), any())
    verify(appointmentRepository, never()).markAppointmentsCreatedBeforeTodayAsVisited(any())

    verify(patientRepository).compareAndUpdateRecordedAt(patientUuid, entryDateAsInstant)
    verify(ui).showDate(newInputDate)
    verify(ui).setBpSavedResultAndFinish()

    verifyNoMoreInteractions(ui)
  }
}
