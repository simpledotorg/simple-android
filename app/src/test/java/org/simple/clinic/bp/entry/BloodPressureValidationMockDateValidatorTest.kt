package org.simple.clinic.bp.entry

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.TestData
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.bp.entry.BloodPressureEntrySheetLogicTest.InvalidDateTestParams
import org.simple.clinic.bp.entry.OpenAs.New
import org.simple.clinic.bp.entry.OpenAs.Update
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.user.User
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.util.UserInputDatePaddingCharacter
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.uuid.FakeUuidGenerator
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Invalid.DateIsInFuture
import org.simple.mobius.migration.MobiusTestFixture
import java.time.LocalDate
import java.time.ZoneOffset
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

  private val uiEvents = PublishSubject.create<UiEvent>()
  private val patientUuid = UUID.fromString("79145baf-7a5c-4442-ab30-2da564a32944")

  private val testUserClock = TestUserClock()
  private val testUtcClock = TestUtcClock()

  private val facility = TestData.facility(uuid = UUID.fromString("2a70f82e-92c6-4fce-b60e-6f083a8e725b"))
  private val user = TestData.loggedInUser(
      uuid = UUID.fromString("1367a583-12b1-48c6-ae9d-fb34f9aac449"),
      currentFacilityUuid = facility.uuid,
      registrationFacilityUuid = facility.uuid
  )
  private val userSubject = PublishSubject.create<User>()

  private val existingBpUuid = UUID.fromString("2c4eccbb-d1bc-4c7c-b1ec-60a13acfeea4")

  private val uiRenderer = BloodPressureEntryUiRenderer(ui)
  private lateinit var fixture: MobiusTestFixture<BloodPressureEntryModel, BloodPressureEntryEvent, BloodPressureEntryEffect>

  @Before
  fun setUp() {
    whenever(dateValidator.dateInUserTimeZone()).doReturn(LocalDate.now(testUtcClock))

    userSubject.onNext(user)
  }

  // TODO(rj): 2019-10-10 There isn't a straight-forward way to set a time in the date validator as of now.
  // TODO      We'll rely on mocks to test this code path (showDateIsInFutureError) for now.
  @Test
  @Suppress("IMPLICIT_CAST_TO_ANY")
  @Parameters(method = "params for showing date validation errors")
  fun `when save is clicked, date entry is active and input is invalid then validation errors should be shown`(
      testParams: InvalidDateTestParams
  ) {
    val (openAs, day, month, year, errorResult, uiChangeVerification) = testParams

    whenever(dateValidator.validate(any(), any())).doReturn(errorResult)
    whenever(bloodPressureRepository.measurement(any())).doReturn(Observable.never())

    sheetCreated(openAs)
    uiEvents.run {
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
    return listOf(
        InvalidDateTestParams(New(patientUuid), "01", "01", "2099", DateIsInFuture) {
          ui: Ui -> verify(ui).showDateIsInFutureError()
        },

        InvalidDateTestParams(Update(existingBpUuid), "01", "01", "2099", DateIsInFuture) {
          ui: Ui -> verify(ui).showDateIsInFutureError()
        }
    )
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

    sheetCreated(openAs)
    uiEvents.onNext(ScreenChanged(BloodPressureEntrySheet.ScreenType.DATE_ENTRY))
    uiEvents.onNext(DayChanged("1"))
    uiEvents.onNext(MonthChanged("4"))
    uiEvents.onNext(YearChanged("9"))
    uiEvents.onNext(SaveClicked)

    when (openAs) {
      is New -> verify(bloodPressureRepository, never()).saveMeasurement(any(), any(), any(), any(), any(), any())
      is Update -> verify(bloodPressureRepository, never()).updateMeasurement(any())
      else -> throw AssertionError()
    }

    verify(ui).showDateIsInFutureError()
    verify(ui, never()).setBpSavedResultAndFinish()
  }

  @Suppress("Unused")
  private fun `params for checking valid date input`(): List<DoNotSaveBpWithInvalidDateTestParams> {
    return listOf(
        DoNotSaveBpWithInvalidDateTestParams(New(patientUuid), DateIsInFuture),
        DoNotSaveBpWithInvalidDateTestParams(Update(existingBpUuid), DateIsInFuture))
  }

  data class DoNotSaveBpWithInvalidDateTestParams(
      val openAs: OpenAs,
      val result: Result
  )

  private fun sheetCreatedForNew(patientUuid: UUID) {
    val openAsNew = New(patientUuid)
    instantiateFixture(openAsNew)
  }

  private fun sheetCreatedForUpdate(existingBpUuid: UUID) {
    val openAsUpdate = Update(existingBpUuid)
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
    val effectHandler = BloodPressureEntryEffectHandler(
        ui = ui,
        patientRepository = patientRepository,
        bloodPressureRepository = bloodPressureRepository,
        appointmentsRepository = appointmentRepository,
        userClock = testUserClock,
        schedulersProvider = TestSchedulersProvider.trampoline(),
        uuidGenerator = FakeUuidGenerator.fixed(UUID.fromString("7283abf4-b718-4379-b101-46f011b5536b")),
        currentUser = { user },
        currentFacility = { facility }
    ).build()

    fixture = MobiusTestFixture(
        uiEvents.ofType(),
        BloodPressureEntryModel.create(openAs, LocalDate.now(testUserClock).year),
        BloodPressureEntryInit(),
        BloodPressureEntryUpdate(dateValidator, LocalDate.now(ZoneOffset.UTC), UserInputDatePaddingCharacter.ZERO),
        effectHandler,
        uiRenderer::render
    ).also { it.start() }
  }
}
