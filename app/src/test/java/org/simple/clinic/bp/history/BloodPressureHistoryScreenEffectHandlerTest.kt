package org.simple.clinic.bp.history

import androidx.paging.PagingData
import io.reactivex.Observable
import kotlinx.coroutines.test.TestScope
import org.junit.After
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.bp.history.adapter.BloodPressureHistoryListItem
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.summary.PatientSummaryConfig
import org.simple.clinic.util.PagerFactory
import org.simple.clinic.util.PagingSourceFactory
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.sharedTestCode.TestData
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Optional
import java.util.UUID

class BloodPressureHistoryScreenEffectHandlerTest {

  private val patientRepository = mock<PatientRepository>()
  private val bloodPressureRepository = mock<BloodPressureRepository>()
  private val patientUuid = UUID.fromString("433d058f-daef-47a7-8c61-95f1a220cbcb")
  private val uiActions = mock<BloodPressureHistoryScreenUiActions>()
  private val viewEffectHandler = BloodPressureHistoryViewEffectHandler(uiActions)
  private val pagerFactory = mock<PagerFactory>()
  private val pagingCacheScope = TestScope()
  private val effectHandler = BloodPressureHistoryScreenEffectHandler(
      bloodPressureRepository,
      patientRepository,
      TestSchedulersProvider.trampoline(),
      pagerFactory = pagerFactory,
      pagingSourceFactory = mock(),
      patientSummaryConfig = PatientSummaryConfig(
          bpEditableDuration = Duration.ofMinutes(10),
          numberOfMeasurementsForTeleconsultation = 0,
      ),
      viewEffectsConsumer = viewEffectHandler::handle,
      pagingCacheScope = { pagingCacheScope }
  ).build()
  private val testCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when load patient effect is received, then load patient`() {
    // given
    val patientUuid = UUID.fromString("46044a13-012e-439b-81c9-8bbb15307629")
    val patient = TestData.patient(uuid = patientUuid)
    whenever(patientRepository.patient(patientUuid)) doReturn Observable.just<Optional<Patient>>(Optional.of(patient))

    // when
    testCase.dispatch(LoadPatient(patientUuid))

    // then
    testCase.assertOutgoingEvents(PatientLoaded(patient))
    verifyNoInteractions(uiActions)
  }

  @Test
  fun `when open blood pressure entry sheet effect is received, then open blood pressure entry sheet`() {
    // given
    val patientUuid = UUID.fromString("5adeb648-00a6-4073-b509-ac74cbd5f08b")

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
    val bloodPressureMeasurement = TestData.bloodPressureMeasurement(
        UUID.fromString("3c6fb840-86b9-4b85-aa75-b24c4bb9fbfd"),
        patientUuid
    )

    // when
    testCase.dispatch(OpenBloodPressureUpdateSheet(bloodPressureMeasurement))

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).openBloodPressureUpdateSheet(bloodPressureMeasurement.uuid)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when load blood pressure history effect is received, then load blood pressure history`() {
    // given
    val patientUuid = UUID.fromString("f6f60760-b290-4e0f-9db0-74179b7cd170")
    val createdAt = Instant.parse("2020-01-01T00:00:00Z")
    val recordedAt = Instant.parse("2020-01-01T00:00:00Z")

    val bloodPressureNow = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("78e876ae-3055-43d6-a132-7ad5dd930e23"),
        patientUuid = patientUuid,
        systolic = 120,
        diastolic = 70,
        createdAt = createdAt,
        recordedAt = recordedAt
    )

    val bloodPressure15MinutesInPast = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("2b929a33-e543-4a4f-a98e-5d0d3e8e1e03"),
        patientUuid = patientUuid,
        systolic = 120,
        diastolic = 85,
        createdAt = createdAt.minus(15, ChronoUnit.MINUTES),
        recordedAt = recordedAt.minus(1, ChronoUnit.DAYS)
    )

    val bloodPressures: PagingData<BloodPressureHistoryListItem> = PagingData.from(listOf(
        BloodPressureHistoryListItem.BloodPressureHistoryItem(
            measurement = bloodPressureNow,
            isBpEditable = true,
            isBpHigh = false,
            bpDate = "1-Jan-2020",
            bpTime = null
        ),
        BloodPressureHistoryListItem.BloodPressureHistoryItem(
            measurement = bloodPressure15MinutesInPast,
            isBpEditable = false,
            isBpHigh = false,
            bpDate = "31-Dec-2019",
            bpTime = "12:00 AM"
        ),
    ))

    whenever(pagerFactory.createPager(
        sourceFactory = any<PagingSourceFactory<Int, BloodPressureHistoryListItem>>(),
        pageSize = eq(25),
        enablePlaceholders = eq(false),
        initialKey = eq(null),
        cacheScope = eq(pagingCacheScope),
    )) doReturn Observable.just(bloodPressures)

    // when
    testCase.dispatch(LoadBloodPressureHistory(patientUuid))

    // then
    testCase.assertOutgoingEvents(BloodPressuresHistoryLoaded(bloodPressures))
    verifyNoInteractions(uiActions)
  }
}
