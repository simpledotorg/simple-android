package org.simple.clinic.util

import com.squareup.moshi.Moshi
import retrofit2.HttpException
import retrofit2.Response

inline fun <reified T : Any> readErrorResponseJson(error: HttpException, moshi: Moshi): T {
  val jsonAdapter = moshi.adapter(T::class.java)
  return jsonAdapter.fromJson(error.response()!!.errorBody()!!.source())!!
}


fun <T> Response<T>.read(): T? {
  return if (isSuccessful) body() else throw HttpException(this)
}
