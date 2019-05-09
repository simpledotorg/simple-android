package org.simple.clinic.util

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.mock
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
import org.simple.clinic.util.ResolvedError.*
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
    assertThat(resolvedError.actualCause).isSameAs(expectedActualCause)
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
  @Parameters(method = "params for http unauthorized errors")
  fun `http unauthorized errors must be identified correctly`(
      httpException: HttpException,
      expectedResolvedError: ResolvedError
  ) {
    val resolvedError = ErrorResolver.resolve(httpException)
    assertThat(resolvedError::class).isSameAs(expectedResolvedError::class)
    assertThat(resolvedError.actualCause).isSameAs(httpException)
  }

  @Suppress("Unused")
  private fun `params for http unauthorized errors`(): List<List<Any>> {
    fun exception(responseCode: Int): HttpException {
      val response = Response.error<String>(
          responseCode,
          ResponseBody.create(MediaType.parse("text/plain"), "FAIL")
      )
      return HttpException(response)
    }

    return listOf(
        exception(401).let { httpException ->
          listOf(httpException, Unauthorized(httpException))
        },
        exception(404).let { httpException ->
          listOf(httpException, Unexpected(httpException))
        },
        exception(500).let { httpException ->
          listOf(httpException, Unexpected(httpException))
        },
        exception(502).let { httpException ->
          listOf(httpException, Unexpected(httpException))
        }
    )
  }
}
