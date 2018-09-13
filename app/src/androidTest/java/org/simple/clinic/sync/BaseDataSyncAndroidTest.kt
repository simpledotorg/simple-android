package org.simple.clinic.sync

import com.f2prateek.rx.preferences2.Preference
import com.google.common.truth.Truth.assertThat
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.AuthenticationRule
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.util.Just
import org.simple.clinic.util.Optional
import org.threeten.bp.Instant
import javax.inject.Inject

// TODO: Use this class for all data sync tests.
abstract class BaseDataSyncAndroidTest<T, P> {

  @get:Rule
  val authenticationRule = AuthenticationRule()

  @Inject
  lateinit var configProvider: Single<SyncConfig>

  @Test
  fun when_pending_sync_records_are_present_then_they_should_be_pushed_to_the_server_and_marked_as_synced_on_success() {
    if (isPushEnabled().not()) {
      return
    }

    val repository = repository()
    val records = (0 until 5).map { generateRecord(SyncStatus.PENDING) }

    repository.save(records)
        .andThen(push())
        .blockingAwait()

    val updatedRecords = repository.recordsWithSyncStatus(SyncStatus.DONE).blockingGet()
    assertThat(updatedRecords).hasSize(5)
  }

  @Test
  fun when_pulling_records_then_paginate_till_the_server_does_not_have_anymore_records() {
    if (isPullEnabled().not()) {
      return
    }

    lastPullTimestamp().set(Just(Instant.EPOCH))

    val recordsToInsert = 2 * configProvider.blockingGet().batchSize + 7
    val payloads = (0 until recordsToInsert).map { (::generatePayload)() }

    pushNetworkCall(payloads)
        .toCompletable()
        .andThen(pull())
        .blockingAwait()

    val recordCountAfterPull = repository().recordCount().blockingGet()
    assertThat(recordCountAfterPull).isAtLeast(recordsToInsert)
  }

  open fun isPullEnabled() = true

  open fun isPushEnabled() = true

  abstract fun push(): Completable

  abstract fun pull(): Completable

  abstract fun repository(): SynceableRepository<T, P>

  abstract fun generateRecord(syncStatus: SyncStatus): T

  abstract fun generatePayload(): P

  abstract fun lastPullTimestamp(): Preference<Optional<Instant>>

  abstract fun pushNetworkCall(payloads: List<P>): Single<DataPushResponse>
}
