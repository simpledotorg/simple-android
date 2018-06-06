package org.resolvetosavelives.red.bp.sync

import android.support.test.runner.AndroidJUnit4
import com.f2prateek.rx.preferences2.Preference
import com.google.common.truth.Truth.assertThat
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.resolvetosavelives.red.AppDatabase
import org.resolvetosavelives.red.TestRedApp
import org.resolvetosavelives.red.bp.BloodPressureRepository
import org.resolvetosavelives.red.search.SyncStatus
import org.resolvetosavelives.red.sync.SyncConfig
import org.resolvetosavelives.red.util.Just
import org.resolvetosavelives.red.util.Optional
import org.threeten.bp.Instant
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
  lateinit var database: AppDatabase

  @Inject
  @field:[Named("last_bp_pull_timestamp")]
  lateinit var lastPullTimestamp: Preference<Optional<Instant>>

  @Inject
  lateinit var bpSync: BloodPressureSync

  @Before
  fun setUp() {
    TestRedApp.appComponent().inject(this)
  }

  private fun insertDummyMeasurements(count: Int): Completable {
    val parentPatientUuid = UUID.randomUUID()
    return Observable.range(0, count)
        .flatMapCompletable({ index ->
          repository.save(parentPatientUuid, systolic = 100 + index, diastolic = 50 + index)
        })
  }

  @Test
  fun when_pending_sync_measurements_are_present_then_they_should_be_pushed_to_the_server_and_marked_as_synced_on_success() {
    insertDummyMeasurements(count = 5).blockingAwait()

    bpSync.push().blockingAwait()

    repository.measurementsWithSyncStatus(SyncStatus.DONE)
        .test()
        .await()
        .assertValue({ measurements -> measurements.size == 5 })
        .assertComplete()
        .assertNoErrors()
  }

  @Test
  fun when_pulling_measurements_then_paginate_till_the_server_does_not_have_anymore_measurements() {
    lastPullTimestamp.set(Just(Instant.now().minusSeconds(1)))

    val measurementsToInsert = 2 * configProvider.blockingGet().batchSize + 7

    insertDummyMeasurements(count = measurementsToInsert)
        .andThen(bpSync.push())
        .andThen(Completable.fromAction({ database.clearAllTables() }))
        .blockingAwait()

    bpSync.pull().blockingAwait()

    val measurementCountAfterPull = repository.measurementCount().blockingGet()
    assertThat(measurementCountAfterPull).isAtLeast(measurementsToInsert)
  }

  @After
  fun tearDown() {
    database.clearAllTables()
    database.close()
  }
}
