package org.simple.clinic.di

import dagger.Module
import dagger.Provides
import org.simple.clinic.remoteconfig.ConfigReader
import org.simple.sharedTestCode.remoteconfig.DefaultValueConfigReader
import org.simple.sharedTestCode.remoteconfig.NoOpRemoteConfigService
import org.simple.clinic.remoteconfig.RemoteConfigService

@Module
class TestRemoteConfigModule {

  @Provides
  fun remoteConfigService(configReader: ConfigReader): RemoteConfigService {
    return NoOpRemoteConfigService(configReader)
  }

  @Provides
  fun remoteConfigReader(): ConfigReader {
    return DefaultValueConfigReader()
  }

}
