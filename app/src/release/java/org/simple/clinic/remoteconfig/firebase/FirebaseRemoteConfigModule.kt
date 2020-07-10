package org.simple.clinic.remoteconfig.firebase

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import dagger.Module
import dagger.Provides
import org.simple.clinic.di.AppScope
import org.simple.clinic.platform.crash.CrashReporter
import java.time.Duration
import timber.log.Timber

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
}
