package org.simple.clinic.registration

import dagger.Module
import dagger.Provides
import org.simple.clinic.appconfig.Country
import org.simple.clinic.registration.phone.LengthBasedNumberValidator
import org.simple.clinic.registration.phone.PhoneNumberValidator

@Module
class RegistrationModule {

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
