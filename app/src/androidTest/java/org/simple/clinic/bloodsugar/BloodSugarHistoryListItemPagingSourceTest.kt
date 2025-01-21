package org.simple.clinic.bloodsugar

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
import org.simple.clinic.bloodsugar.history.adapter.BloodSugarHistoryListItem
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

class BloodSugarHistoryListItemPagingSourceTest {
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

  private val canEditBloodSugarFor = Duration.ofMinutes(10)
  private val bloodSugarUnitPreference = BloodSugarUnitPreference.Mg

  @Before
  fun setup() {
    TestClinicApp.appComponent().inject(this)
    utcClock.setDate(LocalDate.parse("2020-01-01"))
  }

  @After
  fun tearDown() {
    appDatabase.bloodSugarDao().clear()
  }

  @Test
  fun load_paged_data_should_work_correctly() {
    val patientUuid = UUID.fromString("f6f60760-b290-4e0f-9db0-74179b7cd170")
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

    val bloodSugar20MinutesInPast = TestData.bloodSugarMeasurement(
        uuid = UUID.fromString("e1f40c2f-3737-4cc6-843d-9eecf543d008"),
        patientUuid = patientUuid,
        createdAt = createdAt.minus(20, ChronoUnit.MINUTES),
        recordedAt = recordedAt.minus(1, ChronoUnit.DAYS)
    )

    val bloodSugar30MinutesInPast = TestData.bloodSugarMeasurement(
        uuid = UUID.fromString("5fe4cad4-4652-48d6-b1bf-67cb630f2fe9"),
        patientUuid = patientUuid,
        createdAt = createdAt.minus(30, ChronoUnit.MINUTES),
        recordedAt = recordedAt.minus(1, ChronoUnit.DAYS)
    )

    val bloodSugar40MinutesInPast = TestData.bloodSugarMeasurement(
        uuid = UUID.fromString("aa55ff64-2574-4426-97c6-40fed8dc7db6"),
        patientUuid = patientUuid,
        createdAt = createdAt.minus(40, ChronoUnit.MINUTES),
        recordedAt = recordedAt.minus(1, ChronoUnit.DAYS)
    )

    val bloodSugar45MinutesInPast = TestData.bloodSugarMeasurement(
        uuid = UUID.fromString("ce4972af-e59f-4485-a6a7-5f00cb55870b"),
        patientUuid = patientUuid,
        createdAt = createdAt.minus(45, ChronoUnit.MINUTES),
        recordedAt = recordedAt.minus(1, ChronoUnit.DAYS)
    )

    appDatabase.bloodSugarDao().save(listOf(
        bloodSugarNow,
        bloodSugar15MinutesInPast,
        bloodSugar20MinutesInPast,
        bloodSugar30MinutesInPast,
        bloodSugar40MinutesInPast,
        bloodSugar45MinutesInPast,
    ))

    // when
    val pagingSource = BloodSugarHistoryListItemPagingSource(
        appDatabase = appDatabase,
        utcClock = utcClock,
        userClock = userClock,
        canEditFor = canEditBloodSugarFor,
        schedulersProvider = schedulersProvider,
        dateFormatter = dateFormatter,
        timeFormatter = timeFormatter,
        source = appDatabase.bloodSugarDao().allBloodSugarsPagingSource(patientUuid),
        bloodSugarUnitPreference = bloodSugarUnitPreference
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
            BloodSugarHistoryListItem.BloodSugarHistoryItem(
                measurement = bloodSugarNow,
                isBloodSugarEditable = true,
                bloodSugarUnitPreference = bloodSugarUnitPreference,
                bloodSugarDate = "1-Jan-2020",
                bloodSugarTime = null
            ),
            BloodSugarHistoryListItem.BloodSugarHistoryItem(
                measurement = bloodSugar15MinutesInPast,
                bloodSugarUnitPreference = bloodSugarUnitPreference,
                isBloodSugarEditable = false,
                bloodSugarDate = "31-Dec-2019",
                bloodSugarTime = "12:00 AM"
            ),
            BloodSugarHistoryListItem.BloodSugarHistoryItem(
                measurement = bloodSugar20MinutesInPast,
                isBloodSugarEditable = false,
                bloodSugarUnitPreference = bloodSugarUnitPreference,
                bloodSugarDate = "31-Dec-2019",
                bloodSugarTime = "12:00 AM"
            ),
            BloodSugarHistoryListItem.BloodSugarHistoryItem(
                measurement = bloodSugar30MinutesInPast,
                isBloodSugarEditable = false,
                bloodSugarUnitPreference = bloodSugarUnitPreference,
                bloodSugarDate = "31-Dec-2019",
                bloodSugarTime = "12:00 AM"
            ),
            BloodSugarHistoryListItem.BloodSugarHistoryItem(
                measurement = bloodSugar40MinutesInPast,
                isBloodSugarEditable = false,
                bloodSugarUnitPreference = bloodSugarUnitPreference,
                bloodSugarDate = "31-Dec-2019",
                bloodSugarTime = "12:00 AM"
            ),
        )).inOrder()

    assertThat(loadResult.data)
        .doesNotContain(BloodSugarHistoryListItem.BloodSugarHistoryItem(
            measurement = bloodSugar45MinutesInPast,
            isBloodSugarEditable = false,
            bloodSugarUnitPreference = bloodSugarUnitPreference,
            bloodSugarDate = "31-Dec-2019",
            bloodSugarTime = "12:00 AM"
        ))
  }
}
