package org.simple.clinic.remoteconfig

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import io.reactivex.Completable
import org.simple.clinic.util.toCompletable
import timber.log.Timber
import javax.inject.Inject

class FirebaseRemoteConfigService @Inject constructor(
    private val firebaseRemoteConfig: FirebaseRemoteConfig,
    private val cacheExpiration: FirebaseRemoteConfigCacheExpiration
) : RemoteConfigService {

  override fun reader(): ConfigReader = FirebaseConfigReader(firebaseRemoteConfig, cacheExpiration)

  override fun update(): Completable {
    return firebaseRemoteConfig
        .fetch(cacheExpiration.value.seconds)
        .toCompletable { Timber.w("Failed to update Firebase remote config") }
        .doOnComplete {
          Timber.i("Firebase remote config updated successfully")
          firebaseRemoteConfig.activateFetched()
        }
  }
}
