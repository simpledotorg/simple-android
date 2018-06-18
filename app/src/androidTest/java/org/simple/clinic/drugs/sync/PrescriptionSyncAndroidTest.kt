package org.simple.clinic.drugs.sync

import android.support.test.runner.AndroidJUnit4
import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.AppDatabase
import org.simple.clinic.TestClinicApp
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.util.Optional
import org.threeten.bp.Instant
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
  lateinit var database: AppDatabase

  @Inject
  @field:[Named("last_bp_pull_timestamp")]
  lateinit var lastPullTimestamp: Preference<Optional<Instant>>

  @Inject
  lateinit var bpSync: PrescriptionSync

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
  }

  private fun insertDummyPrescriptions(count: Int): Completable {
    val parentPatientUuid = UUID.randomUUID()
    return Observable.range(0, count)
        .flatMapCompletable { index ->
          repository.savePrescription(
              parentPatientUuid,
              name = "Drug #$index",
              dosage = "1${index}mg",
              rxNormCode = "rx-norm-code-$index",
              isProtocolDrug = false)
        }
  }

  // TODO: Uncomment these once the backend has updated their prescription sync APIs to accept "is_deleted" and "is_protocol_drug" fields.
  @Test
  fun when_pending_sync_prescriptions_are_present_then_they_should_be_pushed_to_the_server_and_marked_as_synced_on_success() {
//    insertDummyPrescriptions(count = 5).blockingAwait()
//
//    bpSync.push().blockingAwait()
//
//    repository.prescriptionsWithSyncStatus(SyncStatus.DONE)
//        .test()
//        .await()
//        .assertValue { prescriptions -> prescriptions.size == 5 }
//        .assertComplete()
//        .assertNoErrors()
  }

  @Test
  fun when_pulling_prescriptions_then_paginate_till_the_server_does_not_have_anymore_prescriptions() {
//    lastPullTimestamp.set(Just(Instant.now().minusSeconds(1)))
//
//    val prescriptionsToInsert = 2 * configProvider.blockingGet().batchSize + 7
//
//    insertDummyPrescriptions(count = prescriptionsToInsert)
//        .andThen(bpSync.push())
//        .andThen(Completable.fromAction { database.clearAllTables() })
//        .blockingAwait()
//
//    bpSync.pull().blockingAwait()
//
//    val prescriptionCountAfterPull = repository.prescriptionCount().blockingGet()
//    assertThat(prescriptionCountAfterPull).isAtLeast(prescriptionsToInsert)
  }

  @After
  fun tearDown() {
    database.clearAllTables()
  }
}
