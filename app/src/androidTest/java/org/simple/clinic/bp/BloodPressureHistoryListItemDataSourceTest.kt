package org.simple.clinic.bp

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
import org.simple.clinic.bp.history.adapter.BloodPressureHistoryListItem
import org.simple.clinic.bp.history.adapter.BloodPressureHistoryListItem.BloodPressureHistoryItem
import org.simple.clinic.bp.history.adapter.BloodPressureHistoryListItem.NewBpButton
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

class BloodPressureHistoryListItemDataSourceTest {

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

  private val canEditBpFor = Duration.ofMinutes(10)

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
  fun load_initial_should_work_correctly() {
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

    val bloodPressureInPast = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("2b929a33-e543-4a4f-a98e-5d0d3e8e1e03"),
        patientUuid = patientUuid,
        systolic = 120,
        diastolic = 85,
        createdAt = createdAt.minus(15, ChronoUnit.MINUTES),
        recordedAt = recordedAt.minus(1, ChronoUnit.DAYS)
    )

    val params = LoadInitialParams(
        0,
        20,
        20,
        false
    )

    val dataSource = BloodPressureHistoryListItemDataSource(
        appDatabase,
        utcClock,
        userClock,
        dateFormatter,
        timeFormatter,
        canEditBpFor,
        appDatabase.bloodPressureDao().allBloodPressuresDataSource(patientUuid).create() as PositionalDataSource<BloodPressureMeasurement>
    )

    appDatabase.bloodPressureDao().save(listOf(bloodPressureNow, bloodPressureInPast))
    dataSource.loadInitial(params, object : LoadInitialCallback<BloodPressureHistoryListItem>() {
      override fun onResult(
          data: List<BloodPressureHistoryListItem>,
          position: Int,
          totalCount: Int
      ) {
        assertThat(data)
            .containsExactly(
                NewBpButton,
                BloodPressureHistoryItem(measurement = bloodPressureNow, isBpEditable = true, isBpHigh = false, bpDate = "1-Jan-2020", bpTime = null),
                BloodPressureHistoryItem(measurement = bloodPressureInPast, isBpEditable = false, isBpHigh = false, bpDate = "31-Dec-2019", bpTime = null)
            )
      }

      override fun onResult(data: List<BloodPressureHistoryListItem>, position: Int) {
      }
    })
  }

  @Test
  fun load_range_should_work_correctly() {
    val patientUuid = UUID.fromString("a33266b7-0c49-4db0-91e2-9cc60f741bf7")
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

    val bloodPressure40MinutesInPast = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("eaf032e1-64a9-40e8-9fd6-df598ede51c4"),
        patientUuid = patientUuid,
        systolic = 120,
        diastolic = 85,
        createdAt = createdAt.minus(40, ChronoUnit.MINUTES),
        recordedAt = recordedAt.minus(2, ChronoUnit.DAYS)
    )

    val bloodPressure1DayInPast = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("fe434375-4ffa-441c-a4c5-911ebdb29b38"),
        patientUuid = patientUuid,
        systolic = 120,
        diastolic = 85,
        createdAt = createdAt.minus(1, ChronoUnit.DAYS),
        recordedAt = recordedAt.minus(3, ChronoUnit.DAYS)
    )

    val params = LoadRangeParams(
        1,
        3
    )

    val dataSource = BloodPressureHistoryListItemDataSource(
        appDatabase,
        utcClock,
        userClock,
        dateFormatter,
        timeFormatter,
        canEditBpFor,
        appDatabase.bloodPressureDao().allBloodPressuresDataSource(patientUuid).create() as PositionalDataSource<BloodPressureMeasurement>
    )

    appDatabase.bloodPressureDao().save(listOf(bloodPressureNow, bloodPressure15MinutesInPast, bloodPressure40MinutesInPast, bloodPressure1DayInPast))
    dataSource.loadRange(params, object : PositionalDataSource.LoadRangeCallback<BloodPressureHistoryListItem>() {
      override fun onResult(data: List<BloodPressureHistoryListItem>) {
        assertThat(data)
            .containsExactly(
                BloodPressureHistoryItem(measurement = bloodPressureNow, isBpEditable = true, isBpHigh = false, bpDate = "1-Jan-2020", bpTime = null),
                BloodPressureHistoryItem(measurement = bloodPressure15MinutesInPast, isBpEditable = false, isBpHigh = false, bpDate = "31-Dec-2019", bpTime = null),
                BloodPressureHistoryItem(measurement = bloodPressure40MinutesInPast, isBpEditable = false, isBpHigh = false, bpDate = "30-Dec-2019", bpTime = null)
            )
      }
    })
  }

  @Test
  fun new_bp_button_should_be_present_if_load_range_is_starting_from_zero() {
    val patientUuid = UUID.fromString("a33266b7-0c49-4db0-91e2-9cc60f741bf7")
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

    val bloodPressure40MinutesInPast = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("eaf032e1-64a9-40e8-9fd6-df598ede51c4"),
        patientUuid = patientUuid,
        systolic = 120,
        diastolic = 85,
        createdAt = createdAt.minus(40, ChronoUnit.MINUTES),
        recordedAt = recordedAt.minus(2, ChronoUnit.DAYS)
    )

    val bloodPressure1DayInPast = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("fe434375-4ffa-441c-a4c5-911ebdb29b38"),
        patientUuid = patientUuid,
        systolic = 120,
        diastolic = 85,
        createdAt = createdAt.minus(1, ChronoUnit.DAYS),
        recordedAt = recordedAt.minus(3, ChronoUnit.DAYS)
    )

    val params = LoadRangeParams(
        0,
        3
    )

    val dataSource = BloodPressureHistoryListItemDataSource(
        appDatabase,
        utcClock,
        userClock,
        dateFormatter,
        timeFormatter,
        canEditBpFor,
        appDatabase.bloodPressureDao().allBloodPressuresDataSource(patientUuid).create() as PositionalDataSource<BloodPressureMeasurement>
    )

    appDatabase.bloodPressureDao().save(listOf(bloodPressureNow, bloodPressure15MinutesInPast, bloodPressure40MinutesInPast, bloodPressure1DayInPast))
    dataSource.loadRange(params, object : PositionalDataSource.LoadRangeCallback<BloodPressureHistoryListItem>() {
      override fun onResult(data: List<BloodPressureHistoryListItem>) {
        assertThat(data)
            .containsExactly(
                NewBpButton,
                BloodPressureHistoryItem(measurement = bloodPressureNow, isBpEditable = true, isBpHigh = false, bpDate = "1-Jan-2020", bpTime = null),
                BloodPressureHistoryItem(measurement = bloodPressure15MinutesInPast, isBpEditable = false, isBpHigh = false, bpDate = "31-Dec-2019", bpTime = null),
                BloodPressureHistoryItem(measurement = bloodPressure40MinutesInPast, isBpEditable = false, isBpHigh = false, bpDate = "30-Dec-2019", bpTime = null)
            )
      }
    })
  }
}
