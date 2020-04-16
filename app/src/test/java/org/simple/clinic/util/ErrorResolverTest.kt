package org.simple.clinic.util

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.squareup.moshi.JsonDataException
import io.reactivex.exceptions.CompositeException
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import okhttp3.MediaType
import okhttp3.ResponseBody
import okhttp3.internal.http2.ConnectionShutdownException
import okhttp3.internal.http2.ErrorCode
import okhttp3.internal.http2.StreamResetException
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.util.ResolvedError.NetworkRelated
import org.simple.clinic.util.ResolvedError.ServerError
import org.simple.clinic.util.ResolvedError.Unauthenticated
import org.simple.clinic.util.ResolvedError.Unexpected
import retrofit2.HttpException
import retrofit2.Response
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@RunWith(JUnitParamsRunner::class)
class ErrorResolverTest {

  @Test
  @Parameters(method = "network related errors")
  fun `network errors should be identified correctly`(error: Throwable) {
    val resolvedError = ErrorResolver.resolve(error)
    assertThat(resolvedError).isInstanceOf(NetworkRelated::class.java)
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
    assertThat(resolvedError).isInstanceOf(Unexpected::class.java)
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
  @Parameters(method = "params for unwrapping composite exceptions")
  fun `composite exceptions should be unwrapped`(
      compositeException: CompositeException,
      expectedActualCause: Throwable
  ) {
    val resolvedError = ErrorResolver.resolve(compositeException)
    assertThat(resolvedError.actualCause).isSameInstanceAs(expectedActualCause)
  }

  @Suppress("Unused")
  private fun `params for unwrapping composite exceptions`(): List<List<Any>> {
    fun testCase(
        throwables: List<Throwable>,
        expectedActualCause: Throwable
    ): List<Any> {
      return listOf(CompositeException(throwables), expectedActualCause)
    }

    return listOf(
        IllegalStateException().let { actual ->
          testCase(
              throwables = listOf(actual, NullPointerException()),
              expectedActualCause = actual
          )
        },
        IllegalStateException().let { actual ->
          testCase(
              throwables = listOf(
                  CompositeException(actual, RuntimeException()),
                  NullPointerException()
              ),
              expectedActualCause = actual
          )
        },
        IllegalStateException().let { actual ->
          testCase(
              throwables = listOf(
                  actual,
                  CompositeException(NullPointerException(), RuntimeException())
              ),
              expectedActualCause = actual
          )
        }
    )
  }

  @Test
  fun `http unauthorized errors must be identified`() {
    // given
    val exception = httpException(401)

    // when
    val resolvedError = ErrorResolver.resolve(exception)

    // then
    with(resolvedError) {
      assertThat(this::class).isSameInstanceAs(Unauthenticated::class)
      assertThat(actualCause).isSameInstanceAs(exception)
    }
  }

  @Test
  @Parameters(value = ["403", "404", "499"])
  fun `other http errors must be identified as unexpected errors`(responseCode: Int) {
    // given
    val exception = httpException(responseCode)

    // when
    val resolvedError = ErrorResolver.resolve(exception)

    // then
    with(resolvedError) {
      assertThat(this::class).isSameInstanceAs(Unexpected::class)
      assertThat(actualCause).isSameInstanceAs(exception)
    }
  }

  @Test
  @Parameters(value = ["500", "501", "502", "599"])
  fun `http server errors must be identified`(responseCode: Int) {
    // given
    val exception = httpException(responseCode)

    // when
    val resolvedError = ErrorResolver.resolve(exception)

    // then
    with(resolvedError) {
      assertThat(this::class).isSameInstanceAs(ServerError::class)
      assertThat(actualCause).isSameInstanceAs(exception)
    }
  }

  private fun httpException(responseCode: Int): HttpException {
    val response = Response.error<String>(
        responseCode,
        ResponseBody.create(MediaType.parse("text/plain"), "FAIL")
    )
    return HttpException(response)
  }
}
