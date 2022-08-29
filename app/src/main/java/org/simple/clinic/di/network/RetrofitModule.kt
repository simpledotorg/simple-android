package org.simple.clinic.di.network

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import org.simple.clinic.BuildConfig
import org.simple.clinic.appconfig.Deployment
import org.simple.clinic.di.AppScope
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Named

@Module
class RetrofitModule {

  @Provides
  @AppScope
  fun retrofitBuilder(moshi: Moshi, okHttpClient: OkHttpClient): Retrofit.Builder {
    return Retrofit.Builder()
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addCallAdapterFactory(TimeoutCallAdapterFactory.create())
        .client(okHttpClient)
        .validateEagerly(true)
  }

  @Provides
  @AppScope
  @Named("for_config")
  fun configurationRetrofit(commonRetrofitBuilder: Retrofit.Builder): Retrofit {
    return commonRetrofitBuilder
        .baseUrl(BuildConfig.MANIFEST_ENDPOINT)
        .build()
  }

  @Provides
  @AppScope
  @Named("for_deployment")
  fun retrofit(
      deployment: Deployment,
      commonRetrofitBuilder: Retrofit.Builder
  ): Retrofit {
    // Since the endpoint is not under our control, and is defined at the server level,
    // this is a safety check that will generate the right endpoint regardless of whether the
    // endpoint defined in the manifest has a trailing slash or not.
    val baseUrl = deployment.endPoint.toString().removeSuffix("/")
    val endpoint = "$baseUrl/".toHttpUrl()

    return commonRetrofitBuilder
        .baseUrl(endpoint)
        .build()
  }
}
