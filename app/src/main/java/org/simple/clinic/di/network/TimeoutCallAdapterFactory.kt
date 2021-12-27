package org.simple.clinic.di.network

import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.Type

class TimeoutCallAdapterFactory : CallAdapter.Factory() {

  companion object {
    fun create(): TimeoutCallAdapterFactory {
      return TimeoutCallAdapterFactory()
    }
  }

  override fun get(returnType: Type, annotations: Array<out Annotation>, retrofit: Retrofit): CallAdapter<*, *>? {
    if (getRawType(returnType) != Call::class.java) {
      return null
    }

    val timeout = annotations.firstOrNull { it is Timeout } as? Timeout ?: return null
    val delegate = retrofit.nextCallAdapter(this, returnType, annotations)

    return object : CallAdapter<Any, Call<Any>> {
      override fun responseType(): Type {
        return delegate.responseType()
      }

      override fun adapt(call: Call<Any>): Call<Any> {
        call.timeout().timeout(timeout.value, timeout.unit)
        return call
      }
    }
  }
}
