package org.simple.clinic.remoteconfig.firebase

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import dagger.Module
import dagger.Provides
import org.simple.clinic.di.AppScope
import org.threeten.bp.Duration
import javax.inject.Named

@Module
class FirebaseRemoteConfigModule {

  @Provides
  @AppScope
  fun remoteConfig(): FirebaseRemoteConfig {
    return FirebaseRemoteConfig.getInstance().apply {
      // Enable developer mode so that calls are not throttled during dev.
      // More details in FirebaseRemoteConfigCacheExpiration.
      setConfigSettings(FirebaseRemoteConfigSettings.Builder()
          .setDeveloperModeEnabled(true)
          .build()
      )
    }
  }

  @Provides
  @Named("firebase_cache_expiration_duration")
  fun remoteConfigCacheExpiration(): Duration {
    /**
     * Firebase says calls are unthrottled when developer mode is enabled with up
     * to 10 developers. Developer mode is enabled using
     * [FirebaseRemoteConfigSettings.Builder.setDeveloperModeEnabled].
     */
    return Duration.ofMinutes(0)
  }
}
