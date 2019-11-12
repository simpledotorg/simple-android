package org.simple.clinic.di

import dagger.Module
import dagger.Provides
import org.simple.clinic.BuildConfig
import retrofit2.Retrofit
import javax.inject.Named

@Module
class CountryRetrofitModule {

  @Provides
  @AppScope
  @Named("for_country")
  fun retrofit(commonRetrofitBuilder: Retrofit.Builder): Retrofit {
    val baseUrl = BuildConfig.API_ENDPOINT
    val currentApiVersion = "v3"

    return commonRetrofitBuilder
        .baseUrl("$baseUrl$currentApiVersion/")
        .build()
  }
}
