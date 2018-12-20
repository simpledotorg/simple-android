package org.simple.clinic.sync

import com.f2prateek.rx.preferences2.Preference
import com.google.common.truth.Truth.assertThat
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.After
import org.junit.Test
import org.simple.clinic.network.FailAllNetworkCallsInterceptor
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.patient.SyncStatus.DONE
import org.simple.clinic.patient.SyncStatus.IN_FLIGHT
import org.simple.clinic.patient.SyncStatus.PENDING
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import javax.inject.Inject

// TODO: Use this class for all data sync tests.
abstract class BaseSyncCoordinatorAndroidTest<T, P> {

  @Inject
  lateinit var configProvider: Single<SyncConfig>

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

    lastPullToken().set(None)

    val recordsToInsert = 2 * configProvider.blockingGet().batchSize + 7
    val payloads = (0 until recordsToInsert).map { (::generatePayload)() }

    pushNetworkCall(payloads)
        .toCompletable()
        .andThen(pull())
        .blockingAwait()

    val recordCountAfterPull = repository().recordCount().blockingFirst()
    assertThat(recordCountAfterPull).isAtLeast(recordsToInsert)
  }

  @Test(expected = FailAllNetworkCallsInterceptor.ForcedException::class)
  fun before_starting_a_push_records_that_are_stuck_in_IN_FLIGHT_from_a_previous_sync_should_be_marked_as_PENDING_again() {
    if (isPushEnabled().not()) {
      return
    }

    FailAllNetworkCallsInterceptor.shouldFailAll = true

    val repository = repository()
    val records = (0 until 5).map { generateRecord(PENDING) }

    repository.save(records)
        .andThen(push())
        .blockingAwait()

    assertThat(recordsWithSyncStatus(IN_FLIGHT)).hasSize(records.size)
    assertThat(recordsWithSyncStatus(PENDING)).hasSize(0)
    assertThat(recordsWithSyncStatus(DONE)).hasSize(0)

    FailAllNetworkCallsInterceptor.shouldFailAll = false
    push().blockingAwait()

    assertThat(recordsWithSyncStatus(IN_FLIGHT)).hasSize(0)
    assertThat(recordsWithSyncStatus(PENDING)).hasSize(0)
    assertThat(recordsWithSyncStatus(DONE)).hasSize(records.size)
  }

  private fun recordsWithSyncStatus(status: SyncStatus) = repository().recordsWithSyncStatus(status).blockingGet()

  @After
  fun tearDown() {
    FailAllNetworkCallsInterceptor.shouldFailAll = false
  }

  open fun isPullEnabled() = true

  open fun isPushEnabled() = true

  abstract fun push(): Completable

  abstract fun pull(): Completable

  abstract fun repository(): SynceableRepository<T, P>

  abstract fun generateRecord(syncStatus: SyncStatus): T

  abstract fun generatePayload(): P

  abstract fun lastPullToken(): Preference<Optional<String>>

  abstract fun pushNetworkCall(payloads: List<P>): Single<DataPushResponse>
}
