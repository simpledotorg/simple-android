package org.simple.clinic.setup

import dagger.Module
import dagger.Provides
import java.time.Duration

@Module
class SetupActivityConfigModule {

  @Provides
  fun providesSetupActivityConfig(): SetupActivityConfig {
    return SetupActivityConfig(databaseMaintenanceTaskInterval = Duration.ofMinutes(1))
  }
}
