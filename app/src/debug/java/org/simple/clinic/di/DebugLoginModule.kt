package org.simple.clinic.di

import io.reactivex.Single
import org.simple.clinic.login.LoginModule
import org.simple.clinic.login.applock.AppLockConfig
import java.util.concurrent.TimeUnit

class DebugLoginModule : LoginModule() {
  override fun appLockConfig(): Single<AppLockConfig> {
    return Single.just(AppLockConfig(lockAfterTimeMillis = TimeUnit.SECONDS.toMillis(4)))
  }
}
