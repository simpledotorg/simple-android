package org.simple.clinic.bp

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.simple.clinic.TestClinicApp
import org.simple.sharedTestCode.TestData
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.PatientStatus
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.rules.LocalAuthenticationRule
import org.simple.clinic.rules.SaveDatabaseRule
import org.simple.clinic.user.User
import org.simple.sharedTestCode.util.Rules
import org.simple.sharedTestCode.util.TestUtcClock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.time.temporal.ChronoUnit.DAYS
import java.util.UUID
import javax.inject.Inject


class BloodPressureRepositoryAndroidTest {

  @Inject
  lateinit var clock: TestUtcClock

  @Inject
  lateinit var appDatabase: org.simple.clinic.AppDatabase

  @Inject
  lateinit var repository: BloodPressureRepository

  @Inject
  lateinit var testData: TestData

  @Inject
  lateinit var user: User

  @Inject
  lateinit var facility: Facility

  @Inject
  lateinit var patientRepository: PatientRepository

  @get:Rule
  val ruleChain: RuleChain = Rules
      .global()
      .around(LocalAuthenticationRule())
      .around(SaveDatabaseRule())

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
    clock.setDate(LocalDate.of(2000, Month.JANUARY, 1))
  }

  @Test
  fun saving_a_blood_pressure_with_an_older_recorded_time_should_set_the_updated_time_to_the_current_time() {
    val now = Instant.now(clock)
    val oneWeek = Duration.ofDays(7L)
    clock.advanceBy(oneWeek)

    val savedBloodPressure = repository
        .saveMeasurement(patientUuid = UUID.fromString("a0d7f00b-9d2a-4594-b2e0-9f12285b8f03"),
            reading = BloodPressureReading(120, 80),
            loggedInUser = user,
            currentFacility = facility,
            recordedAt = now,
            uuid = UUID.fromString("cd2194f6-c662-4814-86fc-a813efb9b006"))

    assertThat(savedBloodPressure.recordedAt).isEqualTo(now)
    assertThat(savedBloodPressure.updatedAt).isEqualTo(now.plus(oneWeek))
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
    clock.advanceBy(durationToAdvanceBy)
    val newReading = BloodPressureReading(130, 90)

    repository.updateMeasurement(bloodPressure.copy(reading = newReading))

    val expected = bloodPressure.copy(
        reading = newReading,
        updatedAt = bloodPressure.updatedAt.plus(durationToAdvanceBy),
        syncStatus = SyncStatus.PENDING
    )

    assertThat(appDatabase.bloodPressureDao().getOne(bloodPressure.uuid)!!).isEqualTo(expected)
  }

  @Test
  fun when_fetching_newest_blood_pressure_the_list_should_be_ordered_by_recorded_at() {
    val patientUuid = UUID.randomUUID()
    val bloodPressure1 = testData.bloodPressureMeasurement(
        patientUuid = patientUuid,
        recordedAt = Instant.now(clock))

    val bloodPressure2 = testData.bloodPressureMeasurement(
        patientUuid = patientUuid,
        recordedAt = Instant.now(clock).plus(1, DAYS))

    val bloodPressure3 = testData.bloodPressureMeasurement(
        patientUuid = patientUuid,
        recordedAt = Instant.now(clock).minus(1, DAYS))

    val bloodPressure4 = testData.bloodPressureMeasurement(
        patientUuid = patientUuid,
        recordedAt = Instant.now(clock).plusMillis(1000))

    val bloodPressure5 = testData.bloodPressureMeasurement(
        patientUuid = patientUuid,
        recordedAt = Instant.now(clock).minus(10, DAYS))

    appDatabase.bloodPressureDao().save(listOf(bloodPressure1, bloodPressure2, bloodPressure3, bloodPressure4, bloodPressure5))

    val bpMeasurements = repository.newestMeasurementsForPatient(patientUuid, 4).blockingFirst()

    assertThat(bpMeasurements).isEqualTo(listOf(bloodPressure2, bloodPressure4, bloodPressure1, bloodPressure3))
  }

  @Test
  fun deleted_blood_pressures_should_not_be_included_when_fetching_newest_blood_pressures() {
    val patientUuid = UUID.randomUUID()
    val bloodPressure1 = testData.bloodPressureMeasurement(
        patientUuid = patientUuid,
        recordedAt = Instant.now(clock))

    val bloodPressure2 = testData.bloodPressureMeasurement(
        patientUuid = patientUuid,
        recordedAt = Instant.now(clock).plus(1, DAYS),
        deletedAt = Instant.now(clock).plus(3, DAYS))

    val bloodPressure3 = testData.bloodPressureMeasurement(
        patientUuid = patientUuid,
        recordedAt = Instant.now(clock).minus(1, DAYS))

    val bloodPressure4 = testData.bloodPressureMeasurement(
        patientUuid = patientUuid,
        recordedAt = Instant.now(clock).plusMillis(1000),
        deletedAt = Instant.now(clock))

    val bloodPressure5 = testData.bloodPressureMeasurement(
        patientUuid = patientUuid,
        recordedAt = Instant.now(clock).minus(10, DAYS))

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
    clock.advanceBy(durationToAdvanceBy)

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
  fun getting_the_blood_pressure_count_immediate_for_a_patient_should_work_correctly() {
    val patientUuidWithOnlyDeletedBloodPressures = UUID.randomUUID()
    val patientUuidWithBloodPressures = UUID.randomUUID()

    val now = Instant.now(clock)
    val bpsForPatientWithOnlyDeletedBloodPressures = listOf(
        testData.bloodPressureMeasurement(patientUuid = patientUuidWithOnlyDeletedBloodPressures, deletedAt = now),
        testData.bloodPressureMeasurement(patientUuid = patientUuidWithOnlyDeletedBloodPressures, deletedAt = now),
        testData.bloodPressureMeasurement(patientUuid = patientUuidWithOnlyDeletedBloodPressures, deletedAt = now)
    )

    val bpsForPatientWithBloodPressures = listOf(
        testData.bloodPressureMeasurement(patientUuid = patientUuidWithBloodPressures, deletedAt = now),
        testData.bloodPressureMeasurement(patientUuid = patientUuidWithBloodPressures),
        testData.bloodPressureMeasurement(patientUuid = patientUuidWithBloodPressures, deletedAt = now)
    )

    appDatabase.bloodPressureDao().save(bpsForPatientWithOnlyDeletedBloodPressures + bpsForPatientWithBloodPressures)
    assertThat(appDatabase.bloodPressureDao().count().blockingFirst()).isEqualTo(6)

    assertThat(repository.bloodPressureCountImmediate(patientUuidWithOnlyDeletedBloodPressures)).isEqualTo(0)
    assertThat(repository.bloodPressureCountImmediate(patientUuidWithBloodPressures)).isEqualTo(1)
  }

  @Test
  fun getting_then_blood_pressure_count_for_a_patient_should_work_correctly() {
    val patientUuidWithOnlyDeletedBloodPressures = UUID.randomUUID()
    val patientUuidWithBloodPressures = UUID.randomUUID()

    val now = Instant.now(clock)
    val bpsForPatientWithOnlyDeletedBloodPressures = listOf(
        testData.bloodPressureMeasurement(patientUuid = patientUuidWithOnlyDeletedBloodPressures, deletedAt = now),
        testData.bloodPressureMeasurement(patientUuid = patientUuidWithOnlyDeletedBloodPressures, deletedAt = now),
        testData.bloodPressureMeasurement(patientUuid = patientUuidWithOnlyDeletedBloodPressures, deletedAt = now)
    )

    val bpsForPatientWithBloodPressures = listOf(
        testData.bloodPressureMeasurement(patientUuid = patientUuidWithBloodPressures, deletedAt = now),
        testData.bloodPressureMeasurement(patientUuid = patientUuidWithBloodPressures),
        testData.bloodPressureMeasurement(patientUuid = patientUuidWithBloodPressures, deletedAt = now)
    )

    appDatabase.bloodPressureDao().save(bpsForPatientWithOnlyDeletedBloodPressures + bpsForPatientWithBloodPressures)
    assertThat(appDatabase.bloodPressureDao().count().blockingFirst()).isEqualTo(6)

    assertThat(repository.bloodPressureCount(patientUuidWithOnlyDeletedBloodPressures).blockingFirst()).isEqualTo(0)
    assertThat(repository.bloodPressureCount(patientUuidWithBloodPressures).blockingFirst()).isEqualTo(1)
  }

  @Test
  fun when_fetching_all_blood_pressure_the_list_should_be_ordered_by_recorded_at() {
    val patientUuid = UUID.fromString("b5c190d1-3d99-40ce-aef1-d0c82270f834")
    val bpRecordRightNow = testData.bloodPressureMeasurement(
        patientUuid = patientUuid,
        recordedAt = Instant.now(clock))

    val bpRecordADayInFuture = testData.bloodPressureMeasurement(
        patientUuid = patientUuid,
        recordedAt = Instant.now(clock).plus(1, DAYS))

    val bpRecordADayInPast = testData.bloodPressureMeasurement(
        patientUuid = patientUuid,
        recordedAt = Instant.now(clock).minus(1, DAYS))

    appDatabase.bloodPressureDao().save(listOf(bpRecordRightNow, bpRecordADayInFuture, bpRecordADayInPast))

    val bpMeasurements = repository.allBloodPressures(patientUuid).blockingFirst()

    assertThat(bpMeasurements)
        .isEqualTo(listOf(
            bpRecordADayInFuture,
            bpRecordRightNow,
            bpRecordADayInPast
        ))
  }

  @Test
  fun checking_if_newest_blood_pressure_entry_is_high_for_a_patient_should_work_correctly() {
    // given
    val patient1Uuid = UUID.fromString("b8060a91-911e-4695-b905-20ff0d3fb8d5")
    val patient1 = TestData.patientProfile(
        patientUuid = patient1Uuid,
        patientStatus = PatientStatus.Active,
        patientDeletedAt = null,
        patientCreatedAt = Instant.parse("1999-10-01T00:00:00Z"),
        patientUpdatedAt = Instant.parse("1999-10-01T00:00:00Z"),
        patientRecordedAt = Instant.parse("1999-10-01T00:00:00Z")
    )

    val patient2Uuid = UUID.fromString("414ed968-3972-4723-8613-5b0b0ed65a5a")
    val patient2 = TestData.patientProfile(
        patientUuid = patient2Uuid,
        patientStatus = PatientStatus.Active,
        patientDeletedAt = null,
        patientCreatedAt = Instant.parse("1999-10-01T00:00:00Z"),
        patientUpdatedAt = Instant.parse("1999-10-01T00:00:00Z"),
        patientRecordedAt = Instant.parse("1999-10-01T00:00:00Z")
    )

    val patient3Uuid = UUID.fromString("df519ae8-1b0a-4d81-b916-f7bb26faa1ba")
    val patient3 = TestData.patientProfile(
        patientUuid = patient3Uuid,
        patientStatus = PatientStatus.Active,
        patientDeletedAt = null,
        patientCreatedAt = Instant.parse("1999-10-01T00:00:00Z"),
        patientUpdatedAt = Instant.parse("1999-10-01T00:00:00Z"),
        patientRecordedAt = Instant.parse("1999-10-01T00:00:00Z")
    )

    val twoMonthsOldBpForPatient1 = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("cfeef99e-8477-4c78-9b81-b205b0b6d50d"),
        patientUuid = patient1Uuid,
        systolic = 145,
        diastolic = 90,
        createdAt = Instant.parse("1999-10-01T00:00:00Z"),
        recordedAt = Instant.parse("1999-10-01T00:00:00Z"),
        updatedAt = Instant.parse("1999-10-01T00:00:00Z"),
        deletedAt = null
    )
    val deletedOneMonthOldBpForPatient1 = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("3e383092-f2f0-4b88-abd6-4253d710080a"),
        patientUuid = patient1Uuid,
        systolic = 132,
        diastolic = 85,
        createdAt = Instant.parse("1999-11-01T00:00:00Z"),
        recordedAt = Instant.parse("1999-11-01T00:00:00Z"),
        updatedAt = Instant.parse("1999-11-01T00:00:00Z"),
        deletedAt = Instant.parse("1999-11-01T00:00:00Z")
    )
    val oneMonthOldBpForPatient1 = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("53134f7b-3acf-4ea6-acde-505cd45851da"),
        patientUuid = patient1Uuid,
        systolic = 130,
        diastolic = 85,
        createdAt = Instant.parse("1999-12-01T00:00:00Z"),
        recordedAt = Instant.parse("1999-12-01T00:00:00Z"),
        updatedAt = Instant.parse("1999-12-01T00:00:00Z"),
        deletedAt = null
    )
    val newestBpForPatient1 = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("89888e1f-e02d-47a1-8ad3-bf6e60021a92"),
        patientUuid = patient1Uuid,
        systolic = 145,
        diastolic = 90,
        createdAt = Instant.parse("2000-01-01T00:00:00Z"),
        recordedAt = Instant.parse("2000-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2000-01-01T00:00:00Z"),
        deletedAt = null
    )

    val oneMonthOldBpForPatient2 = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("dd280b11-939c-4c1f-975f-8485d7562c21"),
        patientUuid = patient2Uuid,
        systolic = 150,
        diastolic = 92,
        createdAt = Instant.parse("1999-12-01T00:00:00Z"),
        recordedAt = Instant.parse("1999-12-01T00:00:00Z"),
        updatedAt = Instant.parse("1999-12-01T00:00:00Z"),
        deletedAt = null
    )

    val newestBpForPatient2 = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("e2026b41-9418-4126-ab81-e6d83465087b"),
        patientUuid = patient2Uuid,
        systolic = 120,
        diastolic = 80,
        createdAt = Instant.parse("2000-01-01T00:00:00Z"),
        recordedAt = Instant.parse("2000-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2000-01-01T00:00:00Z"),
        deletedAt = null
    )

    val twoMinutesOldBpForPatient3 = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("1beac089-6c6e-44db-9e3b-0af4c4144dbb"),
        patientUuid = patient3Uuid,
        systolic = 150,
        diastolic = 92,
        createdAt = Instant.parse("2000-01-01T00:02:00Z"),
        recordedAt = Instant.parse("2000-01-01T00:02:00Z"),
        updatedAt = Instant.parse("2000-01-01T00:02:00Z"),
        deletedAt = null
    )

    val newestBpForPatient3 = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("3d948d3b-6d8f-4608-bde0-d8750fef75d4"),
        patientUuid = patient3Uuid,
        systolic = 150,
        diastolic = 90,
        createdAt = Instant.parse("2000-01-01T00:03:00Z"),
        recordedAt = Instant.parse("2000-01-01T00:03:00Z"),
        updatedAt = Instant.parse("2000-01-01T00:03:00Z"),
        deletedAt = null
    )

    patientRepository.save(listOf(patient1, patient2, patient3))

    repository.save(listOf(
        twoMonthsOldBpForPatient1,
        deletedOneMonthOldBpForPatient1,
        oneMonthOldBpForPatient1,
        newestBpForPatient1,
        oneMonthOldBpForPatient2,
        newestBpForPatient2,
        twoMinutesOldBpForPatient3,
        newestBpForPatient3
    ))

    // when
    val isNewestBpEntryHighForPatient1 = repository.isNewestBpEntryHigh(patient1Uuid).blockingFirst()
    val isNewestBpEntryHighForPatient2 = repository.isNewestBpEntryHigh(patient2Uuid).blockingFirst()
    val isNewestBpEntryHighForPatient3 = repository.isNewestBpEntryHigh(patient3Uuid).blockingFirst()

    // then
    assertThat(isNewestBpEntryHighForPatient1).isTrue()
    assertThat(isNewestBpEntryHighForPatient2).isFalse()
    assertThat(isNewestBpEntryHighForPatient3).isTrue()
  }
}
