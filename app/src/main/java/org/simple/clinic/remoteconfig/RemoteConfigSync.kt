package org.simple.clinic.remoteconfig

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import io.reactivex.Completable
import io.reactivex.Single
import org.simple.clinic.BuildConfig
import org.simple.clinic.sync.BatchSize
import org.simple.clinic.sync.ModelSync
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.sync.SyncGroup
import org.simple.clinic.sync.SyncInterval
import timber.log.Timber
import javax.inject.Inject

class RemoteConfigSync @Inject constructor(private val remoteConfig: FirebaseRemoteConfig) : ModelSync {

  override fun sync(): Completable = pull()

  override fun push(): Completable = Completable.complete()

  override fun pull(): Completable {
    // Firebase throttles network calls to 5 per hour.
    // Source: https://firebase.google.com/docs/remote-config/android
    val cacheExpiration = if (BuildConfig.DEBUG) 0 else SyncInterval.FREQUENT.frequency.seconds

    return Completable.fromAction {
      remoteConfig.fetch(cacheExpiration)
          .addOnCompleteListener { task ->
            if (task.isSuccessful) {
              Timber.i("Firebase remote config updated successfully")
              remoteConfig.activateFetched()

            } else {
              Timber.w("Failed to update Firebase remote config")
            }
          }
    }
  }

  override fun syncConfig(): Single<SyncConfig> {
    return Single.just(SyncConfig(
        syncInterval = SyncInterval.FREQUENT,
        batchSize = BatchSize.SMALL,
        syncGroup = SyncGroup.FREQUENT))
  }
}
