package org.simple.clinic.mobius.migration

import dagger.Module
import dagger.Provides
import org.simple.clinic.remoteconfig.ConfigReader

@Deprecated("""
  We are no longer using this approach to migrate towards Mobius.
  This package will be deleted as soon as Edit Patient feature becomes stable.""")
@Module
class MobiusMigrationModule {

  @Provides
  fun mobiusMigrationConfig(reader: ConfigReader): MobiusMigrationConfig =
      MobiusMigrationConfig.read(reader)
}
