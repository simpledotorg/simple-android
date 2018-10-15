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
import org.simple.clinic.patient.sync.PatientSync

@RunWith(JUnitParamsRunner::class)
class ModelSyncTest {

  @Suppress("Unused")
  private fun `sync models that both push and pull`(): List<(SyncCoordinator) -> ModelSync> {
    return listOf(
        { syncCoordinator: SyncCoordinator -> PatientSync(syncCoordinator, mock(), mock(), mock()) }
    )
  }

  @Test
  @Parameters(method = "sync models that both push and pull")
  fun <T : Any, P : Any> `errors during push should not affect pull`(
      modelSyncProvider: (SyncCoordinator) -> ModelSync
  ) {
    val syncCoordinator = mock<SyncCoordinator>()
    var pullCompleted = false

    whenever(syncCoordinator.push(any<SynceableRepository<T, P>>(), any())).thenReturn(Completable.error(RuntimeException()))
    whenever(syncCoordinator.pull(any<SynceableRepository<T, P>>(), any(), any()))
        .thenReturn(Completable.complete().doOnComplete { pullCompleted = true })

    val modelSync = modelSyncProvider(syncCoordinator)

    modelSync.sync()
        .test()
        .await()
        .assertError(RuntimeException::class.java)

    assertThat(pullCompleted).isTrue()
  }

  @Test
  @Parameters(method = "sync models that both push and pull")
  fun <T : Any, P : Any> `errors during pull should not affect push`(
      modelSyncProvider: (SyncCoordinator) -> ModelSync
  ) {
    val dataSync = mock<SyncCoordinator>()
    var pushCompleted = false

    whenever(dataSync.pull(any<SynceableRepository<T, P>>(), any(), any())).thenReturn(Completable.error(RuntimeException()))
    whenever(dataSync.push(any<SynceableRepository<T, P>>(), any()))
        .thenReturn(Completable.complete().doOnComplete { pushCompleted = true })

    val modelSync = modelSyncProvider(dataSync)

    modelSync.sync()
        .test()
        .await()
        .assertError(RuntimeException::class.java)

    assertThat(pushCompleted).isTrue()
  }
}
