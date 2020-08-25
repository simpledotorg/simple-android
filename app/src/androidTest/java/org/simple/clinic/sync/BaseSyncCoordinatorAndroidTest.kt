package org.simple.clinic.sync

import com.f2prateek.rx.preferences2.Preference
import com.google.common.truth.Truth.assertThat
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Test
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.patient.SyncStatus.DONE
import org.simple.clinic.patient.SyncStatus.PENDING
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional

// TODO: Use this class for all data sync tests.
abstract class BaseSyncCoordinatorAndroidTest<T, P> {

  @Test
  fun when_pending_sync_records_are_present_then_they_should_be_pushed_to_the_server_and_marked_as_synced_on_success() {
    if (isPushEnabled().not()) {
      return
    }

    val repository = repository()
    val records = (0 until 5).map { generateRecord(PENDING) }

    repository.save(records)
        .andThen(push())
        .blockingAwait()

    val updatedRecords = recordsWithSyncStatus(DONE)
    assertThat(updatedRecords).hasSize(5)
  }

  @Test
  fun when_pulling_records_then_paginate_till_the_server_does_not_have_anymore_records() {
    if (isPullEnabled().not()) {
      return
    }

    lastPullToken().set(None())

    val recordsToInsert = 2 * batchSize() + 7
    val payloads = (0 until recordsToInsert).map { (::generatePayload)() }

    pushNetworkCall(payloads)
        .toCompletable()
        .andThen(pull())
        .blockingAwait()

    val recordCountAfterPull = repository().recordCount().blockingFirst()
    assertThat(recordCountAfterPull).isAtLeast(recordsToInsert)
  }

  private fun recordsWithSyncStatus(status: SyncStatus) = repository().recordsWithSyncStatus(status).blockingGet()

  open fun isPullEnabled() = true

  open fun isPushEnabled() = true

  abstract fun push(): Completable

  abstract fun pull(): Completable

  abstract fun repository(): SynceableRepository<T, P>

  abstract fun generateRecord(syncStatus: SyncStatus): T

  abstract fun generatePayload(): P

  abstract fun lastPullToken(): Preference<Optional<String>>

  abstract fun pushNetworkCall(payloads: List<P>): Single<DataPushResponse>

  abstract fun batchSize(): Int
}
