package org.simple.clinic.sync

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.bp.sync.BloodPressureSync
import org.simple.clinic.drugs.sync.PrescriptionSync
import org.simple.clinic.facility.FacilitySync
import org.simple.clinic.medicalhistory.sync.MedicalHistorySync
import org.simple.clinic.overdue.AppointmentSync
import org.simple.clinic.patient.sync.PatientSync
import org.simple.clinic.protocol.sync.ProtocolSync
import org.simple.clinic.sync.ModelSyncTest.SyncOperation.PULL
import org.simple.clinic.sync.ModelSyncTest.SyncOperation.PUSH
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.RxErrorsRule

@RunWith(JUnitParamsRunner::class)
class ModelSyncTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  val syncConfig = SyncConfig(
      syncInterval = SyncInterval.FREQUENT,
      batchSize = 10,
      syncGroup = SyncGroup.FREQUENT
  )

  @Suppress("Unused")
  private fun `sync models that both push and pull`(): List<List<Any>> {
    return listOf(
        listOf<Any>(
            { syncCoordinator: SyncCoordinator, userSession: UserSession ->
              PatientSync(
                  syncCoordinator = syncCoordinator,
                  repository = mock(),
                  api = mock(),
                  userSession = userSession,
                  lastPullToken = mock(),
                  config = syncConfig
              )
            },
            setOf(PUSH, PULL)),
        listOf<Any>(
            { syncCoordinator: SyncCoordinator, userSession: UserSession ->
              BloodPressureSync(
                  syncCoordinator = syncCoordinator,
                  api = mock(),
                  repository = mock(),
                  userSession = userSession,
                  lastPullToken = mock(),
                  config = syncConfig)
            },
            setOf(PUSH, PULL)),
        listOf<Any>(
            { syncCoordinator: SyncCoordinator, userSession: UserSession ->
              PrescriptionSync(
                  syncCoordinator = syncCoordinator,
                  api = mock(),
                  repository = mock(),
                  userSession = userSession,
                  lastPullToken = mock(),
                  config = syncConfig)
            },
            setOf(PUSH, PULL)),
        listOf<Any>(
            { syncCoordinator: SyncCoordinator, userSession: UserSession ->
              AppointmentSync(
                  syncCoordinator = syncCoordinator,
                  repository = mock(),
                  api = mock(),
                  userSession = userSession,
                  lastPullToken = mock(),
                  config = syncConfig)
            },
            setOf(PUSH, PULL)),
        listOf<Any>(
            { syncCoordinator: SyncCoordinator, userSession: UserSession ->
              MedicalHistorySync(
                  syncCoordinator = syncCoordinator,
                  repository = mock(),
                  api = mock(),
                  userSession = userSession,
                  lastPullToken = mock(),
                  config = syncConfig)
            },
            setOf(PUSH, PULL)),
        listOf<Any>(
            { syncCoordinator: SyncCoordinator, _: UserSession ->
              FacilitySync(
                  syncCoordinator = syncCoordinator,
                  repository = mock(),
                  api = mock(),
                  lastPullToken = mock(),
                  config = syncConfig)
            },
            setOf(PULL)),
        listOf<Any>(
            { syncCoordinator: SyncCoordinator, _: UserSession ->
              ProtocolSync(
                  syncCoordinator = syncCoordinator,
                  repository = mock(),
                  api = mock(),
                  lastPullToken = mock(),
                  config = syncConfig)
            },
            setOf(PULL)
        )
    )
  }

  @Test
  @Parameters(method = "sync models that both push and pull")
  fun <T : Any, P : Any> `errors during push should not affect pull`(
      modelSyncProvider: (SyncCoordinator, UserSession) -> ModelSync,
      supportedSyncOperations: Set<SyncOperation>
  ) {
    if ((PULL in supportedSyncOperations).not()) {
      return
    }

    val syncCoordinator = mock<SyncCoordinator>()
    val userSession = mock<UserSession>()
    var pullCompleted = false

    whenever(syncCoordinator.push(any<SynceableRepository<T, P>>(), any()))
        .thenReturn(Completable.error(RuntimeException()))
    whenever(syncCoordinator.pull(any<SynceableRepository<T, P>>(), any(), any(), any()))
        .thenReturn(Completable.complete().doOnComplete { pullCompleted = true })
    whenever(userSession.canSyncData()).thenReturn(Observable.just(true))

    val modelSync = modelSyncProvider(syncCoordinator, userSession)

    modelSync.sync()
        .onErrorComplete()
        .blockingAwait()

    assertThat(pullCompleted).isTrue()
  }

  @Test
  @Parameters(method = "sync models that both push and pull")
  fun <T : Any, P : Any> `errors during pull should not affect push`(
      modelSyncProvider: (SyncCoordinator, UserSession) -> ModelSync,
      supportedSyncOperations: Set<SyncOperation>
  ) {
    if ((PUSH in supportedSyncOperations).not()) {
      return
    }

    val syncCoordinator = mock<SyncCoordinator>()
    val userSession = mock<UserSession>()
    var pushCompleted = false

    whenever(syncCoordinator.pull(any<SynceableRepository<T, P>>(), any(), any(), any()))
        .thenReturn(Completable.error(RuntimeException()))
    whenever(syncCoordinator.push(any<SynceableRepository<T, P>>(), any()))
        .thenReturn(Completable.complete().doOnComplete { pushCompleted = true })
    whenever(userSession.canSyncData()).thenReturn(Observable.just(true))

    val modelSync = modelSyncProvider(syncCoordinator, userSession)

    modelSync.sync()
        .onErrorComplete()
        .blockingAwait()

    assertThat(pushCompleted).isTrue()
  }

  @Test
  @Parameters(method = "params for pushing during sync when user cannot sync")
  fun <T : Any, P : Any> `syncs that depend on user sync permission must not attempt push if the user cannot sync`(
      modelSyncProvider: (SyncCoordinator, UserSession) -> ModelSync,
      shouldPushHappen: Boolean
  ) {
    val syncCoordinator = mock<SyncCoordinator>()
    val userSession = mock<UserSession>()

    whenever(syncCoordinator.pull(any<SynceableRepository<T, P>>(), any(), any(), any()))
        .thenReturn(Completable.complete())
    whenever(syncCoordinator.push(any<SynceableRepository<T, P>>(), any()))
        .thenReturn(Completable.complete())
    whenever(userSession.canSyncData()).thenReturn(Observable.just(false))

    val modelSync = modelSyncProvider(syncCoordinator, userSession)

    modelSync.sync()
        .onErrorComplete()
        .blockingAwait()

    if (shouldPushHappen) {
      verify(syncCoordinator).push(any<SynceableRepository<T, P>>(), any())
    } else {
      verify(syncCoordinator, never()).push(any<SynceableRepository<T, P>>(), any())
    }
  }

  @Suppress("Unused")
  private fun `params for pushing during sync when user cannot sync`(): List<List<Any>> {
    fun testCase(
        modelSyncProvider: (SyncCoordinator, UserSession) -> ModelSync,
        shouldPushHappen: Boolean
    ): List<Any> {
      return listOf(modelSyncProvider, shouldPushHappen)
    }

    return listOf(
        testCase(
            modelSyncProvider = { syncCoordinator, userSession ->
              PatientSync(
                  syncCoordinator = syncCoordinator,
                  repository = mock(),
                  api = mock(),
                  userSession = userSession,
                  lastPullToken = mock(),
                  config = syncConfig
              )
            },
            shouldPushHappen = false),
        testCase(
            modelSyncProvider = { syncCoordinator, userSession ->
              BloodPressureSync(
                  syncCoordinator = syncCoordinator,
                  api = mock(),
                  repository = mock(),
                  userSession = userSession,
                  lastPullToken = mock(),
                  config = syncConfig)
            },
            shouldPushHappen = false),
        testCase(
            modelSyncProvider = { syncCoordinator, userSession ->
              PrescriptionSync(
                  syncCoordinator = syncCoordinator,
                  api = mock(),
                  repository = mock(),
                  userSession = userSession,
                  lastPullToken = mock(),
                  config = syncConfig)
            },
            shouldPushHappen = false),
        testCase(
            modelSyncProvider = { syncCoordinator, userSession ->
              AppointmentSync(
                  syncCoordinator = syncCoordinator,
                  repository = mock(),
                  api = mock(),
                  userSession = userSession,
                  lastPullToken = mock(),
                  config = syncConfig)
            },
            shouldPushHappen = false),
        testCase(
            modelSyncProvider = { syncCoordinator, userSession ->
              MedicalHistorySync(
                  syncCoordinator = syncCoordinator,
                  repository = mock(),
                  api = mock(),
                  userSession = userSession,
                  lastPullToken = mock(),
                  config = syncConfig)
            },
            shouldPushHappen = false),
        testCase(
            modelSyncProvider = { syncCoordinator, _ ->
              FacilitySync(
                  syncCoordinator = syncCoordinator,
                  repository = mock(),
                  api = mock(),
                  lastPullToken = mock(),
                  config = syncConfig)
            },
            shouldPushHappen = false),
        testCase(
            modelSyncProvider = { syncCoordinator, _ ->
              ProtocolSync(
                  syncCoordinator = syncCoordinator,
                  repository = mock(),
                  api = mock(),
                  lastPullToken = mock(),
                  config = syncConfig)
            },
            shouldPushHappen = false
        )
    )
  }

  @Test
  @Parameters(method = "params for pulling during sync when user cannot sync")
  fun <T : Any, P : Any> `syncs that depend on user sync permission must not attempt pull if the user cannot sync`(
      modelSyncProvider: (SyncCoordinator, UserSession) -> ModelSync,
      shouldPullHappen: Boolean
  ) {
    val syncCoordinator = mock<SyncCoordinator>()
    val userSession = mock<UserSession>()

    whenever(syncCoordinator.pull(any<SynceableRepository<T, P>>(), any(), any(), any()))
        .thenReturn(Completable.complete())
    whenever(syncCoordinator.push(any<SynceableRepository<T, P>>(), any()))
        .thenReturn(Completable.complete())
    whenever(userSession.canSyncData()).thenReturn(Observable.just(false))

    val modelSync = modelSyncProvider(syncCoordinator, userSession)

    modelSync.sync()
        .onErrorComplete()
        .blockingAwait()

    if (shouldPullHappen) {
      verify(syncCoordinator).pull(any<SynceableRepository<T, P>>(), any(), any(), any())
    } else {
      verify(syncCoordinator, never()).pull(any<SynceableRepository<T, P>>(), any(), any(), any())
    }
  }

  @Suppress("Unused")
  private fun `params for pulling during sync when user cannot sync`(): List<List<Any>> {
    fun testCase(
        modelSyncProvider: (SyncCoordinator, UserSession) -> ModelSync,
        shouldPullHappen: Boolean
    ): List<Any> {
      return listOf(modelSyncProvider, shouldPullHappen)
    }

    return listOf(
        testCase(
            modelSyncProvider = { syncCoordinator, userSession ->
              PatientSync(
                  syncCoordinator = syncCoordinator,
                  repository = mock(),
                  api = mock(),
                  userSession = userSession,
                  lastPullToken = mock(),
                  config = syncConfig
              )
            },
            shouldPullHappen = false),
        testCase(
            modelSyncProvider = { syncCoordinator, userSession ->
              BloodPressureSync(
                  syncCoordinator = syncCoordinator,
                  api = mock(),
                  repository = mock(),
                  userSession = userSession,
                  lastPullToken = mock(),
                  config = syncConfig)
            },
            shouldPullHappen = false),
        testCase(
            modelSyncProvider = { syncCoordinator, userSession ->
              PrescriptionSync(
                  syncCoordinator = syncCoordinator,
                  api = mock(),
                  repository = mock(),
                  userSession = userSession,
                  lastPullToken = mock(),
                  config = syncConfig)
            },
            shouldPullHappen = false),
        testCase(
            modelSyncProvider = { syncCoordinator, userSession ->
              AppointmentSync(
                  syncCoordinator = syncCoordinator,
                  repository = mock(),
                  api = mock(),
                  userSession = userSession,
                  lastPullToken = mock(),
                  config = syncConfig)
            },
            shouldPullHappen = false),
        testCase(
            modelSyncProvider = { syncCoordinator, userSession ->
              MedicalHistorySync(
                  syncCoordinator = syncCoordinator,
                  repository = mock(),
                  api = mock(),
                  userSession = userSession,
                  lastPullToken = mock(),
                  config = syncConfig)
            },
            shouldPullHappen = false),
        testCase(
            modelSyncProvider = { syncCoordinator, _ ->
              FacilitySync(
                  syncCoordinator = syncCoordinator,
                  repository = mock(),
                  api = mock(),
                  lastPullToken = mock(),
                  config = syncConfig)
            },
            shouldPullHappen = true),
        testCase(
            modelSyncProvider = { syncCoordinator, _ ->
              ProtocolSync(
                  syncCoordinator = syncCoordinator,
                  repository = mock(),
                  api = mock(),
                  lastPullToken = mock(),
                  config = syncConfig)
            },
            shouldPullHappen = true
        )
    )
  }

  enum class SyncOperation {
    PUSH, PULL
  }
}
