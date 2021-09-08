package org.simple.clinic.appconfig

import dagger.Module
import dagger.Provides
import org.simple.clinic.di.AppScope
import org.simple.clinic.login.AppLockConfigModule
import org.simple.clinic.storage.MemoryValue
import java.time.Instant
import java.util.Optional

@Module(includes = [AppLockConfigModule::class])
class AppLockModule {

  @Provides
  @AppScope
  fun provideAppLockAfterTimestamp(): MemoryValue<Optional<Instant>> {
    return MemoryValue(defaultValue = Optional.empty())
  }
}
