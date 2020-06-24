package org.simple.clinic.summary.bloodsugar

import dagger.Module
import dagger.Provides
import org.simple.clinic.remoteconfig.ConfigReader

@Module
class BloodSugarSummaryConfigModule {

  @Provides
  fun bloodSugarSummaryConfig(configReader: ConfigReader): BloodSugarSummaryConfig {
    return BloodSugarSummaryConfig.read(configReader = configReader)
  }
}
