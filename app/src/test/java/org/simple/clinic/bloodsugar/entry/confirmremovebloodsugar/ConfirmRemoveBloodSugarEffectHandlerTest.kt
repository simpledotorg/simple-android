package org.simple.clinic.bloodsugar.entry.confirmremovebloodsugar

import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import io.reactivex.Completable
import org.junit.After
import org.junit.Test
import org.simple.sharedTestCode.TestData
import org.simple.clinic.bloodsugar.BloodSugarRepository
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import java.util.UUID

class ConfirmRemoveBloodSugarEffectHandlerTest {
  private val patientRepository = mock<PatientRepository>()
  private val bloodSugarRepository = mock<BloodSugarRepository>()
  private val uiActions = mock<ConfirmRemoveBloodSugarUiActions>()
  private val effectHandler = ConfirmRemoveBloodSugarEffectHandler(
      patientRepository,
      bloodSugarRepository,
      uiActions,
      TrampolineSchedulersProvider()
  ).build()
  private val testCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `mark blood sugar as deleted, when remove blood sugar effect is received`() {
    // given
    val patientUuid = UUID.fromString("8fb98b2b-84d7-4e19-a5bc-3a57c3f53982")
    val bloodSugarMeasurementUuid = UUID.fromString("7693aafb-9044-4ef3-999a-5ecda2895415")
    val bloodSugarMeasurement = TestData.bloodSugarMeasurement(
        uuid = bloodSugarMeasurementUuid,
        patientUuid = patientUuid
    )

    whenever(bloodSugarRepository.measurement(bloodSugarMeasurementUuid)) doReturn bloodSugarMeasurement
    whenever(patientRepository.updateRecordedAt(patientUuid)) doReturn Completable.complete()

    // when
    testCase.dispatch(MarkBloodSugarAsDeleted(bloodSugarMeasurementUuid))

    // then
    testCase.assertOutgoingEvents(BloodSugarMarkedAsDeleted)
  }

  @Test
  fun `close confirm blood sugar dialog, when close confirm dialog effect is received`() {
    // when
    testCase.dispatch(CloseConfirmRemoveBloodSugarDialog)

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).closeDialog()
    verifyNoMoreInteractions(uiActions)
  }

}
