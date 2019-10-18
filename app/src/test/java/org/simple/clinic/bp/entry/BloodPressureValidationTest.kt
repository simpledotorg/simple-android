package org.simple.clinic.bp.entry

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.rxkotlin.ofType
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.bp.entry.BloodPressureEntrySheet.ScreenType.BP_ENTRY
import org.simple.clinic.bp.entry.BpValidator.Validation
import org.simple.clinic.bp.entry.BpValidator.Validation.ErrorDiastolicEmpty
import org.simple.clinic.bp.entry.BpValidator.Validation.ErrorDiastolicTooHigh
import org.simple.clinic.bp.entry.BpValidator.Validation.ErrorDiastolicTooLow
import org.simple.clinic.bp.entry.BpValidator.Validation.ErrorSystolicEmpty
import org.simple.clinic.bp.entry.BpValidator.Validation.ErrorSystolicLessThanDiastolic
import org.simple.clinic.bp.entry.BpValidator.Validation.ErrorSystolicTooHigh
import org.simple.clinic.bp.entry.BpValidator.Validation.ErrorSystolicTooLow
import org.simple.clinic.bp.entry.OpenAs.New
import org.simple.clinic.bp.entry.OpenAs.Update
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.UserInputDatePaddingCharacter
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.simple.mobius.migration.MobiusTestFixture
import org.threeten.bp.ZoneOffset
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class BloodPressureValidationTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val ui = mock<BloodPressureEntryUi>()
  private val bloodPressureRepository = mock<BloodPressureRepository>()
  private val appointmentRepository = mock<AppointmentRepository>()
  private val patientRepository = mock<PatientRepository>()
  private val dateValidator = UserInputDateValidator(ZoneOffset.UTC, DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH))
  private val bpValidator = BpValidator()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private val patientUuid = UUID.fromString("79145baf-7a5c-4442-ab30-2da564a32944")

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
      inputDatePaddingCharacter = UserInputDatePaddingCharacter.ZERO,
      userSession = userSession,
      facilityRepository = facilityRepository)

  private val viewRenderer = BloodPressureEntryViewRenderer(ui)
  private lateinit var fixture: MobiusTestFixture<BloodPressureEntryModel, BloodPressureEntryEvent, BloodPressureEntryEffect>

  @Before
  fun setUp() {
    RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }

    whenever(userSession.requireLoggedInUser()).doReturn(userSubject)
    whenever(facilityRepository.currentFacility(user)).doReturn(Observable.just(facility))

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(ui) }

    userSubject.onNext(user)
  }

  @Test
  @Parameters(method = "params for bp validation errors and expected ui changes")
  fun `when BP entry is active, and BP readings are invalid then show error`(
      testParams: ValidationErrorsAndUiChangesTestParams
  ) {
    val (systolic, diastolic, error, uiChangeVerification) = testParams

    // This assertion is not necessary, it was added to stabilize this test for refactoring.
    assertThat(bpValidator.validate(systolic, diastolic))
        .isEqualTo(error)

    sheetCreatedForNew(patientUuid)
    uiEvents.onNext(ScreenChanged(BP_ENTRY))
    uiEvents.onNext(SystolicChanged(systolic))
    uiEvents.onNext(DiastolicChanged(diastolic))
    uiEvents.onNext(SaveClicked)

    verify(bloodPressureRepository, never()).saveMeasurement(any(), any(), any(), any(), any(), any())
    verify(bloodPressureRepository, never()).updateMeasurement(any())

    uiChangeVerification(ui)
  }

  @Suppress("unused")
  fun `params for bp validation errors and expected ui changes`(): List<ValidationErrorsAndUiChangesTestParams> {
    return listOf(
        ValidationErrorsAndUiChangesTestParams("", "80", ErrorSystolicEmpty) { ui: Ui -> verify(ui).showSystolicEmptyError() },
        ValidationErrorsAndUiChangesTestParams("120", "", ErrorDiastolicEmpty) { ui: Ui -> verify(ui).showDiastolicEmptyError() },
        ValidationErrorsAndUiChangesTestParams("999", "80", ErrorSystolicTooHigh) { ui: Ui -> verify(ui).showSystolicHighError() },
        ValidationErrorsAndUiChangesTestParams("0", "80", ErrorSystolicTooLow) { ui: Ui -> verify(ui).showSystolicLowError() },
        ValidationErrorsAndUiChangesTestParams("120", "999", ErrorDiastolicTooHigh) { ui: Ui -> verify(ui).showDiastolicHighError() },
        ValidationErrorsAndUiChangesTestParams("120", "0", ErrorDiastolicTooLow) { ui: Ui -> verify(ui).showDiastolicLowError() },
        ValidationErrorsAndUiChangesTestParams("120", "121", ErrorSystolicLessThanDiastolic) { ui: Ui -> verify(ui).showSystolicLessThanDiastolicError() }
    )
  }

  data class ValidationErrorsAndUiChangesTestParams(
      val systolic: String,
      val diastolic: String,
      val error: Validation,
      val uiChangeVerification: (Ui) -> Unit
  )

  @Test
  @Parameters(method = "params for OpenAs and bp validation errors")
  fun `when BP entry is active, BP readings are invalid and next arrow is pressed then date entry should not be shown`(
      testParams: ValidationErrorsAndDoNotGoToDateEntryParams
  ) {
    val (openAs, systolic, diastolic, error) = testParams

    // This assertion is not necessary, it was added to stabilize this test for refactoring.
    assertThat(bpValidator.validate(systolic, diastolic))
        .isEqualTo(error)

    whenever(bloodPressureRepository.measurement(any())).doReturn(Observable.never())

    sheetCreated(openAs)
    uiEvents.run {
      onNext(ScreenChanged(BP_ENTRY))
      onNext(SystolicChanged(systolic))
      onNext(DiastolicChanged(diastolic))
      onNext(BloodPressureDateClicked)
    }

    verify(ui, never()).showDateEntryScreen()
  }

  @Suppress("unused")
  fun `params for OpenAs and bp validation errors`(): List<ValidationErrorsAndDoNotGoToDateEntryParams> {
    val bpUuid = UUID.fromString("99fed5e5-19a8-4ece-9d07-6beab70ee77c")
    return listOf(
        ValidationErrorsAndDoNotGoToDateEntryParams(New(patientUuid), "", "80", ErrorSystolicEmpty),
        ValidationErrorsAndDoNotGoToDateEntryParams(New(patientUuid), "120", "", ErrorDiastolicEmpty),
        ValidationErrorsAndDoNotGoToDateEntryParams(New(patientUuid), "999", "80", ErrorSystolicTooHigh),
        ValidationErrorsAndDoNotGoToDateEntryParams(New(patientUuid), "0", "80", ErrorSystolicTooLow),
        ValidationErrorsAndDoNotGoToDateEntryParams(New(patientUuid), "120", "999", ErrorDiastolicTooHigh),
        ValidationErrorsAndDoNotGoToDateEntryParams(New(patientUuid), "120", "0", ErrorDiastolicTooLow),
        ValidationErrorsAndDoNotGoToDateEntryParams(New(patientUuid), "120", "140", ErrorSystolicLessThanDiastolic),

        ValidationErrorsAndDoNotGoToDateEntryParams(Update(bpUuid), "", "80", ErrorSystolicEmpty),
        ValidationErrorsAndDoNotGoToDateEntryParams(Update(bpUuid), "120", "", ErrorDiastolicEmpty),
        ValidationErrorsAndDoNotGoToDateEntryParams(Update(bpUuid), "999", "80", ErrorSystolicTooHigh),
        ValidationErrorsAndDoNotGoToDateEntryParams(Update(bpUuid), "0", "80", ErrorSystolicTooLow),
        ValidationErrorsAndDoNotGoToDateEntryParams(Update(bpUuid), "120", "999", ErrorDiastolicTooHigh),
        ValidationErrorsAndDoNotGoToDateEntryParams(Update(bpUuid), "120", "0", ErrorDiastolicTooLow),
        ValidationErrorsAndDoNotGoToDateEntryParams(Update(bpUuid), "120", "140", ErrorSystolicLessThanDiastolic))
  }

  data class ValidationErrorsAndDoNotGoToDateEntryParams(
      val openAs: OpenAs,
      val systolic: String,
      val diastolic: String,
      val error: Validation
  )

  private fun sheetCreatedForNew(patientUuid: UUID) {
    val openAsNew = New(patientUuid)
    uiEvents.onNext(SheetCreated(openAsNew))
    instantiateFixture(openAsNew)
  }

  private fun sheetCreatedForUpdate(existingBpUuid: UUID) {
    val openAsUpdate = Update(existingBpUuid)
    uiEvents.onNext(SheetCreated(openAsUpdate))
    instantiateFixture(openAsUpdate)
  }

  private fun sheetCreated(openAs: OpenAs) {
    when (openAs) {
      is New -> sheetCreatedForNew(openAs.patientUuid)
      is Update -> sheetCreatedForUpdate(openAs.bpUuid)
      else -> throw IllegalStateException("Unknown `openAs`: $openAs")
    }
  }

  private fun instantiateFixture(openAs: OpenAs) {
    val effectHandler = BloodPressureEntryEffectHandler.create(
        ui,
        testUserClock,
        UserInputDatePaddingCharacter.ZERO,
        bloodPressureRepository,
        TrampolineSchedulersProvider()
    )
    val defaultModel = when (openAs) {
      is New -> BloodPressureEntryModel.newBloodPressureEntry(openAs)
      is Update -> BloodPressureEntryModel.updateBloodPressureEntry(openAs)
    }

    fixture = MobiusTestFixture(
        uiEvents.ofType(),
        defaultModel,
        BloodPressureEntryInit(),
        BloodPressureEntryUpdate(bpValidator),
        effectHandler,
        viewRenderer::render
    ).also { it.start() }
  }
}
