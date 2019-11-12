package org.simple.clinic.login

import android.app.Application
import dagger.Module
import dagger.Provides
import org.simple.clinic.util.scheduler.SchedulersProvider

@Module
class LoginOtpSmsListenerModule {

  @Provides
  fun loginSmsListener(app: Application, schedulersProvider: SchedulersProvider): LoginOtpSmsListener {
    return LoginOtpSmsListenerImpl(app, schedulersProvider)
  }
}
