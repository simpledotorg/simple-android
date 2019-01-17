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
import org.simple.clinic.bp.BloodPressureConfig
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.bp.entry.ScreenType.BP_ENTRY
import org.simple.clinic.bp.entry.ScreenType.DATE_ENTRY
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.exhaustive
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthFormatValidator
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthFormatValidator.Result2.Invalid.DateIsInFuture
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthFormatValidator.Result2.Invalid.InvalidPattern
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthFormatValidator.Result2.Valid
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneOffset.UTC
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class BloodPressureEntrySheetControllerTestV2 {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val sheet = mock<BloodPressureEntrySheet>()
  private val bloodPressureRepository = mock<BloodPressureRepository>()
  private val patientUuid = UUID.randomUUID()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private val configEmitter = PublishSubject.create<BloodPressureConfig>()
  private lateinit var controller: BloodPressureEntrySheetControllerV2

  private val dateValidator = mock<DateOfBirthFormatValidator>()

  @Before
  fun setUp() {
    RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }

    controller = BloodPressureEntrySheetControllerV2(
        bloodPressureRepository = bloodPressureRepository,
        configProvider = configEmitter.firstOrError(),
        dateValidator = dateValidator)

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

    when (shouldMove) {
      true -> verify(sheet).changeFocusToDiastolic()
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
  fun `when systolic is less than diastolic, show error`() {
    uiEvents.onNext(BloodPressureEntrySheetCreated(OpenAs.New(patientUuid)))
    uiEvents.onNext(BloodPressureSystolicTextChanged("90"))
    uiEvents.onNext(BloodPressureDiastolicTextChanged("140"))
    uiEvents.onNext(BloodPressureSaveClicked)

    verify(bloodPressureRepository, never()).saveMeasurement(any(), any(), any(), any())
    verify(sheet).showSystolicLessThanDiastolicError()
  }

  @Test
  fun `when systolic is less than minimum possible, show error`() {
    uiEvents.onNext(BloodPressureEntrySheetCreated(OpenAs.New(patientUuid)))
    uiEvents.onNext(BloodPressureSystolicTextChanged("55"))
    uiEvents.onNext(BloodPressureDiastolicTextChanged("55"))
    uiEvents.onNext(BloodPressureSaveClicked)

    verify(bloodPressureRepository, never()).saveMeasurement(any(), any(), any(), any())
    verify(sheet).showSystolicLowError()
  }

  @Test
  fun `when systolic is more than maximum possible, show error`() {
    uiEvents.onNext(BloodPressureEntrySheetCreated(OpenAs.New(patientUuid)))
    uiEvents.onNext(BloodPressureSystolicTextChanged("333"))
    uiEvents.onNext(BloodPressureDiastolicTextChanged("88"))
    uiEvents.onNext(BloodPressureSaveClicked)

    verify(bloodPressureRepository, never()).saveMeasurement(any(), any(), any(), any())
    verify(sheet).showSystolicHighError()
  }

  @Test
  fun `when diastolic is less than minimum possible, show error`() {
    uiEvents.onNext(BloodPressureEntrySheetCreated(OpenAs.New(patientUuid)))
    uiEvents.onNext(BloodPressureSystolicTextChanged("110"))
    uiEvents.onNext(BloodPressureDiastolicTextChanged("33"))
    uiEvents.onNext(BloodPressureSaveClicked)

    verify(bloodPressureRepository, never()).saveMeasurement(any(), any(), any(), any())
    verify(sheet).showDiastolicLowError()
  }

  @Test
  fun `when diastolic is more than maximum possible, show error`() {
    uiEvents.onNext(BloodPressureEntrySheetCreated(OpenAs.New(patientUuid)))
    uiEvents.onNext(BloodPressureSystolicTextChanged("233"))
    uiEvents.onNext(BloodPressureDiastolicTextChanged("190"))
    uiEvents.onNext(BloodPressureSaveClicked)

    verify(bloodPressureRepository, never()).saveMeasurement(any(), any(), any(), any())
    verify(sheet).showDiastolicHighError()
  }

  @Test
  fun `when systolic is empty, show error`() {
    uiEvents.onNext(BloodPressureEntrySheetCreated(OpenAs.New(patientUuid)))
    uiEvents.onNext(BloodPressureSystolicTextChanged(""))
    uiEvents.onNext(BloodPressureDiastolicTextChanged("190"))
    uiEvents.onNext(BloodPressureSaveClicked)

    verify(bloodPressureRepository, never()).saveMeasurement(any(), any(), any(), any())
    verify(sheet).showSystolicEmptyError()
  }

  @Test
  fun `when diastolic is empty, show error`() {
    uiEvents.onNext(BloodPressureEntrySheetCreated(OpenAs.New(patientUuid)))
    uiEvents.onNext(BloodPressureSystolicTextChanged("120"))
    uiEvents.onNext(BloodPressureDiastolicTextChanged(""))
    uiEvents.onNext(BloodPressureSaveClicked)

    verify(bloodPressureRepository, never()).saveMeasurement(any(), any(), any(), any())
    verify(sheet).showDiastolicEmptyError()
  }

  @Test
  fun `when systolic or diastolic values change, hide the error message`() {
    uiEvents.onNext(BloodPressureEntrySheetCreated(OpenAs.New(patientUuid)))
    uiEvents.onNext(BloodPressureSystolicTextChanged("12"))
    uiEvents.onNext(BloodPressureSystolicTextChanged("120"))
    uiEvents.onNext(BloodPressureSystolicTextChanged("130"))
    uiEvents.onNext(BloodPressureDiastolicTextChanged("90"))
    uiEvents.onNext(BloodPressureDiastolicTextChanged("99"))

    verify(sheet, times(5)).hideErrorMessage()
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
  fun `when save is clicked, BP entry is active and input is valid then show date entry`(openAs: OpenAs) {
    if (openAs is OpenAs.Update) {
      whenever(bloodPressureRepository.measurement(any())).thenReturn(Observable.never())
    }

    uiEvents.onNext(BloodPressureEntrySheetCreated(openAs))
    uiEvents.onNext(BloodPressureSystolicTextChanged("142"))
    uiEvents.onNext(BloodPressureDiastolicTextChanged("80"))
    uiEvents.onNext(BloodPressureScreenChanged(BP_ENTRY))
    uiEvents.onNext(BloodPressureSaveClicked)

    verify(bloodPressureRepository, never()).saveMeasurement(any(), any(), any(), any())
    verify(bloodPressureRepository, never()).updateMeasurement(any())
    verify(sheet).showDateEntryScreen()
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

  // TODO: Remove this test when feature flag is lifted.
  @Test
  @Parameters(method = "params for enabling delete bp feature")
  fun `when the delete blood pressure feature flag is enabled and the sheet is opened for update, the remove BP button must be shown`(
      featureEnabled: Boolean,
      openAs: OpenAs,
      shouldShowRemoveBpButton: Boolean
  ) {
    configEmitter.onNext(BloodPressureConfig(deleteBloodPressureFeatureEnabled = featureEnabled, dateEntryEnabled = false))
    whenever(bloodPressureRepository.measurement(any())).thenReturn(Observable.just(PatientMocker.bp()))

    uiEvents.onNext(BloodPressureEntrySheetCreated(openAs))

    if (shouldShowRemoveBpButton) {
      verify(sheet).showRemoveBpButton()
    } else {
      verify(sheet).hideRemoveBpButton()
    }
  }

  @Suppress("Unused")
  private fun `params for enabling delete bp feature`(): List<List<Any>> {
    return listOf(
        listOf(true, OpenAs.New(UUID.randomUUID()), false),
        listOf(true, OpenAs.Update(UUID.randomUUID()), true),
        listOf(false, OpenAs.New(UUID.randomUUID()), false),
        listOf(false, OpenAs.Update(UUID.randomUUID()), false))
  }

  @Test
  @Parameters(method = "params for showing remove button")
  fun `the remove BP button must be shown when the sheet is opened for update`(
      openAs: OpenAs,
      shouldShowRemoveBpButton: Boolean
  ) {
    configEmitter.onNext(BloodPressureConfig(deleteBloodPressureFeatureEnabled = true, dateEntryEnabled = false))
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
      result: DateOfBirthFormatValidator.Result2
  ) {
    whenever(bloodPressureRepository.measurement(any())).thenReturn(Observable.never())
    whenever(dateValidator.validate2("dummy/dummy/dummy")).thenReturn(result)

    uiEvents.onNext(BloodPressureEntrySheetCreated(openAs))
    uiEvents.onNext(BloodPressureScreenChanged(DATE_ENTRY))
    uiEvents.onNext(BloodPressureDayChanged("dummy"))
    uiEvents.onNext(BloodPressureMonthChanged("dummy"))
    uiEvents.onNext(BloodPressureYearChanged("dummy"))
    uiEvents.onNext(BloodPressureSaveClicked)

    when (openAs) {
      is OpenAs.New -> verify(bloodPressureRepository, never()).saveMeasurement(any(), any(), any(), any())
      is OpenAs.Update -> verify(bloodPressureRepository, never()).updateMeasurement(any())
    }.exhaustive()

    verify(sheet, never()).setBpSavedResultAndFinish()
    verify(dateValidator, times(5)).validate2("dummy/dummy/dummy")
  }

  /**
   * TODO: Consider hardcoding all values instead of logic.
   */
  @Suppress("Unused")
  private fun `params for checking valid date input`(): List<Any> {
    return listOf(
        listOf(OpenAs.New(patientUuid), InvalidPattern),
        listOf(OpenAs.New(patientUuid), DateIsInFuture),
        listOf(OpenAs.Update(UUID.randomUUID()), InvalidPattern),
        listOf(OpenAs.Update(UUID.randomUUID()), DateIsInFuture))

    //    return DateOfBirthFormatValidator.Result
    //        .values()
    //        .filter { it != DateOfBirthFormatValidator.Result.VALID }
    //        .flatMap { result ->
    //          listOf(OpenAs.New(patientUuid), result) + listOf(OpenAs.Update(UUID.randomUUID()), result)
    //        }
  }

  @Test
  fun `when save is clicked for a new BP, date entry is active and input is valid then a BP measurement should be saved`() {
    val inputDate = LocalDate.of(1990, 1, 13)
    whenever(dateValidator.validate2(any(), any())).thenReturn(Valid(inputDate))
    whenever(bloodPressureRepository.saveMeasurement(any(), any(), any(), any())).thenReturn(Single.just(PatientMocker.bp()))

    uiEvents.run {
      onNext(BloodPressureEntrySheetCreated(openAs = OpenAs.New(patientUuid)))
      onNext(BloodPressureSystolicTextChanged("120"))
      onNext(BloodPressureDiastolicTextChanged("110"))
      onNext(BloodPressureScreenChanged(DATE_ENTRY))
      onNext(BloodPressureDayChanged("13"))
      onNext(BloodPressureMonthChanged("01"))
      onNext(BloodPressureYearChanged("1990"))
      onNext(BloodPressureSaveClicked)
    }

    val entryDateAsInstant = inputDate.atStartOfDay(UTC).toInstant()

    verify(bloodPressureRepository, never()).updateMeasurement(any())

    verify(bloodPressureRepository).saveMeasurement(
        patientUuid,
        systolic = 120,
        diastolic = 110,
        createdAt = entryDateAsInstant)
    verify(sheet).setBpSavedResultAndFinish()
  }

  @Test
  fun `when save is clicked while updating a BP, date entry is active and input is valid then the updated BP measurement should be saved`() {
    val oldCreatedAt = LocalDate.of(1990, 1, 13).atStartOfDay(UTC).toInstant()
    val existingBp = PatientMocker.bp(systolic = 9000, diastolic = 8999, createdAt = oldCreatedAt, updatedAt = oldCreatedAt)

    val newInputDate = LocalDate.of(1991, 2, 14)
    whenever(dateValidator.validate2(any(), any())).thenReturn(Valid(newInputDate))
    whenever(bloodPressureRepository.saveMeasurement(any(), any(), any(), any())).thenReturn(Single.just(PatientMocker.bp()))
    whenever(bloodPressureRepository.measurement(existingBp.uuid)).thenReturn(Observable.just(existingBp))
    whenever(bloodPressureRepository.updateMeasurement(any())).thenReturn(Completable.complete())

    uiEvents.run {
      onNext(BloodPressureEntrySheetCreated(openAs = OpenAs.Update(existingBp.uuid)))
      onNext(BloodPressureSystolicTextChanged("120"))
      onNext(BloodPressureDiastolicTextChanged("110"))
      onNext(BloodPressureScreenChanged(DATE_ENTRY))
      onNext(BloodPressureDayChanged("14"))
      onNext(BloodPressureMonthChanged("02"))
      onNext(BloodPressureYearChanged("1991"))
      onNext(BloodPressureSaveClicked)
    }

    val newInputDateAsInstant = newInputDate.atStartOfDay(UTC).toInstant()
    val updatedBp = existingBp.copy(systolic = 120, diastolic = 110, createdAt = newInputDateAsInstant, updatedAt = newInputDateAsInstant)
    verify(bloodPressureRepository).updateMeasurement(updatedBp)

    verify(bloodPressureRepository, never()).saveMeasurement(any(), any(), any(), any())
    verify(sheet).setBpSavedResultAndFinish()
  }

  @Test
  @Suppress("IMPLICIT_CAST_TO_ANY")
  @Parameters(method = "params for showing date validation errors")
  fun `when save is clicked, date entry is active and input is invalid then validation errors should be shown`(
      openAs: OpenAs,
      errorResult: DateOfBirthFormatValidator.Result2,
      errorUiChangeVerifications: UiChange
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

    errorUiChangeVerifications(sheet)
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
  fun `when BP entry is active, BP readings are valid and next arrow is pressed then date entry should be shown`() {
    // TODO
  }

  @Test
  fun `when BP entry is active, BP readings are invalid and next arrow is pressed then date entry should not be shown`() {
    // TODO
  }

  @Test
  fun `when BP entry is active and previous arrow is pressed then date entry should be shown`() {
    // TODO
  }

  @Test
  fun `when date entry is active and back is pressed then BP entry should be shown`() {
    // TODO
  }

  @Test
  fun `when screen is opened for a new BP, then the date should be prefilled with the current date`() {
    // TODO
  }
}
