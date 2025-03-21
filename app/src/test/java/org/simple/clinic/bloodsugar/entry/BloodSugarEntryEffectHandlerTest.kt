package org.simple.clinic.bloodsugar.entry

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.simple.clinic.bloodsugar.BloodSugarReading
import org.simple.clinic.bloodsugar.BloodSugarRepository
import org.simple.clinic.bloodsugar.BloodSugarUnitPreference
import org.simple.clinic.bloodsugar.HbA1c
import org.simple.clinic.bloodsugar.PostPrandial
import org.simple.clinic.bloodsugar.Random
import org.simple.clinic.bloodsugar.entry.ValidationResult.ErrorBloodSugarEmpty
import org.simple.clinic.bloodsugar.entry.ValidationResult.ErrorBloodSugarTooHigh
import org.simple.clinic.bloodsugar.entry.ValidationResult.ErrorBloodSugarTooLow
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.SyncStatus.DONE
import org.simple.clinic.storage.Timestamps
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.util.toUtcInstant
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Invalid.DateIsInFuture
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Invalid.InvalidPattern
import org.simple.clinic.TestData
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.uuid.FakeUuidGenerator
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class BloodSugarEntryEffectHandlerTest {

  private val uiActions = mock<BloodSugarEntryUiActions>()
  private val userClock = TestUserClock()

  private val appointmentRepository = mock<AppointmentRepository>()
  private val patientRepository = mock<PatientRepository>()
  private val bloodSugarRepository = mock<BloodSugarRepository>()

  private val user = TestData.loggedInUser(uuid = UUID.fromString("4844b826-a162-49fe-b92c-962da172e86c"))
  private val facility = TestData.facility(uuid = UUID.fromString("7fabe36b-8fc3-457d-b9a8-68df71def7bd"))
  private val measurementUuid = UUID.fromString("175fb078-87b1-4c01-b5f0-91c3d2cefdfd")
  private val bloodSugarUnitPreference = mock<Preference<BloodSugarUnitPreference>>()
  private val bloodSugarUnitPreferenceSelection = BloodSugarUnitPreference.Mg

  private val viewEffectHandler = BloodSugarEntryViewEffectHandler(uiActions)
  private val effectHandler = BloodSugarEntryEffectHandler(
      bloodSugarRepository = bloodSugarRepository,
      patientRepository = patientRepository,
      appointmentsRepository = appointmentRepository,
      userClock = userClock,
      schedulersProvider = TestSchedulersProvider.trampoline(),
      currentUser = { user },
      currentFacility = { facility },
      uuidGenerator = FakeUuidGenerator.fixed(measurementUuid),
      bloodSugarUnitPreference = bloodSugarUnitPreference,
      viewEffectsConsumer = viewEffectHandler::handle
  ).build()
  private val testCase = EffectHandlerTestCase(effectHandler)

  @Before
  fun setup() {
    whenever(bloodSugarUnitPreference.get()) doReturn BloodSugarUnitPreference.Mg
  }

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `blood sugar error message must be hidden when hide blood sugar error message effect is received`() {
    // when
    testCase.dispatch(HideBloodSugarErrorMessage)

    // then
    verify(uiActions).hideBloodSugarErrorMessage()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `date error message must be hidden when hide date error message effect is received`() {
    // when
    testCase.dispatch(HideDateErrorMessage)

    // then
    verify(uiActions).hideDateErrorMessage()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `blood sugar entry sheet must be dismissed when dismiss effect is received`() {
    // when
    testCase.dispatch(Dismiss)

    // then
    verify(uiActions).dismiss()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `show date entry screen when show date entry screen effect is received`() {
    // when
    testCase.dispatch(ShowDateEntryScreen)

    // then
    verify(uiActions).showDateEntryScreen()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `show blood sugar empty error when show blood sugar validation error effect is received with validation result empty`() {
    // when
    testCase.dispatch(ShowBloodSugarValidationError(ErrorBloodSugarEmpty, BloodSugarUnitPreference.Mg))

    // then
    verify(uiActions).showBloodSugarEmptyError()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `show blood sugar high error when show blood sugar validation error effect is received with validation result too high`() {
    // given
    val measurementType = Random

    // when
    testCase.dispatch(ShowBloodSugarValidationError(ErrorBloodSugarTooHigh(measurementType), BloodSugarUnitPreference.Mg))

    // then
    verify(uiActions).showBloodSugarHighError(measurementType, BloodSugarUnitPreference.Mg)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `show blood sugar low error when show blood sugar validation error effect is received with validation result too low`() {
    // given
    val measurementType = PostPrandial

    // when
    testCase.dispatch(ShowBloodSugarValidationError(ErrorBloodSugarTooLow(measurementType), BloodSugarUnitPreference.Mg))

    // then
    verify(uiActions).showBloodSugarLowError(measurementType, BloodSugarUnitPreference.Mg)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `show blood sugar entry screen when show blood sugar entry screen effect is received`() {
    // given
    val bloodSugarDate = LocalDate.of(2020, 1, 1)

    // when
    testCase.dispatch(ShowBloodSugarEntryScreen(bloodSugarDate))

    // then
    verify(uiActions).showBloodSugarEntryScreen()
    verify(uiActions).showBloodSugarDate(bloodSugarDate)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `set date on input fields and date button when prefill date effect is received`() {
    // given
    val entryDate = LocalDate.of(1992, 6, 7)
    userClock.setDate(LocalDate.of(1992, 6, 7))

    // when
    testCase.dispatch(PrefillDate.forNewEntry())

    // then
    testCase.assertOutgoingEvents(DatePrefilled(entryDate))
    verify(uiActions).setDateOnInputFields(entryDate)
    verify(uiActions).showBloodSugarDate(entryDate)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `set specific date on input fields and date button when prefill date for update effect is received`() {
    // given
    val entryDate = LocalDate.of(2020, 2, 14)
    userClock.setDate(entryDate)

    // when
    testCase.dispatch(PrefillDate.forUpdateEntry(Instant.now(userClock)))

    // then
    testCase.assertOutgoingEvents(DatePrefilled(entryDate))
    verify(uiActions).setDateOnInputFields(entryDate)
    verify(uiActions).showBloodSugarDate(entryDate)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `set blood reading when set blood reading effect is received`() {
    // given
    val bloodSugarReading = "128"

    // when
    testCase.dispatch(SetBloodSugarReading(bloodSugarReading))

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).setBloodSugarReading(bloodSugarReading)
    verifyNoMoreInteractions(uiActions)
  }


  @Test
  fun `show invalid date error when show date validation error is received with validation invalid pattern`() {
    // when
    testCase.dispatch(ShowDateValidationError(InvalidPattern))

    // then
    verify(uiActions).showInvalidDateError()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `show date is in future error when show date validation error is received with validation date is in future`() {
    // when
    testCase.dispatch(ShowDateValidationError(DateIsInFuture))

    // then
    verify(uiActions).showDateIsInFutureError()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `set blood sugar saved result and finish when set blood sugar saved result and finish effect is received`() {
    // when
    testCase.dispatch(SetBloodSugarSavedResultAndFinish)

    // then
    verify(uiActions).setBloodSugarSavedResultAndFinish()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `create new blood sugar entry when create blood sugar entry effect is received`() {
    // given
    val date = LocalDate.parse("2020-01-02")
    val bloodSugarReading = BloodSugarReading("120", Random)
    val bloodSugar = TestData.bloodSugarMeasurement(reading = bloodSugarReading)
    val createNewBloodSugarEntry = CreateNewBloodSugarEntry(
        patientUuid = bloodSugar.patientUuid,
        userEnteredDate = date,
        prefilledDate = date,
        bloodSugarReading = bloodSugarReading
    )

    whenever(bloodSugarRepository.saveMeasurement(
        reading = bloodSugar.reading,
        patientUuid = bloodSugar.patientUuid,
        loggedInUser = user,
        facility = facility,
        recordedAt = date.toUtcInstant(userClock),
        uuid = measurementUuid
    )).doReturn(Single.just(bloodSugar))

    // when
    testCase.dispatch(createNewBloodSugarEntry)

    // then
    verify(appointmentRepository).markAppointmentsCreatedBeforeTodayAsVisited(bloodSugar.patientUuid)
    verify(patientRepository).compareAndUpdateRecordedAt(bloodSugar.patientUuid, date.toUtcInstant(userClock))
    testCase.assertOutgoingEvents(BloodSugarSaved(createNewBloodSugarEntry.wasDateChanged))
    verifyNoInteractions(uiActions)
  }

  @Test
  fun `create new hba1c blood sugar entry when create blood sugar entry effect is received`() {
    // given
    val date = LocalDate.parse("2020-01-02")
    val bloodSugarReading = BloodSugarReading("15.2", HbA1c)
    val bloodSugar = TestData.bloodSugarMeasurement(reading = bloodSugarReading)
    val createNewBloodSugarEntry = CreateNewBloodSugarEntry(
        patientUuid = bloodSugar.patientUuid,
        userEnteredDate = date,
        prefilledDate = date,
        bloodSugarReading = bloodSugarReading
    )

    whenever(bloodSugarRepository.saveMeasurement(
        reading = bloodSugar.reading,
        patientUuid = bloodSugar.patientUuid,
        loggedInUser = user,
        facility = facility,
        recordedAt = date.toUtcInstant(userClock),
        uuid = measurementUuid
    )).doReturn(Single.just(bloodSugar))

    // when
    testCase.dispatch(createNewBloodSugarEntry)

    // then
    verify(appointmentRepository).markAppointmentsCreatedBeforeTodayAsVisited(bloodSugar.patientUuid)
    verify(patientRepository).compareAndUpdateRecordedAt(bloodSugar.patientUuid, date.toUtcInstant(userClock))
    testCase.assertOutgoingEvents(BloodSugarSaved(createNewBloodSugarEntry.wasDateChanged))
    verifyNoInteractions(uiActions)
  }

  @Test
  fun `fetch blood sugar measurement, when fetch blood sugar effect is received`() {
    // given
    val bloodSugarMeasurement = TestData.bloodSugarMeasurement()
    whenever(bloodSugarRepository.measurement(bloodSugarMeasurement.uuid)) doReturn bloodSugarMeasurement

    // when
    testCase.dispatch(FetchBloodSugarMeasurement(bloodSugarMeasurement.uuid))

    // then
    testCase.assertOutgoingEvents(BloodSugarMeasurementFetched(bloodSugarMeasurement))
    verifyNoInteractions(uiActions)
  }

  @Test
  fun `update blood sugar entry when update blood sugar entry effect is received`() {
    // given
    val patientUuid = UUID.fromString("260a831f-bc31-4341-bc0d-46325e85e32d")

    val date = LocalDate.parse("2020-02-14")
    val updatedDate = LocalDate.parse("2020-02-12")

    val bloodSugarReading = BloodSugarReading("250", Random)
    val updateBloodSugarReading = bloodSugarReading.copy(value = "145")
    val bloodSugarMeasurementUuid = UUID.fromString("58a3fa4b-2b32-4c43-a1cd-ee3d787064f7")

    val bloodSugar = TestData.bloodSugarMeasurement(
        uuid = bloodSugarMeasurementUuid,
        patientUuid = patientUuid,
        facilityUuid = facility.uuid,
        userUuid = user.uuid,
        reading = bloodSugarReading,
        recordedAt = date.toUtcInstant(userClock),
        timestamps = Timestamps(
            createdAt = date.toUtcInstant(userClock),
            updatedAt = date.toUtcInstant(userClock),
            deletedAt = null
        ),
        syncStatus = DONE
    )
    val updateBloodSugarEntry = UpdateBloodSugarEntry(
        bloodSugarMeasurementUuid = bloodSugarMeasurementUuid,
        userEnteredDate = updatedDate,
        prefilledDate = updatedDate,
        bloodSugarReading = updateBloodSugarReading
    )

    whenever(bloodSugarRepository.measurement(bloodSugarMeasurementUuid)).doReturn(bloodSugar)

    // when
    testCase.dispatch(updateBloodSugarEntry)

    // then
    testCase.assertOutgoingEvents(BloodSugarSaved(updateBloodSugarEntry.wasDateChanged))
    verifyNoInteractions(uiActions)
  }

  @Test
  fun `update hba1c blood sugar entry when update blood sugar entry effect is received`() {
    // given
    val patientUuid = UUID.fromString("260a831f-bc31-4341-bc0d-46325e85e32d")

    val date = LocalDate.parse("2020-02-14")
    val updatedDate = LocalDate.parse("2020-02-12")

    val bloodSugarReading = BloodSugarReading("5.6", HbA1c)
    val updateBloodSugarReading = bloodSugarReading.copy(value = "6.2")
    val bloodSugarMeasurementUuid = UUID.fromString("58a3fa4b-2b32-4c43-a1cd-ee3d787064f7")

    val bloodSugar = TestData.bloodSugarMeasurement(
        uuid = bloodSugarMeasurementUuid,
        patientUuid = patientUuid,
        facilityUuid = facility.uuid,
        userUuid = user.uuid,
        reading = bloodSugarReading,
        recordedAt = date.toUtcInstant(userClock),
        timestamps = Timestamps(
            createdAt = date.toUtcInstant(userClock),
            updatedAt = date.toUtcInstant(userClock),
            deletedAt = null
        ),
        syncStatus = DONE
    )
    val updateBloodSugarEntry = UpdateBloodSugarEntry(
        bloodSugarMeasurementUuid = bloodSugarMeasurementUuid,
        userEnteredDate = updatedDate,
        prefilledDate = updatedDate,
        bloodSugarReading = updateBloodSugarReading
    )

    whenever(bloodSugarRepository.measurement(bloodSugarMeasurementUuid)).doReturn(bloodSugar)

    // when
    testCase.dispatch(updateBloodSugarEntry)

    // then
    testCase.assertOutgoingEvents(BloodSugarSaved(updateBloodSugarEntry.wasDateChanged))
    verifyNoInteractions(uiActions)
  }

  @Test
  fun `show remove blood sugar confirmation dialog, when show confirm remove blood sugar effect is received`() {
    // given
    val bloodSugarMeasurementUuid = UUID.fromString("a9c5f19f-adc5-46ea-9d03-00a415631882")

    // when
    testCase.dispatch(ShowConfirmRemoveBloodSugarDialog(bloodSugarMeasurementUuid))

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).showConfirmRemoveBloodSugarDialog(bloodSugarMeasurementUuid)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when load blood sugar unit preference effect is received, then load unit preference`() {
    // given
    whenever(bloodSugarUnitPreference.asObservable()) doReturn Observable.just(bloodSugarUnitPreferenceSelection)

    // when
    testCase.dispatch(LoadBloodSugarUnitPreference)

    // then
    testCase.assertOutgoingEvents(BloodSugarUnitPreferenceLoaded(bloodSugarUnitPreferenceSelection))
    verifyNoInteractions(uiActions)
  }

  @Test
  fun `when show blood sugar unit selection dialog effect is received, then open the dialog`() {
    // when
    testCase.dispatch(ShowBloodSugarUnitSelectionDialog(bloodSugarUnitPreferenceSelection))

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).showBloodSugarUnitSelectionDialog(bloodSugarUnitPreferenceSelection)
    verifyNoMoreInteractions(uiActions)
  }
}
