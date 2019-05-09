package org.simple.clinic.util

import io.reactivex.exceptions.CompositeException
import okhttp3.internal.http2.ConnectionShutdownException
import okhttp3.internal.http2.StreamResetException
import org.simple.clinic.util.ResolvedError.*
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException
import kotlin.reflect.KClass

private val KNOWN_NETWORK_ERRORS: Set<KClass<out IOException>> = setOf(
    SocketException::class,
    SocketTimeoutException::class,
    UnknownHostException::class,
    StreamResetException::class,
    ConnectionShutdownException::class,
    SSLHandshakeException::class,
    ConnectException::class)

object ErrorResolver {

  fun resolve(error: Throwable): ResolvedError {
    val actualCause = findActualCause(error)

    return when {
      actualCause::class in KNOWN_NETWORK_ERRORS -> NetworkRelated(actualCause)
      actualCause is HttpException && actualCause.code() == 401 -> Unauthorized(actualCause)
      else -> Unexpected(actualCause)
    }
  }

  private fun findActualCause(error: Throwable): Throwable {
    var actualError = error
    if (actualError is CompositeException) {
      // CompositeException wraps the cause inside another
      // exception called CompositeExceptionCausalChain.
      actualError = findActualCause(actualError.cause.cause!!)
    }
    // This place may identify more wrapped errors
    // like UndeliverableException, etc. in the future.
    return actualError
  }
}

sealed class ResolvedError(val actualCause: Throwable) {

  class NetworkRelated(actualCause: Throwable) : ResolvedError(actualCause)

  class Unexpected(actualCause: Throwable) : ResolvedError(actualCause)

  class Unauthorized(actualCause: Throwable) : ResolvedError(actualCause)
}
