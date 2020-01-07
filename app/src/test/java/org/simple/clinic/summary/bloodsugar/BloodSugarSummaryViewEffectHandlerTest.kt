package org.simple.clinic.summary.bloodsugar

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import org.junit.Test
import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import org.simple.clinic.bloodsugar.BloodSugarRepository
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.summary.PatientSummaryConfig
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import java.util.UUID

class BloodSugarSummaryViewEffectHandlerTest {

  private val bloodSugarRepository = mock<BloodSugarRepository>()
  private val uiActions = mock<UiActions>()
  private val config = mock<PatientSummaryConfig>()
  private val effectHandler = BloodSugarSummaryViewEffectHandler(
      bloodSugarRepository,
      TrampolineSchedulersProvider(),
      uiActions,
      config
  ).build()

  private val testCase = EffectHandlerTestCase(effectHandler)

  @Test
  fun `when fetch blood sugar effect is received then blood sugar should be fetched`() {
    //given
    val measurements = listOf<BloodSugarMeasurement>()
    val patientUuid = UUID.fromString("69cdea01-fbd8-437a-844c-25e412f32a9e")
    whenever(bloodSugarRepository.latestMeasurements(patientUuid = patientUuid, limit = config.numberOfBloodSugarsToDisplay)).thenReturn(Observable.just(measurements))

    //when
    testCase.dispatch(FetchBloodSugarSummary(patientUuid))

    //then
    testCase.assertOutgoingEvents(BloodSugarSummaryFetched(measurements))
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when open blood sugar type selector effect is received then type selector sheet should be opened`() {
    //when
    testCase.dispatch(OpenBloodSugarTypeSelector)

    //then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).showBloodSugarTypeSelector()
    verifyNoMoreInteractions(uiActions)
  }
}
