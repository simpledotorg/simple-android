package org.simple.clinic.setup

import dagger.Module
import dagger.Provides
import org.simple.clinic.remoteconfig.ConfigReader
import java.time.Duration

@Module
class SetupActivityConfigModule {

  @Provides
  fun providesSetupActivityConfig(configReader: ConfigReader): SetupActivityConfig {
    val intervalInMins = configReader.long("database_maintenance_interval", 60)
    return SetupActivityConfig(databaseMaintenanceTaskInterval = Duration.ofMinutes(intervalInMins))
  }
}
