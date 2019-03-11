package org.simple.clinic.phone

import dagger.Module
import dagger.Provides

@Module
class PhoneModule {

  @Provides
  fun masker(twilioMasker: TwilioMaskedPhoneCaller): MaskedPhoneCaller = twilioMasker
}
