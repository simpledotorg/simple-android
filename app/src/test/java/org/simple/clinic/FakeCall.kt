package org.simple.clinic

import okhttp3.MediaType
import okhttp3.Request
import okhttp3.ResponseBody
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit

class FakeCall<T> private constructor(
    private val responseData: Pair<Int, T>? = null,
    private val responseErrorData: Triple<MediaType, Int, String>? = null,
    private val responseFailure: Throwable? = null
) : Call<T> {

  private var executed = false

  companion object {
    fun <T> success(response: T, responseCode: Int = 200): FakeCall<T> {
      return FakeCall(responseData = Pair(responseCode, response))
    }

    fun <T> error(data: String, responseCode: Int, mediaType: MediaType = MediaType.parse("application/json")!!): FakeCall<T> {
      return FakeCall(responseErrorData = Triple(mediaType, responseCode, data))
    }

    fun <T> failure(cause: Throwable): FakeCall<T> {
      return FakeCall(responseFailure = cause)
    }
  }

  override fun enqueue(callback: Callback<T>) {
    executed = true
    when {
      responseData != null -> callback.onResponse(this, Response.success(responseData.first, responseData.second))
      responseErrorData != null -> {
        val body = ResponseBody.create(responseErrorData.first, responseErrorData.third)
        callback.onResponse(this, Response.error<T>(responseErrorData.second, body))
      }
      responseFailure != null -> callback.onFailure(this, responseFailure)
      else -> throw RuntimeException("Should not happen!")
    }
  }

  override fun isExecuted(): Boolean = executed

  override fun clone(): Call<T> {
    throw UnsupportedOperationException()
  }

  override fun isCanceled(): Boolean {
    throw UnsupportedOperationException()
  }

  override fun cancel() {
    throw UnsupportedOperationException()
  }

  override fun execute(): Response<T> {
    executed = true
    return when {
      responseData != null -> Response.success(responseData.first, responseData.second)
      responseErrorData != null -> {
        val body = ResponseBody.create(responseErrorData.first, responseErrorData.third)
        Response.error<T>(responseErrorData.second, body)
      }
      responseFailure != null -> throw responseFailure
      else -> throw RuntimeException("Should not happen!")
    }
  }

  override fun request(): Request {
    throw UnsupportedOperationException()
  }

  override fun timeout(): Timeout {
    return Timeout().apply {
      timeout(60, TimeUnit.SECONDS)
    }
  }
}
