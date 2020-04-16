package org.simple.clinic.bloodsugar.history

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import org.junit.After
import org.junit.Test
import org.simple.clinic.bloodsugar.BloodSugarRepository
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.Patient
import org.simple.clinic.TestData
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.Just
import org.simple.clinic.util.Optional
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import java.util.UUID

class BloodSugarHistoryScreenEffectHandlerTest {
  private val patientRepository = mock<PatientRepository>()
  private val bloodSugarRepository = mock<BloodSugarRepository>()
  private val patientUuid = UUID.fromString("1d695883-54cf-4cf0-8795-43f83a0c3f02")
  private val uiActions = mock<BloodSugarHistoryScreenUiActions>()
  private val effectHandler = BloodSugarHistoryScreenEffectHandler(
      patientRepository,
      bloodSugarRepository,
      TrampolineSchedulersProvider(),
      uiActions
  ).build()
  private val testCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when load patient effect is received, then load patient`() {
    // given
    val patient = TestData.patient(uuid = patientUuid)
    whenever(patientRepository.patient(patientUuid)) doReturn Observable.just<Optional<Patient>>(Just(patient))

    // when
    testCase.dispatch(LoadPatient(patientUuid))

    // then
    testCase.assertOutgoingEvents(PatientLoaded(patient))
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when load blood sugars history effect is received, then load all blood sugars`() {
    // given
    val bloodSugarMeasurement = TestData.bloodSugarMeasurement(
        uuid = UUID.fromString("c593e506-e603-4f34-9ea8-89913cdbce9e"),
        patientUuid = patientUuid
    )
    val bloodSugars = listOf(bloodSugarMeasurement)
    whenever(bloodSugarRepository.allBloodSugars(patientUuid)) doReturn Observable.just(bloodSugars)

    // when
    testCase.dispatch(LoadBloodSugarHistory(patientUuid))

    // then
    testCase.assertOutgoingEvents(BloodSugarHistoryLoaded(bloodSugars))
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when open blood sugar entry sheet effect is received, then open blood sugar entry sheet`() {
    // when
    testCase.dispatch(OpenBloodSugarEntrySheet(patientUuid))

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).openBloodSugarEntrySheet(patientUuid)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when open blood sugar update sheet effect is received, then open blood sugar update sheet`() {
    // given
    val measurement = TestData.bloodSugarMeasurement()

    // when
    testCase.dispatch(OpenBloodSugarUpdateSheet(measurement))

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).openBloodSugarUpdateSheet(measurement)
    verifyNoMoreInteractions(uiActions)
  }

}
