package org.simple.clinic.bloodsugar.history

import androidx.paging.PagingData
import com.f2prateek.rx.preferences2.Preference
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
import org.simple.clinic.bloodsugar.BloodSugarRepository
import org.simple.clinic.bloodsugar.BloodSugarUnitPreference
import org.simple.clinic.bloodsugar.history.adapter.BloodSugarHistoryListItem
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.summary.bloodsugar.BloodSugarSummaryConfig
import org.simple.clinic.util.PagerFactory
import org.simple.clinic.util.PagingSourceFactory
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.TestData
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Optional
import java.util.UUID

class BloodSugarHistoryScreenEffectHandlerTest {
  private val patientRepository = mock<PatientRepository>()
  private val bloodSugarRepository = mock<BloodSugarRepository>()
  private val patientUuid = UUID.fromString("1d695883-54cf-4cf0-8795-43f83a0c3f02")
  private val uiActions = mock<BloodSugarHistoryScreenUiActions>()
  private val viewEffectHandler = BloodSugarHistoryScreenViewEffectHandler(uiActions)
  private val pagerFactory = mock<PagerFactory>()
  private val bloodSugarUnitPreference: Preference<BloodSugarUnitPreference> = mock()
  private val pagingCacheScope = TestScope()
  private val effectHandler = BloodSugarHistoryScreenEffectHandler(
      patientRepository,
      bloodSugarRepository,
      TestSchedulersProvider.trampoline(),
      pagerFactory = pagerFactory,
      pagingSourceFactory = mock(),
      config = BloodSugarSummaryConfig(
          bloodSugarEditableDuration = Duration.ofMinutes(10),
          numberOfBloodSugarsToDisplay = 0,
      ),
      bloodSugarUnitPreference = bloodSugarUnitPreference,
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
    val patient = TestData.patient(uuid = patientUuid)
    whenever(patientRepository.patient(patientUuid)) doReturn Observable.just(Optional.of(patient))

    // when
    testCase.dispatch(LoadPatient(patientUuid))

    // then
    testCase.assertOutgoingEvents(PatientLoaded(patient))
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

  @Test
  fun `when load blood sugar history effect is received, then load blood sugar history`() {
    // given
    val patientUuid = UUID.fromString("12515571-10ac-411c-9f65-7a5a91e02538")
    val createdAt = Instant.parse("2020-01-01T00:00:00Z")
    val recordedAt = Instant.parse("2020-01-01T00:00:00Z")

    val bloodSugarNow = TestData.bloodSugarMeasurement(
        uuid = UUID.fromString("375cc86f-c582-43dd-aa0f-c06d73ea954b"),
        patientUuid = patientUuid,
        createdAt = createdAt,
        recordedAt = recordedAt
    )

    val bloodSugar15MinutesInPast = TestData.bloodSugarMeasurement(
        uuid = UUID.fromString("6495f97d-b1a9-42f6-9fb5-8d267e3e0633"),
        patientUuid = patientUuid,
        createdAt = createdAt.minus(15, ChronoUnit.MINUTES),
        recordedAt = recordedAt.minus(1, ChronoUnit.DAYS)
    )

    val bloodSugars: PagingData<BloodSugarHistoryListItem> = PagingData.from(listOf(
        BloodSugarHistoryListItem.BloodSugarHistoryItem(
            measurement = bloodSugarNow,
            isBloodSugarEditable = true,
            bloodSugarUnitPreference = BloodSugarUnitPreference.Mg,
            bloodSugarDate = "1-Jan-2020",
            bloodSugarTime = null
        ),
        BloodSugarHistoryListItem.BloodSugarHistoryItem(
            measurement = bloodSugar15MinutesInPast,
            bloodSugarUnitPreference = BloodSugarUnitPreference.Mg,
            isBloodSugarEditable = false,
            bloodSugarDate = "31-Dec-2019",
            bloodSugarTime = "12:00 AM"
        ),
    ))

    whenever(pagerFactory.createPager(
        sourceFactory = any<PagingSourceFactory<Int, BloodSugarHistoryListItem>>(),
        pageSize = eq(25),
        enablePlaceholders = eq(false),
        initialKey = eq(null),
        cacheScope = eq(pagingCacheScope),
    )) doReturn Observable.just(bloodSugars)

    // when
    testCase.dispatch(LoadBloodSugarHistory(patientUuid))

    // then
    testCase.assertOutgoingEvents(BloodSugarHistoryLoaded(bloodSugars))
    verifyNoInteractions(uiActions)
  }
}
