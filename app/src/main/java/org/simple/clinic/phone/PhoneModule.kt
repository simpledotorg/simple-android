package org.simple.clinic.phone

import dagger.Module
import dagger.Provides
import io.reactivex.Single

@Module
class PhoneModule {

  @Provides
  fun config(): Single<PhoneNumberMaskerConfig> = Single.just(PhoneNumberMaskerConfig(maskingEnabled = false))

  @Provides
  fun masker(twilioMasker: TwilioMaskedPhoneCaller): MaskedPhoneCaller = twilioMasker
}
