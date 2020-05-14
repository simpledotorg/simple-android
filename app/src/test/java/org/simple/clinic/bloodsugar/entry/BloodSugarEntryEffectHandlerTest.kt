package org.simple.clinic.bloodsugar.entry

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.bloodsugar.BloodSugarReading
import org.simple.clinic.bloodsugar.BloodSugarRepository
import org.simple.clinic.bloodsugar.HbA1c
import org.simple.clinic.bloodsugar.PostPrandial
import org.simple.clinic.bloodsugar.Random
import org.simple.clinic.bloodsugar.entry.ValidationResult.ErrorBloodSugarEmpty
import org.simple.clinic.bloodsugar.entry.ValidationResult.ErrorBloodSugarTooHigh
import org.simple.clinic.bloodsugar.entry.ValidationResult.ErrorBloodSugarTooLow
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.SyncStatus.DONE
import org.simple.clinic.storage.Timestamps
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Just
import org.simple.clinic.util.Optional
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import org.simple.clinic.util.toUtcInstant
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Invalid.DateIsInFuture
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Invalid.InvalidPattern
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import java.util.UUID

class BloodSugarEntryEffectHandlerTest {

  private val ui = mock<BloodSugarEntryUi>()
  private val userClock = TestUserClock()

  private val facilityRepository = mock<FacilityRepository>()
  private val appointmentRepository = mock<AppointmentRepository>()
  private val patientRepository = mock<PatientRepository>()
  private val bloodSugarRepository = mock<BloodSugarRepository>()

  private val user = TestData.loggedInUser(uuid = UUID.fromString("4844b826-a162-49fe-b92c-962da172e86c"))

  private val effectHandler = BloodSugarEntryEffectHandler(
      ui,
      facilityRepository,
      bloodSugarRepository,
      patientRepository,
      appointmentRepository,
      userClock,
      TrampolineSchedulersProvider(),
      Lazy { user }
  ).build()
  private val testCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `blood sugar error message must be hidden when hide blood sugar error message effect is received`() {
    // when
    testCase.dispatch(HideBloodSugarErrorMessage)

    // then
    verify(ui).hideBloodSugarErrorMessage()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `date error message must be hidden when hide date error message effect is received`() {
    // when
    testCase.dispatch(HideDateErrorMessage)

    // then
    verify(ui).hideDateErrorMessage()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `blood sugar entry sheet must be dismissed when dismiss effect is received`() {
    // when
    testCase.dispatch(Dismiss)

    // then
    verify(ui).dismiss()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `show date entry screen when show date entry screen effect is received`() {
    // when
    testCase.dispatch(ShowDateEntryScreen)

    // then
    verify(ui).showDateEntryScreen()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `show blood sugar empty error when show blood sugar validation error effect is received with validation result empty`() {
    // when
    testCase.dispatch(ShowBloodSugarValidationError(ErrorBloodSugarEmpty))

    // then
    verify(ui).showBloodSugarEmptyError()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `show blood sugar high error when show blood sugar validation error effect is received with validation result too high`() {
    // given
    val measurementType = Random

    // when
    testCase.dispatch(ShowBloodSugarValidationError(ErrorBloodSugarTooHigh(measurementType)))

    // then
    verify(ui).showBloodSugarHighError(measurementType)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `show blood sugar low error when show blood sugar validation error effect is received with validation result too low`() {
    // given
    val measurementType = PostPrandial

    // when
    testCase.dispatch(ShowBloodSugarValidationError(ErrorBloodSugarTooLow(measurementType)))

    // then
    verify(ui).showBloodSugarLowError(measurementType)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `show blood sugar entry screen when show blood sugar entry screen effect is received`() {
    // given
    val bloodSugarDate = LocalDate.of(2020, 1, 1)

    // when
    testCase.dispatch(ShowBloodSugarEntryScreen(bloodSugarDate))

    // then
    verify(ui).showBloodSugarEntryScreen()
    verify(ui).showDateOnDateButton(bloodSugarDate)
    verifyNoMoreInteractions(ui)
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
    verify(ui).setDateOnInputFields("7", "6", "1992")
    verify(ui).showDateOnDateButton(entryDate)
    verifyNoMoreInteractions(ui)
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
    verify(ui).setDateOnInputFields("14", "2", "2020")
    verify(ui).showDateOnDateButton(entryDate)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `set blood reading when set blood reading effect is received`() {
    // given
    val bloodSugarReading = "128"

    // when
    testCase.dispatch(SetBloodSugarReading(bloodSugarReading))

    // then
    testCase.assertNoOutgoingEvents()
    verify(ui).setBloodSugarReading(bloodSugarReading)
    verifyNoMoreInteractions(ui)
  }


  @Test
  fun `show invalid date error when show date validation error is received with validation invalid pattern`() {
    // when
    testCase.dispatch(ShowDateValidationError(InvalidPattern))

    // then
    verify(ui).showInvalidDateError()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `show date is in future error when show date validation error is received with validation date is in future`() {
    // when
    testCase.dispatch(ShowDateValidationError(DateIsInFuture))

    // then
    verify(ui).showDateIsInFutureError()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `set blood sugar saved result and finish when set blood sugar saved result and finish effect is received`() {
    // when
    testCase.dispatch(SetBloodSugarSavedResultAndFinish)

    // then
    verify(ui).setBloodSugarSavedResultAndFinish()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `create new blood sugar entry when create blood sugar entry effect is received`() {
    // given
    val facility = TestData.facility(uuid = UUID.fromString("f895b54f-ee32-4471-bc0c-a91b80368778"))

    val date = LocalDate.parse("2020-01-02")
    val bloodSugarReading = BloodSugarReading("120", Random)
    val bloodSugar = TestData.bloodSugarMeasurement(reading = bloodSugarReading)
    val createNewBloodSugarEntry = CreateNewBloodSugarEntry(
        patientUuid = bloodSugar.patientUuid,
        userEnteredDate = date,
        prefilledDate = date,
        bloodSugarReading = bloodSugarReading
    )

    whenever(facilityRepository.currentFacility(user)).doReturn(Observable.just(facility))
    whenever(bloodSugarRepository.saveMeasurement(eq(bloodSugar.reading), eq(bloodSugar.patientUuid), eq(user), eq(facility), eq(date.toUtcInstant(userClock)), any())).doReturn(Single.just(bloodSugar))
    whenever(patientRepository.compareAndUpdateRecordedAt(bloodSugar.patientUuid, date.toUtcInstant(userClock))).doReturn(Completable.complete())
    whenever(appointmentRepository.markAppointmentsCreatedBeforeTodayAsVisited(bloodSugar.patientUuid)).doReturn(Completable.complete())

    // when
    testCase.dispatch(createNewBloodSugarEntry)

    // then
    testCase.assertOutgoingEvents(BloodSugarSaved(createNewBloodSugarEntry.wasDateChanged))
    verifyZeroInteractions(ui)
  }

  @Test
  fun `create new hba1c blood sugar entry when create blood sugar entry effect is received`() {
    // given
    val facility = TestData.facility(uuid = UUID.fromString("f895b54f-ee32-4471-bc0c-a91b80368778"))

    val date = LocalDate.parse("2020-01-02")
    val bloodSugarReading = BloodSugarReading("15.2", HbA1c)
    val bloodSugar = TestData.bloodSugarMeasurement(reading = bloodSugarReading)
    val createNewBloodSugarEntry = CreateNewBloodSugarEntry(
        patientUuid = bloodSugar.patientUuid,
        userEnteredDate = date,
        prefilledDate = date,
        bloodSugarReading = bloodSugarReading
    )

    whenever(facilityRepository.currentFacility(user)).doReturn(Observable.just(facility))
    whenever(bloodSugarRepository.saveMeasurement(eq(bloodSugar.reading), eq(bloodSugar.patientUuid), eq(user), eq(facility), eq(date.toUtcInstant(userClock)), any())).doReturn(Single.just(bloodSugar))
    whenever(patientRepository.compareAndUpdateRecordedAt(bloodSugar.patientUuid, date.toUtcInstant(userClock))).doReturn(Completable.complete())
    whenever(appointmentRepository.markAppointmentsCreatedBeforeTodayAsVisited(bloodSugar.patientUuid)).doReturn(Completable.complete())

    // when
    testCase.dispatch(createNewBloodSugarEntry)

    // then
    testCase.assertOutgoingEvents(BloodSugarSaved(createNewBloodSugarEntry.wasDateChanged))
    verifyZeroInteractions(ui)
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
    verifyZeroInteractions(ui)
  }

  @Test
  fun `update blood sugar entry when update blood sugar entry effect is received`() {
    // given
    val facility = TestData.facility(uuid = UUID.fromString("7fabe36b-8fc3-457d-b9a8-68df71def7bd"))
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

    whenever(facilityRepository.currentFacilityImmediate(user)).doReturn(facility)
    whenever(bloodSugarRepository.measurement(bloodSugarMeasurementUuid)).doReturn(bloodSugar)

    // when
    testCase.dispatch(updateBloodSugarEntry)

    // then
    testCase.assertOutgoingEvents(BloodSugarSaved(updateBloodSugarEntry.wasDateChanged))
    verifyZeroInteractions(ui)
  }

  @Test
  fun `update hba1c blood sugar entry when update blood sugar entry effect is received`() {
    // given
    val facility = TestData.facility(uuid = UUID.fromString("7fabe36b-8fc3-457d-b9a8-68df71def7bd"))
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

    whenever(facilityRepository.currentFacilityImmediate(user)).doReturn(facility)
    whenever(bloodSugarRepository.measurement(bloodSugarMeasurementUuid)).doReturn(bloodSugar)

    // when
    testCase.dispatch(updateBloodSugarEntry)

    // then
    testCase.assertOutgoingEvents(BloodSugarSaved(updateBloodSugarEntry.wasDateChanged))
    verifyZeroInteractions(ui)
  }

  @Test
  fun `show remove blood sugar confirmation dialog, when show confirm remove blood sugar effect is received`() {
    // given
    val bloodSugarMeasurementUuid = UUID.fromString("a9c5f19f-adc5-46ea-9d03-00a415631882")

    // when
    testCase.dispatch(ShowConfirmRemoveBloodSugarDialog(bloodSugarMeasurementUuid))

    // then
    testCase.assertNoOutgoingEvents()
    verify(ui).showConfirmRemoveBloodSugarDialog(bloodSugarMeasurementUuid)
    verifyNoMoreInteractions(ui)
  }

}
