package org.simple.clinic.user

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.TestData
import org.simple.clinic.sync.DataSync
import org.simple.clinic.util.ResolvedError
import org.simple.clinic.util.ResolvedError.NetworkRelated
import org.simple.clinic.util.ResolvedError.ServerError
import org.simple.clinic.util.ResolvedError.Unauthenticated
import org.simple.clinic.util.ResolvedError.Unexpected
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import org.simple.clinic.util.toOptional

@RunWith(JUnitParamsRunner::class)
class UnauthorizeUserTest {

  @Test
  @Parameters(method = "params for unauthorizing user")
  fun `when an unauthorized error is emitted, the user must be unauthorized`(
      resolvedError: ResolvedError,
      shouldUnauthorizeUser: Boolean
  ) {
    val dataSync = mock<DataSync>()
    val errorSubject: PublishSubject<ResolvedError> = PublishSubject.create<ResolvedError>()
    whenever(dataSync.streamSyncErrors()).thenReturn(errorSubject)

    val userSession = mock<UserSession>()
    var unauthorizeCompleted = false
    whenever(userSession.loggedInUser()).thenReturn(Observable.just(TestData.loggedInUser().toOptional()))
    whenever(userSession.unauthorize()).thenReturn(Completable.fromAction { unauthorizeCompleted = true })

    val unauthorizeUser = UnauthorizeUser(userSession = userSession, dataSync = dataSync, schedulersProvider = TrampolineSchedulersProvider())

    unauthorizeUser.listen()

    verifyZeroInteractions(userSession)

    errorSubject.onNext(resolvedError)

    if (shouldUnauthorizeUser) {
      verify(userSession).unauthorize()
      assertThat(unauthorizeCompleted).isTrue()
    } else {
      verify(userSession, never()).unauthorize()
      assertThat(unauthorizeCompleted).isFalse()
    }
  }

  @Suppress("Unused")
  private fun `params for unauthorizing user`(): List<List<Any>> {
    fun testCase(
        error: ResolvedError,
        shouldUnauthorizeUser: Boolean
    ): List<Any> {
      return listOf(error, shouldUnauthorizeUser)
    }

    return listOf(
        testCase(error = Unauthenticated(RuntimeException()), shouldUnauthorizeUser = true),
        testCase(error = Unauthenticated(NullPointerException()), shouldUnauthorizeUser = true),
        testCase(error = NetworkRelated(RuntimeException()), shouldUnauthorizeUser = false),
        testCase(error = Unexpected(RuntimeException()), shouldUnauthorizeUser = false),
        testCase(error = ServerError(RuntimeException()), shouldUnauthorizeUser = false)
    )
  }
}
