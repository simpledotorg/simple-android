package org.simple.clinic.remoteconfig

import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import org.threeten.bp.Duration

/**
 * Duration to cache local values before they're synced with Firebase again.
 * This is required because Firebase throttles network calls by returning cached
 * values once the limit is reached. More information here:
 *
 * https://firebase.google.com/docs/remote-config/android
 *
 * TODO: convert to inline class once they're stable.
 */
data class FirebaseRemoteConfigCacheExpiration(val value: Duration) {

  companion object {
    /**
     * Calls on production builds are limited to a maximum of 5 per 60 minutes.
     */
    val PRODUCTION = FirebaseRemoteConfigCacheExpiration(Duration.ofMinutes(12))

    /**
     * Firebase says calls are unthrottled when developer mode is enabled with up
     * to 10 developers. Developer mode is enabled using
     * [FirebaseRemoteConfigSettings.Builder.setDeveloperModeEnabled].
     */
    val DEBUG = FirebaseRemoteConfigCacheExpiration(Duration.ofMinutes(0))
  }
}
