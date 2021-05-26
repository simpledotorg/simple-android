package org.simple.clinic.sync

import com.nhaarman.mockitokotlin2.clearInvocations
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.TestData
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.UserStatus
import org.simple.clinic.user.UserStatus.ApprovedForSyncing
import org.simple.clinic.user.UserStatus.DisapprovedForSyncing
import org.simple.clinic.user.UserStatus.WaitingForApproval
import org.simple.clinic.util.Optional
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import org.simple.clinic.util.toOptional

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
    verifyZeroInteractions(dataSync)

    userSubject.onNext(user.withStatus(ApprovedForSyncing, clock).toOptional())
    verify(dataSync).fireAndForgetSync()
    verifyNoMoreInteractions(dataSync)
    clearInvocations(dataSync)

    userSubject.onNext(user.withStatus(ApprovedForSyncing, clock).toOptional())
    verifyZeroInteractions(dataSync)

    userSubject.onNext(user.withStatus(previousUserStatus, clock).toOptional())
    verifyZeroInteractions(dataSync)

    userSubject.onNext(user.withStatus(ApprovedForSyncing, clock).toOptional())
    verify(dataSync).fireAndForgetSync()
    verifyNoMoreInteractions(dataSync)
  }

  @Suppress("Unused")
  private fun `params for syncing on user status changed`(): List<UserStatus> {
    return listOf(WaitingForApproval, DisapprovedForSyncing)
  }
}
