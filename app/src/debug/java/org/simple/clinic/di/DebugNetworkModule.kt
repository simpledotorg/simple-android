package org.simple.clinic.di

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import org.simple.clinic.remoteconfig.FirebaseRemoteConfigCacheExpiration

class DebugNetworkModule : NetworkModule() {
  override fun remoteConfig(): FirebaseRemoteConfig {
    return super.remoteConfig().apply {
      // Enable developer mode so that calls are not throttled during dev.
      // More details in FirebaseRemoteConfigCacheExpiration.
      setConfigSettings(FirebaseRemoteConfigSettings.Builder()
          .setDeveloperModeEnabled(true)
          .build())
    }
  }

  override fun remoteConfigCacheExpiration() = FirebaseRemoteConfigCacheExpiration.DEBUG
}
