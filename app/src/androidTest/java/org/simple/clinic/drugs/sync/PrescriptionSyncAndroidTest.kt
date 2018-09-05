package org.simple.clinic.drugs.sync

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
import org.simple.clinic.drugs.PrescriptionRepository
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
class PrescriptionSyncAndroidTest {

  @Inject
  lateinit var configProvider: Single<SyncConfig>

  @Inject
  lateinit var repository: PrescriptionRepository

  @Inject
  lateinit var userSession: UserSession

  @Inject
  lateinit var database: AppDatabase

  @Inject
  @field:Named("last_prescription_pull_timestamp")
  lateinit var lastPullTimestamp: Preference<Optional<Instant>>

  @Inject
  lateinit var prescriptionSync: PrescriptionSync

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

  private fun insertDummyPrescriptions(count: Int): Completable {
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
        .flatMapCompletable { index ->
          repository.savePrescription(
              patientUuid,
              name = "Drug #$index",
              dosage = "1${index}mg",
              rxNormCode = "rx-norm-code-$index",
              isProtocolDrug = false)
        }
  }

  @Test
  fun when_pending_sync_prescriptions_are_present_then_they_should_be_pushed_to_the_server_and_marked_as_synced_on_success() {
    insertDummyPrescriptions(count = 5).blockingAwait()

    prescriptionSync.push().blockingAwait()

    repository.prescriptionsWithSyncStatus(SyncStatus.DONE)
        .test()
        .await()
        .assertValue { prescriptions -> prescriptions.size == 5 }
        .assertComplete()
        .assertNoErrors()
  }

  @Test
  fun when_pulling_prescriptions_then_paginate_till_the_server_does_not_have_anymore_prescriptions() {
    lastPullTimestamp.set(Just(Instant.now().minusMillis(100)))

    val prescriptionsToInsert = 2 * configProvider.blockingGet().batchSize + 7
    insertDummyPrescriptions(count = prescriptionsToInsert).blockingAwait()

    prescriptionSync.push()
        .andThen(Completable.fromAction { database.prescriptionDao().clearData() })
        .blockingAwait()

    prescriptionSync.pull().blockingAwait()

    val prescriptionCountAfterPull = repository.prescriptionCount().blockingGet()
    assertThat(prescriptionCountAfterPull).isAtLeast(prescriptionsToInsert)
  }
}
