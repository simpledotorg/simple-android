package org.simple.clinic.summary.bloodpressures

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
import org.simple.clinic.summary.bloodpressures.BloodPressuresCountLoaded
import org.simple.clinic.summary.bloodpressures.BloodPressuresLoaded
import org.simple.clinic.summary.bloodpressures.LoadBloodPressures
import org.simple.clinic.summary.bloodpressures.LoadBloodPressuresCount
import org.simple.clinic.summary.bloodpressures.NewBloodPressureSummaryViewEffectHandler
import org.simple.clinic.summary.bloodpressures.NewBloodPressureSummaryViewUiActions
import org.simple.clinic.summary.bloodpressures.OpenBloodPressureEntrySheet
import org.simple.clinic.summary.bloodpressures.OpenBloodPressureUpdateSheet
import org.simple.clinic.summary.bloodpressures.ShowBloodPressureHistoryScreen
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import java.util.UUID

class NewBloodPressureSummaryViewEffectHandlerTest {

  private val uiActions = mock<NewBloodPressureSummaryViewUiActions>()
  private val bloodPressureRepository = mock<BloodPressureRepository>()
  private val effectHandler = NewBloodPressureSummaryViewEffectHandler(
      bloodPressureRepository = bloodPressureRepository,
      schedulersProvider = TrampolineSchedulersProvider(),
      uiActions = uiActions
  ).build()
  private val testCase = EffectHandlerTestCase(effectHandler)
  private val patientUuid = UUID.fromString("6b00207f-a613-4adc-9a72-dff68481a3ff")

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when load blood pressures effect is received, then load blood pressures`() {
    // given
    val numberOfBpsToDisplay = 3
    val bloodPressure = PatientMocker.bp(
        UUID.fromString("51ac042d-2f70-495c-a3e3-2599d8990da2"),
        patientUuid
    )
    val bloodPressures = listOf(bloodPressure)
    whenever(bloodPressureRepository.newestMeasurementsForPatient(patientUuid = patientUuid, limit = numberOfBpsToDisplay)) doReturn Observable.just(bloodPressures)

    // when
    testCase.dispatch(LoadBloodPressures(patientUuid, numberOfBpsToDisplay))

    // then
    testCase.assertOutgoingEvents(BloodPressuresLoaded(bloodPressures))
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when load blood pressures count effect is received, then load blood pressures count`() {
    // given
    val bloodPressuresCount = 10
    whenever(bloodPressureRepository.bloodPressureCount(patientUuid)) doReturn Observable.just(bloodPressuresCount)

    // when
    testCase.dispatch(LoadBloodPressuresCount(patientUuid))

    // then
    testCase.assertOutgoingEvents(BloodPressuresCountLoaded(bloodPressuresCount))
  }

  @Test
  fun `when open blood pressure entry sheet effect is received, then open blood pressure entry sheet`() {
    // when
    testCase.dispatch(OpenBloodPressureEntrySheet(patientUuid))

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).openBloodPressureEntrySheet(patientUuid)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when open blood pressure update sheet effect is received, then open blood pressure update sheet`() {
    // given
    val bloodPressure = PatientMocker.bp(
        UUID.fromString("3c59796e-780b-4e2d-9aaf-8cd662975378"),
        patientUuid
    )

    // when
    testCase.dispatch(OpenBloodPressureUpdateSheet(bloodPressure))

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).openBloodPressureUpdateSheet(bloodPressure.uuid)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when show blood pressure history screen effect is received, then show blood pressure history screen`() {
    // when
    testCase.dispatch(ShowBloodPressureHistoryScreen(patientUuid))

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).showBloodPressureHistoryScreen(patientUuid)
    verifyNoMoreInteractions(uiActions)
  }
}
