package org.simple.clinic.registration

import dagger.Module
import dagger.Provides
import io.reactivex.Single
import org.simple.clinic.registration.phone.IndianPhoneNumberValidator
import org.simple.clinic.registration.phone.PhoneNumberValidator
import retrofit2.Retrofit

@Module
open class RegistrationModule {

  @Provides
  fun registrationApi(retrofit: Retrofit): RegistrationApiV1 {
    return retrofit.create(RegistrationApiV1::class.java)
  }

  @Provides
  open fun registrationConfig(): Single<RegistrationConfig> {
    return Single.just(RegistrationConfig(retryBackOffDelayInMinutes = 1))
  }

  @Provides
  open fun phoneNumberValidator(): PhoneNumberValidator {
    // In the future, we will want to return a validator depending upon the location.
    return IndianPhoneNumberValidator()
  }
}
