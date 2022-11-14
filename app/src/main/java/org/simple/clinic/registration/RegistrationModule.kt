package org.simple.clinic.registration

import dagger.Module
import dagger.Provides
import org.simple.clinic.registration.phone.PhoneNumberValidator

@Module(
    includes = [RegistrationConfigModule::class]
)
class RegistrationModule {

  @Provides
  fun phoneNumberValidator(): PhoneNumberValidator {
    return PhoneNumberValidator(minimumRequiredLength = 6)
  }
}
