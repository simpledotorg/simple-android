package org.simple.clinic.instantsearch

import dagger.Module
import dagger.Provides
import org.simple.clinic.remoteconfig.ConfigReader

@Module
object InstantSearchConfigModule {

  @Provides
  fun instantSearchConfig(configReader: ConfigReader): InstantSearchConfig {
    return InstantSearchConfig.read(configReader = configReader)
  }
}
