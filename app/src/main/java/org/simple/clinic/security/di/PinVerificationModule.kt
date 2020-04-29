package org.simple.clinic.security.di

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import org.simple.clinic.security.pin.verification.LocalUserPinVerificationMethod
import org.simple.clinic.security.pin.verification.LoginPinServerVerificationMethod
import org.simple.clinic.security.pin.verification.OngoingLoginEntryPinVerificationMethod
import org.simple.clinic.security.pin.verification.PinVerificationMethod

@Module
abstract class PinVerificationModule {

  @Binds
  @IntoMap
  @VerificationMethodKey(Method.Local)
  abstract fun bindsLocalPinVerificationMethod(
      pinVerificationMethod: LocalUserPinVerificationMethod
  ): PinVerificationMethod

  @Binds
  @IntoMap
  @VerificationMethodKey(Method.OngoingEntry)
  abstract fun bindsOngoingEntryPinVerificationMethod(
      pinVerificationMethod: OngoingLoginEntryPinVerificationMethod
  ): PinVerificationMethod

  @Binds
  @IntoMap
  @VerificationMethodKey(Method.LoginPinOnServer)
  abstract fun bindsLoginPinServerVerificationMethod(
      pinVerificationMethod: LoginPinServerVerificationMethod
  ): PinVerificationMethod
}
