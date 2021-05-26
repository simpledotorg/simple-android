package org.simple.clinic.remoteconfig.firebase

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import dagger.Module
import dagger.Provides
import org.simple.clinic.di.AppScope
import timber.log.Timber
import java.time.Duration

@Module
class FirebaseRemoteConfigModule {

  @Provides
  @AppScope
  fun remoteConfig(): FirebaseRemoteConfig {
    return FirebaseRemoteConfig.getInstance().apply {
      val settings = FirebaseRemoteConfigSettings
          .Builder()
          .setMinimumFetchIntervalInSeconds(Duration.ZERO.seconds)
          .build()

      setConfigSettingsAsync(settings)
          .addOnSuccessListener { Timber.i("Set remote config settings") }
          .addOnFailureListener { Timber.e(it) }
    }
  }
}
