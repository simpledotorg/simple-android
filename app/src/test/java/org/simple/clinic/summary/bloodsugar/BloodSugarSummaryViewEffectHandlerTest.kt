package org.simple.clinic.summary.bloodsugar

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import org.junit.Test
import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import org.simple.clinic.bloodsugar.BloodSugarRepository
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import java.util.UUID

class BloodSugarSummaryViewEffectHandlerTest {

  private val bloodSugarRepository = mock<BloodSugarRepository>()
  private val uiActions = mock<UiActions>()
  private val effectHandler = BloodSugarSummaryViewEffectHandler(
      bloodSugarRepository,
      TrampolineSchedulersProvider(),
      uiActions
  ).build()

  private val testCase = EffectHandlerTestCase(effectHandler)

  @Test
  fun `when fetch blood sugar effect is received then blood sugar should be fetched`() {
    //given
    val measurements = listOf<BloodSugarMeasurement>()
    whenever(bloodSugarRepository.latestMeasurements(any(), any())).thenReturn(Observable.just(measurements))

    //when
    testCase.dispatch(FetchBloodSugarSummary(UUID.fromString("69cdea01-fbd8-437a-844c-25e412f32a9e")))

    //then
    testCase.assertOutgoingEvents(BloodSugarSummaryFetched(measurements))
  }

  @Test
  fun `when open blood sugar type selector effect is received then type selector sheet should be opened`() {
    //when
    testCase.dispatch(OpenBloodSugarTypeSelector)

    //then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).showBloodSugarTypeSelector()
  }
}
