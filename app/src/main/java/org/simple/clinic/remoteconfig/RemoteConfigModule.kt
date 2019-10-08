package org.simple.clinic.remoteconfig

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Module
import dagger.Provides

@Module
class RemoteConfigModule {

  @Provides
  fun provideRemoteConfigService(
      firebaseRemoteConfig: FirebaseRemoteConfig,
      cacheExpiration: FirebaseRemoteConfigCacheExpiration
  ): RemoteConfigService {
    return FirebaseRemoteConfigService(firebaseRemoteConfig, cacheExpiration)
  }
}
