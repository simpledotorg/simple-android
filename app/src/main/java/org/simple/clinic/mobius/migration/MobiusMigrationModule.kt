package org.simple.clinic.mobius.migration

import dagger.Module
import dagger.Provides
import org.simple.clinic.remoteconfig.ConfigReader

@Module
class MobiusMigrationModule {

  @Provides
  fun mobiusMigrationConfig(reader: ConfigReader): MobiusMigrationConfig =
      MobiusMigrationConfig.read(reader)
}
