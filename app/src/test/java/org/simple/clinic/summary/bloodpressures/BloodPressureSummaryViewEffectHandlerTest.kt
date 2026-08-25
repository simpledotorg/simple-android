package org.simple.clinic.summary.bloodpressures

import dagger.Lazy
import io.reactivex.Observable
import io.sentry.util.UUIDGenerator
import org.junit.After
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.simple.clinic.TestData
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import org.simple.clinic.uuid.FakeUuidGenerator
import java.util.UUID

class BloodPressureSummaryViewEffectHandlerTest {

  private val uiActions = mock<BloodPressureSummaryViewUiActions>()
  private val bloodPressureRepository = mock<BloodPressureRepository>()

  private val medicalHistoryRepository = mock<MedicalHistoryRepository>()
  private val patientUuid = UUID.fromString("6b00207f-a613-4adc-9a72-dff68481a3ff")
  private val currentFacility = TestData.facility(uuid = UUID.fromString("2257f737-0e8a-452d-a270-66bdc2422664"))

  private val uuidGenerator = FakeUuidGenerator(uuid = UUID.fromString("e78ec5f7-6fe9-4812-a894-9e34e55c670e"))

  private val effectHandler = BloodPressureSummaryViewEffectHandler(
      bloodPressureRepository = bloodPressureRepository,
      medicalHistoryRepository = medicalHistoryRepository,
      schedulersProvider = TrampolineSchedulersProvider(),
      facility = { currentFacility },
      uiActions = uiActions,
      uuidGenerator = uuidGenerator
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
    verifyNoInteractions(uiActions)
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
    verifyNoInteractions(uiActions)
  }

  @Test
  fun `when load current facility effect is received, then load current facility`() {
    // when
    testCase.dispatch(LoadCurrentFacility)

    // then
    testCase.assertOutgoingEvents(CurrentFacilityLoaded(currentFacility))
    verifyNoInteractions(uiActions)
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
    testCase.dispatch(OpenBloodPressureUpdateSheet(bloodPressure.uuid))

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

  @Test
  fun `when load medical history effect is received, then load medical history`() {
    // given
    val medicalHistory = TestData.medicalHistory(
        uuid = UUID.fromString("8568fd00-7de8-469e-a869-e92e54eb7f9b"),
        patientUuid = patientUuid
    )
    whenever(medicalHistoryRepository.historyForPatientOrDefault(
        patientUuid = patientUuid,
        defaultHistoryUuid = uuidGenerator.v4()
    )) doReturn Observable.just(medicalHistory)

    // when
    testCase.dispatch(LoadMedicalHistory(patientUuid))

    // then
    testCase.assertOutgoingEvents(MedicalHistoryLoaded(medicalHistory))
    verifyNoInteractions(uiActions)
  }
}
