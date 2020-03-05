package org.simple.clinic.util

import com.squareup.moshi.Moshi
import retrofit2.HttpException

inline fun <reified T : Any> readErrorResponseJson(error: HttpException, moshi: Moshi): T {
  val jsonAdapter = moshi.adapter(T::class.java)
  return jsonAdapter.fromJson(error.response()!!.errorBody()!!.source())!!
}
