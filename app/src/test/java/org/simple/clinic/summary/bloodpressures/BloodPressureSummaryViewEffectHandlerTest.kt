package org.simple.clinic.summary.bloodpressures

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import io.reactivex.Observable
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import java.util.UUID

class BloodPressureSummaryViewEffectHandlerTest {

  private val uiActions = mock<BloodPressureSummaryViewUiActions>()
  private val bloodPressureRepository = mock<BloodPressureRepository>()
  private val patientUuid = UUID.fromString("6b00207f-a613-4adc-9a72-dff68481a3ff")
  private val currentFacility = TestData.facility(uuid = UUID.fromString("2257f737-0e8a-452d-a270-66bdc2422664"))

  private val effectHandler = BloodPressureSummaryViewEffectHandler(
      bloodPressureRepository = bloodPressureRepository,
      schedulersProvider = TrampolineSchedulersProvider(),
      facility = Lazy { currentFacility },
      uiActions = uiActions
  ).build()
  private val testCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when load blood pressures effect is received, then load blood pressures`() {
    // given
    val numberOfBpsToDisplay = 3
    val bloodPressure = TestData.bloodPressureMeasurement(
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
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when load current facility effect is received, then load current facility`() {
    // when
    testCase.dispatch(LoadCurrentFacility)

    // then
    testCase.assertOutgoingEvents(CurrentFacilityLoaded(currentFacility))
    verifyZeroInteractions(uiActions)
  }


  @Test
  fun `when open blood pressure entry sheet effect is received, then open blood pressure entry sheet`() {
    // when
    testCase.dispatch(OpenBloodPressureEntrySheet(patientUuid, currentFacility))

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).openBloodPressureEntrySheet(patientUuid, currentFacility)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when open blood pressure update sheet effect is received, then open blood pressure update sheet`() {
    // given
    val bloodPressure = TestData.bloodPressureMeasurement(
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
