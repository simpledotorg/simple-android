package org.simple.clinic.login

import dagger.Module
import dagger.Provides
import org.simple.clinic.login.applock.AppLockConfig
import java.util.concurrent.TimeUnit

@Module
class AppLockConfigModule {

  @Provides
  fun appLockConfig(): AppLockConfig {
    return AppLockConfig(lockAfterTimeMillis = TimeUnit.SECONDS.toMillis(30))
  }
}
