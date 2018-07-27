package org.simple.clinic.registration

import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
class RegistrationModule {

  @Provides
  fun registrationApi(retrofit: Retrofit): RegistrationApiV1 {
    return retrofit.create(RegistrationApiV1::class.java)
  }
}