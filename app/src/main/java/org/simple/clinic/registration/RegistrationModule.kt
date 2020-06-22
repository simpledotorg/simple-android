package org.simple.clinic.registration

import dagger.Module
import dagger.Provides
import org.simple.clinic.appconfig.Country
import org.simple.clinic.registration.phone.LengthBasedNumberValidator
import org.simple.clinic.registration.phone.PhoneNumberValidator
import org.simple.clinic.util.Distance
import org.threeten.bp.Duration

@Module
class RegistrationModule {

  @Provides
  fun config(): RegistrationConfig {
    return RegistrationConfig(
        locationListenerExpiry = Duration.ofSeconds(5),
        locationUpdateInterval = Duration.ofSeconds(1),
        proximityThresholdForNearbyFacilities = Distance.ofKilometers(2.0),
        staleLocationThreshold = Duration.ofMinutes(10)
    )
  }

  @Provides
  fun phoneNumberValidator(country: Country): PhoneNumberValidator {
    // In the future, we will want to return a validator depending upon the location.
    return when (country.isoCountryCode) {
      Country.ETHIOPIA -> LengthBasedNumberValidator(
          minimumRequiredLengthMobile = 9,
          maximumAllowedLengthMobile = 10,
          minimumRequiredLengthLandlinesOrMobile = 9,
          maximumAllowedLengthLandlinesOrMobile = 10)
      else -> LengthBasedNumberValidator(
          minimumRequiredLengthMobile = 10,
          maximumAllowedLengthMobile = 10,
          minimumRequiredLengthLandlinesOrMobile = 6,
          maximumAllowedLengthLandlinesOrMobile = 12)
    }
  }
}
