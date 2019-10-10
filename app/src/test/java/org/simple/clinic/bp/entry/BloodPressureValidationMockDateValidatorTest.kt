package org.simple.clinic.bp.entry

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.bp.entry.BloodPressureEntrySheetControllerTest.InvalidDateTestParams
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
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Invalid.DateIsInFuture
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Invalid.InvalidPattern
import org.threeten.bp.LocalDate
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class BloodPressureValidationMockDateValidatorTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val ui = mock<BloodPressureEntryUi>()
  private val bloodPressureRepository = mock<BloodPressureRepository>()
  private val appointmentRepository = mock<AppointmentRepository>()
  private val patientRepository = mock<PatientRepository>()
  private val dateValidator = mock<UserInputDateValidator>()
  private val bpValidator = BpValidator()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private val patientUuid = UUID.fromString("79145baf-7a5c-4442-ab30-2da564a32944")

  private val testUserClock = TestUserClock()
  private val testUtcClock = TestUtcClock()
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

  // TODO(rj): 2019-10-10 There isn't a straight-forward way to set a time in the date validator as of now. We'll rely on mocks to test this code path (showDateIsInFutureError) for now.
  @Test
  @Suppress("IMPLICIT_CAST_TO_ANY")
  @Parameters(method = "params for showing date validation errors")
  fun `when save is clicked, date entry is active and input is invalid then validation errors should be shown`(
      testParams: InvalidDateTestParams
  ) {
    val (openAs, day, month, year, errorResult, uiChangeVerification) = testParams

    whenever(dateValidator.validate(any(), any())).doReturn(errorResult)
    whenever(bloodPressureRepository.measurement(any())).doReturn(Observable.never())

    uiEvents.run {
      onNext(SheetCreated(openAs = openAs))
      onNext(ScreenChanged(BloodPressureEntrySheet.ScreenType.DATE_ENTRY))
      onNext(DayChanged(day))
      onNext(MonthChanged(month))
      onNext(YearChanged(year.takeLast(2)))
      onNext(SaveClicked)
    }

    verify(bloodPressureRepository, never()).saveMeasurement(any(), any(), any(), any(), any(), any())
    verify(bloodPressureRepository, never()).updateMeasurement(any())
    verify(ui, never()).setBpSavedResultAndFinish()

    uiChangeVerification(ui)
  }

  @Suppress("unused")
  fun `params for showing date validation errors`(): List<InvalidDateTestParams> {
    val existingBpUuid = UUID.fromString("2c4eccbb-d1bc-4c7c-b1ec-60a13acfeea4")
    return listOf(
        InvalidDateTestParams(OpenAs.New(patientUuid), "01", "01", "2099", DateIsInFuture) { ui: Ui -> verify(ui).showDateIsInFutureError() },
        InvalidDateTestParams(OpenAs.Update(existingBpUuid), "01", "01", "2099", DateIsInFuture) { ui: Ui -> verify(ui).showDateIsInFutureError() })
  }

  @Test
  @Suppress("IMPLICIT_CAST_TO_ANY")
  @Parameters(method = "params for checking valid date input")
  fun `when save is clicked, date entry is active, but input is invalid then BP measurement should not be saved`(
      testParams: DoNotSaveBpWithInvalidDateTestParams
  ) {
    val (openAs, result) = testParams

    whenever(dateValidator.validate(any(), any())).doReturn(result)
    whenever(bloodPressureRepository.measurement(any())).doReturn(Observable.never())

    uiEvents.onNext(SheetCreated(openAs))
    uiEvents.onNext(ScreenChanged(BloodPressureEntrySheet.ScreenType.DATE_ENTRY))
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
  private fun `params for checking valid date input`(): List<DoNotSaveBpWithInvalidDateTestParams> {
    return listOf(
        DoNotSaveBpWithInvalidDateTestParams(OpenAs.New(patientUuid), InvalidPattern),
        DoNotSaveBpWithInvalidDateTestParams(OpenAs.New(patientUuid), DateIsInFuture),
        DoNotSaveBpWithInvalidDateTestParams(OpenAs.Update(UUID.fromString("f6f27cad-8b82-461e-8b1e-e14c2ac63832")), InvalidPattern),
        DoNotSaveBpWithInvalidDateTestParams(OpenAs.Update(UUID.fromString("3b082f5e-8e0e-4aa7-a0cb-2d99ab020a30")), DateIsInFuture))
  }

  data class DoNotSaveBpWithInvalidDateTestParams(
      val openAs: OpenAs,
      val result: Result
  )
}