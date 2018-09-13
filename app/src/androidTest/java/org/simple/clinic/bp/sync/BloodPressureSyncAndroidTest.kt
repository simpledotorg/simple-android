package org.simple.clinic.bp.sync

import android.support.test.runner.AndroidJUnit4
import com.f2prateek.rx.preferences2.Preference
import com.google.common.truth.Truth.assertThat
import io.bloco.faker.Faker
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.AppDatabase
import org.simple.clinic.AuthenticationRule
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientStatus
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Just
import org.simple.clinic.util.Optional
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

@RunWith(AndroidJUnit4::class)
class BloodPressureSyncAndroidTest {

  @Inject
  lateinit var configProvider: Single<SyncConfig>

  @Inject
  lateinit var repository: BloodPressureRepository

  @Inject
  lateinit var userSession: UserSession

  @Inject
  lateinit var database: AppDatabase

  @Inject
  @field:Named("last_bp_pull_timestamp")
  lateinit var lastPullTimestamp: Preference<Optional<Instant>>

  @Inject
  lateinit var bpSync: BloodPressureSync

  @Inject
  lateinit var faker: Faker

  @Inject
  lateinit var testData: TestData

  @get:Rule
  val authenticationRule = AuthenticationRule()

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
  }

  private fun insertDummyMeasurements(count: Int): Completable {
    val facilityUUID = testData.qaUserFacilityUuid()
    database.facilityDao().save(listOf(
        Facility(
            facilityUUID,
            faker.company.name(),
            null,
            null,
            null,
            faker.address.city(),
            faker.address.state(),
            "India",
            null,
            Instant.now(),
            Instant.now(),
            SyncStatus.DONE
        )
    ))

    val addressUuid = UUID.randomUUID()
    database.addressDao().save(
        PatientAddress(
            addressUuid,
            null,
            faker.address.city(),
            faker.address.state(),
            "India",
            Instant.now(),
            Instant.now()
        )
    )

    val patientUuid = UUID.randomUUID()
    database.patientDao().save(
        Patient(
            patientUuid,
            addressUuid,
            faker.name.name(),
            faker.name.name(),
            Gender.FEMALE,
            LocalDate.parse("1947-08-15"),
            null,
            PatientStatus.ACTIVE,
            Instant.now(),
            Instant.now(),
            SyncStatus.DONE
        ))

    return Observable.range(0, count)
        .flatMapSingle { index ->
          repository.saveMeasurement(patientUuid, systolic = 100 + index, diastolic = 50 + index)
        }
        .ignoreElements()
  }

  @Test
  fun when_pending_sync_measurements_are_present_then_they_should_be_pushed_to_the_server_and_marked_as_synced_on_success() {
    insertDummyMeasurements(count = 5)
        .andThen(bpSync.push())
        .blockingAwait()

    repository.measurementsWithSyncStatus(SyncStatus.DONE)
        .test()
        .await()
        .assertValue { measurements -> measurements.size == 5 }
        .assertComplete()
        .assertNoErrors()
  }

  @Test
  fun when_pulling_measurements_then_paginate_till_the_server_does_not_have_anymore_measurements() {
    lastPullTimestamp.set(Just(Instant.now().minusMillis(100)))

    val measurementsToInsert = 2 * configProvider.blockingGet().batchSize + 7

    insertDummyMeasurements(count = measurementsToInsert)
        .andThen(bpSync.push())
        .andThen(Completable.fromAction { database.bloodPressureDao().clearData() })
        .blockingAwait()

    bpSync.pull().blockingAwait()

    val measurementCountAfterPull = repository.measurementCount().blockingGet()
    assertThat(measurementCountAfterPull).isAtLeast(measurementsToInsert)
  }
}
