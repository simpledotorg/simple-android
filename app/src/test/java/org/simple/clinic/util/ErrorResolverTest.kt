package org.simple.clinic.util

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.mock
import com.squareup.moshi.JsonDataException
import io.reactivex.exceptions.CompositeException
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import okhttp3.internal.http2.ConnectionShutdownException
import okhttp3.internal.http2.ErrorCode
import okhttp3.internal.http2.StreamResetException
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.HttpException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@RunWith(JUnitParamsRunner::class)
class ErrorResolverTest {

  @Test
  @Parameters(method = "network related errors")
  fun `network errors should be identified correctly`(error: Throwable) {
    val resolvedError = ErrorResolver.resolve(error)
    assertThat(resolvedError).isInstanceOf(ResolvedError.NetworkRelated::class.java)
  }

  @Suppress("unused")
  fun `network related errors`() = arrayOf<Any>(
      SocketException(),
      SocketTimeoutException(),
      UnknownHostException(),
      StreamResetException(ErrorCode.ENHANCE_YOUR_CALM),
      ConnectionShutdownException()
  )

  @Test
  @Parameters(method = "other errors")
  fun `other errors should be identified correctly`(error: Throwable) {
    val resolvedError = ErrorResolver.resolve(NullPointerException())
    assertThat(resolvedError).isInstanceOf(ResolvedError.Unexpected::class.java)
  }

  @Suppress("unused")
  fun `other errors`() = arrayOf<Any>(
      NullPointerException(),
      mock<HttpException>(),
      ClassCastException(),
      JsonDataException(),
      IllegalStateException()
  )

  @Test
  fun `composite exceptions should be unwrapped`() {
    val composite = CompositeException(IllegalStateException(), NullPointerException())
    val resolvedError = ErrorResolver.resolve(composite)
    assertThat(resolvedError.actualCause).isInstanceOf(IllegalStateException::class.java)
  }
}
