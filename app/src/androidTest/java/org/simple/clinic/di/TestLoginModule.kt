package org.simple.clinic.di

import android.app.Application
import io.reactivex.Completable
import org.simple.clinic.login.LoginModule
import org.simple.clinic.login.LoginOtpSmsListener
import org.simple.clinic.util.scheduler.SchedulersProvider

class TestLoginModule : LoginModule() {
  override fun loginSmsListener(app: Application, schedulersProvider: SchedulersProvider): LoginOtpSmsListener {
    return object : LoginOtpSmsListener {
      override fun listenForLoginOtp(): Completable = Completable.complete()
    }
  }
}
