package org.simple.clinic.sync

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.facility.FacilitySync
import org.simple.clinic.patient.sync.PatientSync
import org.simple.clinic.sync.ModelSyncTest.SyncOperation.PULL
import org.simple.clinic.sync.ModelSyncTest.SyncOperation.PUSH

@RunWith(JUnitParamsRunner::class)
class ModelSyncTest {

  @Suppress("Unused")
  private fun `sync models that both push and pull`(): List<List<Any>> {
    return listOf(
        listOf<Any>(
            { syncCoordinator: SyncCoordinator -> PatientSync(syncCoordinator, mock(), mock(), mock()) },
            setOf(PUSH, PULL)
        ),
        listOf<Any>(
            { syncCoordinator: SyncCoordinator -> FacilitySync(syncCoordinator, mock(), mock(), mock()) },
            setOf(PULL)
        )
    )
  }

  @Test
  @Parameters(method = "sync models that both push and pull")
  fun <T : Any, P : Any> `errors during push should not affect pull`(
      modelSyncProvider: (SyncCoordinator) -> ModelSync,
      supportedSyncOperations: Set<SyncOperation>
  ) {
    if ((PULL in supportedSyncOperations).not()) {
      return
    }

    val syncCoordinator = mock<SyncCoordinator>()
    var pullCompleted = false

    whenever(syncCoordinator.push(any<SynceableRepository<T, P>>(), any())).thenReturn(Completable.error(RuntimeException()))
    whenever(syncCoordinator.pull(any<SynceableRepository<T, P>>(), any(), any()))
        .thenReturn(Completable.complete().doOnComplete { pullCompleted = true })

    val modelSync = modelSyncProvider(syncCoordinator)

    modelSync.sync()
        .onErrorComplete()
        .blockingAwait()

    assertThat(pullCompleted).isTrue()
  }

  @Test
  @Parameters(method = "sync models that both push and pull")
  fun <T : Any, P : Any> `errors during pull should not affect push`(
      modelSyncProvider: (SyncCoordinator) -> ModelSync,
      supportedSyncOperations: Set<SyncOperation>
  ) {
    if ((PUSH in supportedSyncOperations).not()) {
      return
    }

    val syncCoordinator = mock<SyncCoordinator>()
    var pushCompleted = false

    whenever(syncCoordinator.pull(any<SynceableRepository<T, P>>(), any(), any())).thenReturn(Completable.error(RuntimeException()))
    whenever(syncCoordinator.push(any<SynceableRepository<T, P>>(), any()))
        .thenReturn(Completable.complete().doOnComplete { pushCompleted = true })

    val modelSync = modelSyncProvider(syncCoordinator)

    modelSync.sync()
        .onErrorComplete()
        .blockingAwait()

    assertThat(pushCompleted).isTrue()
  }

  enum class SyncOperation {
    PUSH, PULL
  }
}
