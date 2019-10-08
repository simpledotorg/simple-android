package org.simple.clinic.remoteconfig

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Module
import dagger.Provides
import org.simple.clinic.di.AppScope

@Module
open class RemoteConfigModule {

  @Provides
  fun provideRemoteConfigService(
      firebaseRemoteConfig: FirebaseRemoteConfig,
      cacheExpiration: FirebaseRemoteConfigCacheExpiration
  ): RemoteConfigService {
    return FirebaseRemoteConfigService(firebaseRemoteConfig, cacheExpiration)
  }

  @Provides
  fun remoteConfigReader(service: RemoteConfigService): ConfigReader {
    return service.reader()
  }

  @Provides
  @AppScope
  open fun remoteConfig(): FirebaseRemoteConfig {
    return FirebaseRemoteConfig.getInstance()
  }

  @Provides
  open fun remoteConfigCacheExpiration(): FirebaseRemoteConfigCacheExpiration {
    return FirebaseRemoteConfigCacheExpiration.PRODUCTION
  }
}
