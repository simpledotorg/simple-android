package org.simple.clinic.remoteconfig.firebase

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import dagger.Module
import dagger.Provides
import org.simple.clinic.di.AppScope
import org.simple.clinic.platform.crash.CrashReporter
import org.threeten.bp.Duration
import timber.log.Timber
import javax.inject.Named

@Module
class FirebaseRemoteConfigModule {

  @Provides
  @AppScope
  fun remoteConfig(crashReporter: dagger.Lazy<CrashReporter>): FirebaseRemoteConfig {
    return FirebaseRemoteConfig.getInstance().apply {
      val settings = FirebaseRemoteConfigSettings
          .Builder()
          .setMinimumFetchIntervalInSeconds(Duration.ofHours(1).seconds)
          .build()

      setConfigSettingsAsync(settings)
          .addOnSuccessListener { Timber.tag("FRC").i("Set remote config settings") }
          .addOnFailureListener { crashReporter.get().report(it) }
    }
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
