package org.simple.clinic.bp.history

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import org.junit.After
import org.junit.Test
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import java.util.UUID

class BloodPressureHistoryScreenEffectHandlerTest {

  private val bloodPressureRepository = mock<BloodPressureRepository>()
  private val patientUuid = UUID.fromString("433d058f-daef-47a7-8c61-95f1a220cbcb")
  private val uiActions = mock<BloodPressureHistoryScreenUiActions>()
  private val effectHandler = BloodPressureHistoryScreenEffectHandler(
      bloodPressureRepository,
      TrampolineSchedulersProvider(),
      uiActions
  ).build()
  private val testCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when load blood pressure history effect is received, then load all blood pressures`() {
    // given
    val bloodPressureMeasurement = PatientMocker.bp(
        UUID.fromString("51ac042d-2f70-495c-a3e3-2599d8990da2"),
        patientUuid
    )
    val bloodPressures = listOf(bloodPressureMeasurement)
    whenever(bloodPressureRepository.allBloodPressures(patientUuid)) doReturn Observable.just(bloodPressures)

    // when
    testCase.dispatch(LoadBloodPressureHistory(patientUuid))

    // then
    testCase.assertOutgoingEvents(BloodPressureHistoryLoaded(bloodPressures))
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when open blood pressure entry sheet effect is received, then open blood pressure entry sheet`() {
    // when
    testCase.dispatch(OpenBloodPressureEntrySheet)

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).openBloodPressureEntrySheet()
    verifyNoMoreInteractions(uiActions)
  }
}
