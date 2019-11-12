package org.simple.clinic.remoteconfig.firebase

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Module
import dagger.Provides
import org.simple.clinic.di.AppScope

@Module
class FirebaseRemoteConfigModule {

  @Provides
  @AppScope
  fun remoteConfig(): FirebaseRemoteConfig {
    return FirebaseRemoteConfig.getInstance()
  }

  @Provides
  @Named("firebase_cache_expiration_duration")
  fun remoteConfigCacheExpiration(): Duration {
    /**
     * Calls on production builds are limited to a maximum of 5 per 60 minutes.
     */
    return Duration.ofMinutes(12)
  }
}
