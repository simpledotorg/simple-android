package org.simple.clinic.bp

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.testing.TestPager
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.rx2.rxSingle
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.simple.clinic.AppDatabase
import org.simple.clinic.TestClinicApp
import org.simple.clinic.bp.history.adapter.BloodPressureHistoryListItem
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.TestData
import org.simple.clinic.util.Rules
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.TestUtcClock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

class BloodPressureHistoryListItemPagingSourceTest {

  @Inject
  lateinit var appDatabase: AppDatabase

  @Inject
  lateinit var utcClock: TestUtcClock

  @Inject
  lateinit var userClock: TestUserClock

  @Inject
  @Named("full_date")
  lateinit var dateFormatter: DateTimeFormatter

  @Inject
  @Named("time_for_measurement_history")
  lateinit var timeFormatter: DateTimeFormatter

  @Inject
  lateinit var schedulersProvider: SchedulersProvider

  @get:Rule
  val ruleChain: RuleChain = Rules.global()

  @Before
  fun setup() {
    TestClinicApp.appComponent().inject(this)
    utcClock.setDate(LocalDate.parse("2020-01-01"))
  }

  @After
  fun tearDown() {
    appDatabase.bloodPressureDao().clearData()
  }

  @Test
  fun loading_paged_data_should_work_correctly() {
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

    val bloodPressure20MinutesInPast = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("65c32dd7-ab01-4dab-ba83-11f45e1f5fcd"),
        patientUuid = patientUuid,
        systolic = 120,
        diastolic = 85,
        createdAt = createdAt.minus(20, ChronoUnit.MINUTES),
        recordedAt = recordedAt.minus(1, ChronoUnit.DAYS)
    )

    val bloodPressure30MinutesInPast = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("b1ae0aee-a153-43da-8437-e1bc73cf8a61"),
        patientUuid = patientUuid,
        systolic = 120,
        diastolic = 85,
        createdAt = createdAt.minus(30, ChronoUnit.MINUTES),
        recordedAt = recordedAt.minus(1, ChronoUnit.DAYS)
    )

    val bloodPressure40MinutesInPast = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("2096f82f-b58a-42db-b280-c72230fc1a5d"),
        patientUuid = patientUuid,
        systolic = 120,
        diastolic = 85,
        createdAt = createdAt.minus(40, ChronoUnit.MINUTES),
        recordedAt = recordedAt.minus(1, ChronoUnit.DAYS)
    )

    val bloodPressure50MinutesInPast = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("1c132a66-b89e-4406-8f52-65a116395c58"),
        patientUuid = patientUuid,
        systolic = 120,
        diastolic = 85,
        createdAt = createdAt.minus(50, ChronoUnit.MINUTES),
        recordedAt = recordedAt.minus(1, ChronoUnit.DAYS)
    )

    appDatabase.bloodPressureDao().save(listOf(
        bloodPressureNow,
        bloodPressure15MinutesInPast,
        bloodPressure20MinutesInPast,
        bloodPressure30MinutesInPast,
        bloodPressure40MinutesInPast,
        bloodPressure50MinutesInPast,
    ))

    // when
    val pagingSource = BloodPressureHistoryListItemPagingSource(
        appDatabase = appDatabase,
        utcClock = utcClock,
        userClock = userClock,
        bpEditableDuration = Duration.ofMinutes(10),
        schedulersProvider = schedulersProvider,
        dateFormatter = dateFormatter,
        timeFormatter = timeFormatter,
        source = appDatabase.bloodPressureDao().allBloodPressuresPagingSource(patientUuid)
    )

    val pager = TestPager(
        config = PagingConfig(
            pageSize = 5,
            initialLoadSize = 5,
            prefetchDistance = 0,
            enablePlaceholders = true,
        ),
        pagingSource = pagingSource
    )

    // then
    val loadResult = rxSingle { pager.refresh() }.blockingGet() as PagingSource.LoadResult.Page
    assertThat(loadResult.data)
        .containsExactlyElementsIn(listOf(
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
            BloodPressureHistoryListItem.BloodPressureHistoryItem(
                measurement = bloodPressure20MinutesInPast,
                isBpEditable = false,
                isBpHigh = false,
                bpDate = "31-Dec-2019",
                bpTime = "12:00 AM"
            ),
            BloodPressureHistoryListItem.BloodPressureHistoryItem(
                measurement = bloodPressure30MinutesInPast,
                isBpEditable = false,
                isBpHigh = false,
                bpDate = "31-Dec-2019",
                bpTime = "12:00 AM"
            ),
            BloodPressureHistoryListItem.BloodPressureHistoryItem(
                measurement = bloodPressure40MinutesInPast,
                isBpEditable = false,
                isBpHigh = false,
                bpDate = "31-Dec-2019",
                bpTime = "12:00 AM"
            ),
        )).inOrder()

    assertThat(loadResult.data)
        .doesNotContain(BloodPressureHistoryListItem.BloodPressureHistoryItem(
            measurement = bloodPressure50MinutesInPast,
            isBpEditable = false,
            isBpHigh = false,
            bpDate = "31-Dec-2019",
            bpTime = "12:00 AM"
        ))
  }
}
