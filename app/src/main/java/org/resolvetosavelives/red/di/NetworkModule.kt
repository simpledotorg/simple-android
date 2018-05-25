package org.resolvetosavelives.red.di

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import org.resolvetosavelives.red.util.InstantMoshiTypeConverter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

@Module
class NetworkModule {

  @Provides
  fun moshi(): Moshi {
    return Moshi.Builder()
        .add(InstantMoshiTypeConverter())
        .build()
  }

  @Provides
  fun retrofitBuilder(moshi: Moshi): Retrofit.Builder {
    return Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .validateEagerly(true)
  }
}
