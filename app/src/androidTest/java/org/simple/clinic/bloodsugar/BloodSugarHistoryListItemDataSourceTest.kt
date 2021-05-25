package org.simple.clinic.bloodsugar

import androidx.paging.PositionalDataSource
import androidx.paging.PositionalDataSource.LoadInitialCallback
import androidx.paging.PositionalDataSource.LoadInitialParams
import androidx.paging.PositionalDataSource.LoadRangeParams
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.simple.clinic.AppDatabase
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.bloodsugar.history.adapter.BloodSugarHistoryListItem
import org.simple.clinic.bloodsugar.history.adapter.BloodSugarHistoryListItem.BloodSugarHistoryItem
import org.simple.clinic.bloodsugar.history.adapter.BloodSugarHistoryListItem.NewBloodSugarButton
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

class BloodSugarHistoryListItemDataSourceTest {

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
  fun load_initial_should_work_correctly() {
    val patientUuid = UUID.fromString("f6f60760-b290-4e0f-9db0-74179b7cd170")
    val createdAt = Instant.parse("2020-01-01T00:00:00Z")
    val recordedAt = Instant.parse("2020-01-01T00:00:00Z")
    val bloodSugarNow = TestData.bloodSugarMeasurement(
        uuid = UUID.fromString("78e876ae-3055-43d6-a132-7ad5dd930e23"),
        patientUuid = patientUuid,
        createdAt = createdAt,
        recordedAt = recordedAt
    )

    val bloodSugarInPast = TestData.bloodSugarMeasurement(
        uuid = UUID.fromString("2b929a33-e543-4a4f-a98e-5d0d3e8e1e03"),
        patientUuid = patientUuid,
        createdAt = createdAt.minus(15, ChronoUnit.MINUTES),
        recordedAt = recordedAt.minus(1, ChronoUnit.DAYS)
    )

    val params = LoadInitialParams(
        0,
        20,
        20,
        false
    )

    val dataSource = BloodSugarHistoryListItemDataSource(
        appDatabase,
        utcClock,
        userClock,
        dateFormatter,
        timeFormatter,
        canEditBloodSugarFor,
        appDatabase.bloodSugarDao().allBloodSugarsDataSource(patientUuid).create() as PositionalDataSource<BloodSugarMeasurement>,
        bloodSugarUnitPreference
    )

    appDatabase.bloodSugarDao().save(listOf(bloodSugarNow, bloodSugarInPast))
    dataSource.loadInitial(params, object : LoadInitialCallback<BloodSugarHistoryListItem>() {
      override fun onResult(
          data: MutableList<BloodSugarHistoryListItem>,
          position: Int,
          totalCount: Int
      ) {
        assertThat(data)
            .containsExactly(
                NewBloodSugarButton,
                BloodSugarHistoryItem(measurement = bloodSugarNow, bloodSugarDate = "1-Jan-2020", bloodSugarTime = null, isBloodSugarEditable = true, bloodSugarUnitPreference = bloodSugarUnitPreference),
                BloodSugarHistoryItem(measurement = bloodSugarInPast, bloodSugarDate = "31-Dec-2019", bloodSugarTime = null, isBloodSugarEditable = false, bloodSugarUnitPreference = bloodSugarUnitPreference)
            )
      }

      override fun onResult(data: MutableList<BloodSugarHistoryListItem>, position: Int) {
      }
    })
  }

  @Test
  fun load_range_should_work_correctly() {
    val patientUuid = UUID.fromString("a33266b7-0c49-4db0-91e2-9cc60f741bf7")
    val createdAt = Instant.parse("2020-01-01T00:00:00Z")
    val recordedAt = Instant.parse("2020-01-01T00:00:00Z")
    val bloodSugarNow = TestData.bloodSugarMeasurement(
        uuid = UUID.fromString("78e876ae-3055-43d6-a132-7ad5dd930e23"),
        patientUuid = patientUuid,
        createdAt = createdAt,
        recordedAt = recordedAt
    )

    val bloodSugar15MinutesInPast = TestData.bloodSugarMeasurement(
        uuid = UUID.fromString("2b929a33-e543-4a4f-a98e-5d0d3e8e1e03"),
        patientUuid = patientUuid,
        createdAt = createdAt.minus(15, ChronoUnit.MINUTES),
        recordedAt = recordedAt.minus(1, ChronoUnit.DAYS)
    )

    val bloodSugar40MinutesInPast = TestData.bloodSugarMeasurement(
        uuid = UUID.fromString("eaf032e1-64a9-40e8-9fd6-df598ede51c4"),
        patientUuid = patientUuid,
        createdAt = createdAt.minus(40, ChronoUnit.MINUTES),
        recordedAt = recordedAt.minus(2, ChronoUnit.DAYS)
    )

    val bloodSugar1DayInPast = TestData.bloodSugarMeasurement(
        uuid = UUID.fromString("fe434375-4ffa-441c-a4c5-911ebdb29b38"),
        patientUuid = patientUuid,
        createdAt = createdAt.minus(1, ChronoUnit.DAYS),
        recordedAt = recordedAt.minus(3, ChronoUnit.DAYS)
    )

    val params = LoadRangeParams(
        1,
        3
    )

    val dataSource = BloodSugarHistoryListItemDataSource(
        appDatabase,
        utcClock,
        userClock,
        dateFormatter,
        timeFormatter,
        canEditBloodSugarFor,
        appDatabase.bloodSugarDao().allBloodSugarsDataSource(patientUuid).create() as PositionalDataSource<BloodSugarMeasurement>,
        bloodSugarUnitPreference
    )

    appDatabase.bloodSugarDao().save(listOf(bloodSugarNow, bloodSugar15MinutesInPast, bloodSugar40MinutesInPast, bloodSugar1DayInPast))
    dataSource.loadRange(params, object : PositionalDataSource.LoadRangeCallback<BloodSugarHistoryListItem>() {
      override fun onResult(data: MutableList<BloodSugarHistoryListItem>) {
        assertThat(data)
            .containsExactly(
                BloodSugarHistoryItem(measurement = bloodSugarNow, bloodSugarDate = "1-Jan-2020", bloodSugarTime = null, isBloodSugarEditable = true, bloodSugarUnitPreference = bloodSugarUnitPreference),
                BloodSugarHistoryItem(measurement = bloodSugar15MinutesInPast, bloodSugarDate = "31-Dec-2019", bloodSugarTime = null, isBloodSugarEditable = false, bloodSugarUnitPreference = bloodSugarUnitPreference),
                BloodSugarHistoryItem(measurement = bloodSugar40MinutesInPast, bloodSugarDate = "30-Dec-2019", bloodSugarTime = null, isBloodSugarEditable = false, bloodSugarUnitPreference = bloodSugarUnitPreference)
            )
      }
    })
  }

  @Test
  fun new_blood_sugar_button_should_be_present_if_load_range_is_starting_from_zero() {
    val patientUuid = UUID.fromString("a33266b7-0c49-4db0-91e2-9cc60f741bf7")
    val createdAt = Instant.parse("2020-01-01T00:00:00Z")
    val recordedAt = Instant.parse("2020-01-01T00:00:00Z")
    val bloodSugarNow = TestData.bloodSugarMeasurement(
        uuid = UUID.fromString("78e876ae-3055-43d6-a132-7ad5dd930e23"),
        patientUuid = patientUuid,
        createdAt = createdAt,
        recordedAt = recordedAt
    )

    val bloodSugar15MinutesInPast = TestData.bloodSugarMeasurement(
        uuid = UUID.fromString("2b929a33-e543-4a4f-a98e-5d0d3e8e1e03"),
        patientUuid = patientUuid,
        createdAt = createdAt.minus(15, ChronoUnit.MINUTES),
        recordedAt = recordedAt.minus(1, ChronoUnit.DAYS)
    )

    val bloodSugar40MinutesInPast = TestData.bloodSugarMeasurement(
        uuid = UUID.fromString("eaf032e1-64a9-40e8-9fd6-df598ede51c4"),
        patientUuid = patientUuid,
        createdAt = createdAt.minus(40, ChronoUnit.MINUTES),
        recordedAt = recordedAt.minus(2, ChronoUnit.DAYS)
    )

    val bloodSugar1DayInPast = TestData.bloodSugarMeasurement(
        uuid = UUID.fromString("fe434375-4ffa-441c-a4c5-911ebdb29b38"),
        patientUuid = patientUuid,
        createdAt = createdAt.minus(1, ChronoUnit.DAYS),
        recordedAt = recordedAt.minus(3, ChronoUnit.DAYS)
    )

    val params = LoadRangeParams(
        0,
        3
    )

    val dataSource = BloodSugarHistoryListItemDataSource(
        appDatabase,
        utcClock,
        userClock,
        dateFormatter,
        timeFormatter,
        canEditBloodSugarFor,
        appDatabase.bloodSugarDao().allBloodSugarsDataSource(patientUuid).create() as PositionalDataSource<BloodSugarMeasurement>,
        bloodSugarUnitPreference
    )

    appDatabase.bloodSugarDao().save(listOf(bloodSugarNow, bloodSugar15MinutesInPast, bloodSugar40MinutesInPast, bloodSugar1DayInPast))
    dataSource.loadRange(params, object : PositionalDataSource.LoadRangeCallback<BloodSugarHistoryListItem>() {
      override fun onResult(data: MutableList<BloodSugarHistoryListItem>) {
        assertThat(data)
            .containsExactly(
                NewBloodSugarButton,
                BloodSugarHistoryItem(measurement = bloodSugarNow, bloodSugarDate = "1-Jan-2020", bloodSugarTime = null, isBloodSugarEditable = true, bloodSugarUnitPreference = bloodSugarUnitPreference),
                BloodSugarHistoryItem(measurement = bloodSugar15MinutesInPast, bloodSugarDate = "31-Dec-2019", bloodSugarTime = null, isBloodSugarEditable = false, bloodSugarUnitPreference = bloodSugarUnitPreference),
                BloodSugarHistoryItem(measurement = bloodSugar40MinutesInPast, bloodSugarDate = "30-Dec-2019", bloodSugarTime = null, isBloodSugarEditable = false, bloodSugarUnitPreference = bloodSugarUnitPreference)
            )
      }
    })
  }
}
