package org.simple.clinic.login

import dagger.Binds
import dagger.Module

@Module
abstract class LoginOtpSmsListenerModule {

  @Binds
  abstract fun loginSmsListener(listener: LoginOtpSmsListenerImpl): LoginOtpSmsListener
}
