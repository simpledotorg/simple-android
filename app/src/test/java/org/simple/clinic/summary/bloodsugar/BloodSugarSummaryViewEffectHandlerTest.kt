package org.simple.clinic.summary.bloodsugar

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import org.junit.Test
import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import org.simple.clinic.bloodsugar.BloodSugarRepository
import org.simple.clinic.bloodsugar.Random
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.summary.PatientSummaryConfig
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import java.util.UUID

class BloodSugarSummaryViewEffectHandlerTest {

  private val bloodSugarRepository = mock<BloodSugarRepository>()
  private val uiActions = mock<UiActions>()
  private val config = mock<BloodSugarSummaryConfig>()
  private val effectHandler = BloodSugarSummaryViewEffectHandler(
      bloodSugarRepository,
      TrampolineSchedulersProvider(),
      uiActions,
      config
  ).build()

  private val testCase = EffectHandlerTestCase(effectHandler)
  private val patientUuid = UUID.fromString("69cdea01-fbd8-437a-844c-25e412f32a9e")

  @Test
  fun `when fetch blood sugar effect is received then blood sugar should be fetched`() {
    //given
    val measurements = listOf<BloodSugarMeasurement>()
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

  @Test
  fun `when load blood sugars count effect is received, then load blood sugars count`() {
    // given
    val bloodSugarsCount = 10
    whenever(bloodSugarRepository.bloodSugarsCount(patientUuid)) doReturn Observable.just(bloodSugarsCount)

    // when
    testCase.dispatch(FetchBloodSugarCount(patientUuid))

    // then
    testCase.assertOutgoingEvents(BloodSugarCountFetched(bloodSugarsCount))
  }

  @Test
  fun `when show blood sugar history screen effect is received, then show blood sugar history screen`() {
    // when
    testCase.dispatch(ShowBloodSugarHistoryScreen(patientUuid))

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).showBloodSugarHistoryScreen(patientUuid)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when open blood sugar update sheet effect is received, then open blood sugar update sheet`() {
    // given
    val bloodSugar = PatientMocker.bloodSugar(
        UUID.fromString("3be65af9-324f-4904-9ab4-6d8c47941b99"),
        patientUuid = patientUuid
    )

    // when
    testCase.dispatch(OpenBloodSugarUpdateSheet(bloodSugar))

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).openBloodSugarUpdateSheet(bloodSugar.uuid, Random)
    verifyNoMoreInteractions(uiActions)
  }
}
