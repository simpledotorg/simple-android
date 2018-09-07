package org.simple.clinic.sync

import com.f2prateek.rx.preferences2.Preference
import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import io.reactivex.Completable
import io.reactivex.Single
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Just
import org.simple.clinic.util.Optional
import org.threeten.bp.Instant
import javax.inject.Inject

class DataSyncAndroidTester @Inject constructor(val configProvider: Single<SyncConfig>, val userSession: UserSession) {

  fun <T, P> test_push(
      repository: SynceableRepository<T, P>,
      generateRecord: (SyncStatus) -> T,
      push: () -> Completable
  ) {
    val records = (0 until 5).map { generateRecord(SyncStatus.PENDING) }

    repository.save(records)
        .andThen(push())
        .blockingAwait()

    val updatedRecords = repository.recordsWithSyncStatus(SyncStatus.DONE).blockingGet()
    assertThat(updatedRecords).hasSize(5)
  }

  fun <T, P> test_pull(
      repository: SynceableRepository<T, P>,
      lastPullTimestamp: Preference<Optional<Instant>>,
      pull: () -> Completable,
      pushNetworkCall: (payloadCount: Int) -> Single<DataPushResponse>
  ) {
    lastPullTimestamp.set(Just(Instant.EPOCH))
    val recordsToInsert = 2 * configProvider.blockingGet().batchSize + 7

    pushNetworkCall(recordsToInsert)
        .toCompletable()
        .andThen(pull())
        .blockingAwait()

    val recordCountAfterPull = repository.recordCount().blockingGet()
    Truth.assertThat(recordCountAfterPull).isAtLeast(recordsToInsert)
  }
}
