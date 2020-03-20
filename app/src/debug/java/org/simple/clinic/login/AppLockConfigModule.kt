package org.simple.clinic.login

import dagger.Module
import dagger.Provides
import io.reactivex.Single
import org.simple.clinic.login.applock.AppLockConfig
import java.util.concurrent.TimeUnit

@Module
class AppLockConfigModule {

  @Provides
  fun appLockConfig(): Single<AppLockConfig> {
    return Single.just(AppLockConfig(lockAfterTimeMillis = TimeUnit.SECONDS.toMillis(30)))
  }
}
