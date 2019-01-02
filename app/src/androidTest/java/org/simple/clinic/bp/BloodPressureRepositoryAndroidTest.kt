package org.simple.clinic.bp

import android.support.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.AuthenticationRule
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.util.TestClock
import org.threeten.bp.Clock
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.temporal.ChronoUnit
import org.threeten.bp.temporal.ChronoUnit.*
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class BloodPressureRepositoryAndroidTest {

  @Inject
  lateinit var clock: Clock

  @Inject
  lateinit var appDatabase: org.simple.clinic.AppDatabase

  @Inject
  lateinit var repository: BloodPressureRepository

  @Inject
  lateinit var testData: TestData

  @get:Rule
  val authenticationRule = AuthenticationRule()

  val testClock: TestClock
    get() = clock as TestClock

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
    (clock as TestClock).setYear(2000)
  }

  @Test
  fun updating_a_blood_pressure_should_update_it_correctly() {
    val bloodPressure = testData.bloodPressureMeasurement(
        systolic = 120,
        diastolic = 80,
        createdAt = Instant.now(clock),
        updatedAt = Instant.now(clock),
        syncStatus = SyncStatus.DONE)

    appDatabase.bloodPressureDao().save(listOf(bloodPressure))

    val durationToAdvanceBy = Duration.ofMinutes(15L)
    testClock.advanceBy(durationToAdvanceBy)

    repository.updateMeasurement(bloodPressure.copy(systolic = 130, diastolic = 90)).blockingAwait()

    val expected = bloodPressure.copy(
        systolic = 130,
        diastolic = 90,
        updatedAt = bloodPressure.updatedAt.plus(durationToAdvanceBy),
        syncStatus = SyncStatus.PENDING
    )

    assertThat(appDatabase.bloodPressureDao().getOne(bloodPressure.uuid)!!).isEqualTo(expected)
  }

  @Test
  fun when_fetching_newest_blood_pressure_the_list_should_be_ordered_by_created_at() {
    val patientUuid = UUID.randomUUID()
    val bloodPressure1 = testData.bloodPressureMeasurement(
        patientUuid = patientUuid,
        createdAt = Instant.now(clock))

    val bloodPressure2 = testData.bloodPressureMeasurement(
        patientUuid = patientUuid,
        createdAt = Instant.now(clock).plus(1, DAYS))

    val bloodPressure3 = testData.bloodPressureMeasurement(
        patientUuid = patientUuid,
        createdAt = Instant.now(clock).minus(1, DAYS))

    val bloodPressure4 = testData.bloodPressureMeasurement(
        patientUuid = patientUuid,
        createdAt = Instant.now(clock).plusMillis(1000))

    val bloodPressure5 = testData.bloodPressureMeasurement(
        patientUuid = patientUuid,
        createdAt = Instant.now(clock).minus(10, DAYS))

    appDatabase.bloodPressureDao().save(listOf(bloodPressure1, bloodPressure2, bloodPressure3, bloodPressure4, bloodPressure5))

    val bpMeasurements = repository.newestMeasurementsForPatient(patientUuid, 4).blockingFirst()

    assertThat(bpMeasurements).isEqualTo(listOf(bloodPressure2, bloodPressure4, bloodPressure1, bloodPressure3))
  }

  @Test
  fun deleted_blood_pressures_should_not_be_included_when_fetching_newest_blood_pressures() {
    val patientUuid = UUID.randomUUID()
    val bloodPressure1 = testData.bloodPressureMeasurement(
        patientUuid = patientUuid,
        createdAt = Instant.now(clock))

    val bloodPressure2 = testData.bloodPressureMeasurement(
        patientUuid = patientUuid,
        createdAt = Instant.now(clock).plus(1, DAYS),
        deletedAt = Instant.now(clock).plus(3, DAYS))

    val bloodPressure3 = testData.bloodPressureMeasurement(
        patientUuid = patientUuid,
        createdAt = Instant.now(clock).minus(1, DAYS))

    val bloodPressure4 = testData.bloodPressureMeasurement(
        patientUuid = patientUuid,
        createdAt = Instant.now(clock).plusMillis(1000),
        deletedAt = Instant.now(clock))

    val bloodPressure5 = testData.bloodPressureMeasurement(
        patientUuid = patientUuid,
        createdAt = Instant.now(clock).minus(10, DAYS))

    appDatabase.bloodPressureDao().save(listOf(bloodPressure1, bloodPressure2, bloodPressure3, bloodPressure4, bloodPressure5))

    val bpMeasurements = repository.newestMeasurementsForPatient(patientUuid, 4).blockingFirst()

    assertThat(bpMeasurements).isEqualTo(listOf(bloodPressure1, bloodPressure3, bloodPressure5))
  }

  @Test
  fun marking_a_blood_pressure_as_deleted_should_work_correctly() {
    val now = Instant.now(clock)
    val bloodPressure = testData.bloodPressureMeasurement(
        createdAt = now,
        updatedAt = now,
        deletedAt = null,
        syncStatus = SyncStatus.DONE)

    appDatabase.bloodPressureDao().save(listOf(bloodPressure))

    val durationToAdvanceBy = Duration.ofMinutes(15L)
    testClock.advanceBy(durationToAdvanceBy)

    repository.markBloodPressureAsDeleted(bloodPressure).blockingAwait()

    val timeAtWhichBpWasDeleted = now.plus(durationToAdvanceBy)
    val expected = bloodPressure.copy(
        updatedAt = timeAtWhichBpWasDeleted,
        deletedAt = timeAtWhichBpWasDeleted,
        syncStatus = SyncStatus.PENDING)

    val savedBloodPressure = appDatabase.bloodPressureDao().getOne(expected.uuid)!!

    assertThat(savedBloodPressure).isEqualTo(expected)
  }

  @Test
  fun observing_a_deleted_blood_pressure_as_a_stream_should_work_correctly() {
    val now = Instant.now(clock)
    val bloodPressure = testData.bloodPressureMeasurement(
        createdAt = now,
        updatedAt = now,
        deletedAt = null,
        syncStatus = SyncStatus.DONE)
    appDatabase.bloodPressureDao().save(listOf(bloodPressure))

    val deletedMeasurementObserver = repository.deletedMeasurementAsStream(bloodPressure.uuid)
        .test()

    val fifteenSeconds = Duration.ofSeconds(15L)
    val tenHours = Duration.ofHours(10L)
    val twentyDays = Duration.ofDays(20L)

    listOf(fifteenSeconds, tenHours, twentyDays)
        .map { now.plus(it) }
        .map { bloodPressure.copy(deletedAt = it, updatedAt = it) }
        .forEach { appDatabase.bloodPressureDao().save(listOf(it)) }

    // We cannot verify we received the values exactly because Room does not always emit all the
    // intermediate changes. So we'll either have to add a delay between each update to guarantee
    // that the change notification will be published, or just verify that the last event received
    // is what we expect.
    deletedMeasurementObserver.await(1L, TimeUnit.SECONDS)
    val receivedValues = deletedMeasurementObserver.values()

    assertThat(receivedValues.size).isAtLeast(1)
    assertThat(receivedValues.last())
        .isEqualTo(bloodPressure.copy(deletedAt = now.plus(twentyDays), updatedAt = now.plus(twentyDays)))

    deletedMeasurementObserver.dispose()
  }

  @After
  fun tearDown() {
    (clock as TestClock).resetToEpoch()
  }
}
