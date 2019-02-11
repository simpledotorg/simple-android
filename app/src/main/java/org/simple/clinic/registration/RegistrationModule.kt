package org.simple.clinic.registration

import dagger.Module
import dagger.Provides
import io.reactivex.Single
import org.simple.clinic.registration.phone.IndianPhoneNumberValidator
import org.simple.clinic.registration.phone.PhoneNumberValidator
import org.simple.clinic.util.Kilometers
import org.threeten.bp.Duration
import retrofit2.Retrofit

@Module
open class RegistrationModule {

  @Provides
  fun api(retrofit: Retrofit): RegistrationApiV1 {
    return retrofit.create(RegistrationApiV1::class.java)
  }

  @Provides
  open fun config(): Single<RegistrationConfig> {
    return Single.just(RegistrationConfig(
        retryBackOffDelayInMinutes = 1,
        locationListenerExpiry = Duration.ofSeconds(5),
        locationUpdateInterval = Duration.ofSeconds(1),
        proximityThresholdForNearbyFacilities = Kilometers(2.0)))   // TODO: Verify this value with design team.
  }

  @Provides
  open fun phoneNumberValidator(): PhoneNumberValidator {
    // In the future, we will want to return a validator depending upon the location.
    return IndianPhoneNumberValidator()
  }
}
