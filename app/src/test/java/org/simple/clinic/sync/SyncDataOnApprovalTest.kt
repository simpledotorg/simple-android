package org.simple.clinic.sync

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.user.UserSession

@RunWith(JUnitParamsRunner::class)
class SyncDataOnApprovalTest {

  private val userSession = mock<UserSession>()
  private val dataSync = mock<DataSync>()

  private val syncDataOnApproval = SyncDataOnApproval(userSession, dataSync)

  @Before
  fun setup() {
    RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
  }

  @Parameters(value = ["true", "false"])
  @Test
  fun `when user is allowed to sync data only then data sync should be initiated`(canSync: Boolean) {
    whenever(userSession.canSyncData()).thenReturn(Observable.just(canSync))
    whenever(dataSync.sync(null)).thenReturn(Completable.complete())

    syncDataOnApproval.sync()

    if (canSync) {
      verify(dataSync).sync(null)
    } else {
      verify(dataSync, never()).sync(null)
    }
  }
}
