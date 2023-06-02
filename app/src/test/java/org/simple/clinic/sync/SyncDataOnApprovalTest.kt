package org.simple.clinic.sync

import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.sharedTestCode.TestData
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.UserStatus
import org.simple.clinic.user.UserStatus.ApprovedForSyncing
import org.simple.clinic.user.UserStatus.DisapprovedForSyncing
import org.simple.clinic.user.UserStatus.WaitingForApproval
import org.simple.sharedTestCode.util.TestUtcClock
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import org.simple.clinic.util.toOptional
import java.util.Optional

@RunWith(JUnitParamsRunner::class)
class SyncDataOnApprovalTest {

  private val userSession = mock<UserSession>()
  private val dataSync = mock<DataSync>()
  private val clock = TestUtcClock()

  private val syncDataOnApproval = SyncDataOnApproval(userSession, dataSync, TrampolineSchedulersProvider())

  @Parameters(method = "params for syncing on user status changed")
  @Test
  fun `when the user status changes to ApprovedFromSyncing from anything, the sync should be triggered`(
      previousUserStatus: UserStatus
  ) {
    // given
    val user = TestData.loggedInUser(status = previousUserStatus)
    val userSubject = PublishSubject.create<Optional<User>>()
    whenever(userSession.loggedInUser()).thenReturn(userSubject)

    // when
    syncDataOnApproval.sync()

    // then
    userSubject.onNext(user.toOptional())
    verifyNoInteractions(dataSync)

    userSubject.onNext(user.withStatus(ApprovedForSyncing, clock).toOptional())
    verify(dataSync).fireAndForgetSync()
    verifyNoMoreInteractions(dataSync)
    clearInvocations(dataSync)

    userSubject.onNext(user.withStatus(ApprovedForSyncing, clock).toOptional())
    verifyNoInteractions(dataSync)

    userSubject.onNext(user.withStatus(previousUserStatus, clock).toOptional())
    verifyNoInteractions(dataSync)

    userSubject.onNext(user.withStatus(ApprovedForSyncing, clock).toOptional())
    verify(dataSync).fireAndForgetSync()
    verifyNoMoreInteractions(dataSync)
  }

  @Suppress("Unused")
  private fun `params for syncing on user status changed`(): List<UserStatus> {
    return listOf(WaitingForApproval, DisapprovedForSyncing)
  }
}
