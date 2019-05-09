package org.simple.clinic.user

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers.trampoline
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.sync.DataSync
import org.simple.clinic.util.ResolvedError
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
    whenever(userSession.loggedInUser()).thenReturn(Observable.just(PatientMocker.loggedInUser().toOptional()))
    whenever(userSession.unauthorize()).thenReturn(Completable.fromAction { unauthorizeCompleted = true })

    val unauthorizeUser = UnauthorizeUser(userSession = userSession, dataSync = dataSync)

    unauthorizeUser.listen(trampoline())

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
        testCase(error = ResolvedError.Unauthorized(RuntimeException()), shouldUnauthorizeUser = true),
        testCase(error = ResolvedError.Unauthorized(NullPointerException()), shouldUnauthorizeUser = true),
        testCase(error = ResolvedError.NetworkRelated(RuntimeException()), shouldUnauthorizeUser = false),
        testCase(error = ResolvedError.Unexpected(RuntimeException()), shouldUnauthorizeUser = false)
    )
  }
}
