package org.simple.clinic.remoteconfig.firebase

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import io.reactivex.Completable
import org.simple.clinic.remoteconfig.ConfigReader
import org.simple.clinic.remoteconfig.RemoteConfigService
import org.simple.clinic.util.toCompletable
import org.threeten.bp.Duration
import timber.log.Timber
import javax.inject.Inject

class FirebaseRemoteConfigService @Inject constructor(
    private val firebaseRemoteConfig: FirebaseRemoteConfig,
    /**
     * Duration to cache local values before they're synced with Firebase again.
     * This is required because Firebase throttles network calls by returning cached
     * values once the limit is reached. More information here:
     *
     * https://firebase.google.com/docs/remote-config/android
     */
    private val cacheExpirationDuration: Duration
) : RemoteConfigService {

  override fun reader(): ConfigReader = FirebaseConfigReader(firebaseRemoteConfig)

  override fun update(): Completable {
    return firebaseRemoteConfig
        .fetch(cacheExpirationDuration.seconds)
        .toCompletable { Timber.w("Failed to update Firebase remote config") }
        .doOnComplete {
          Timber.i("Firebase remote config updated successfully")
          firebaseRemoteConfig.activateFetched()
        }
  }
}
