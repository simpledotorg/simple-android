package org.simple.clinic.remoteconfig

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import io.reactivex.Completable
import io.reactivex.Single
import org.simple.clinic.sync.BatchSize
import org.simple.clinic.sync.ModelSync
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.sync.SyncGroup
import org.simple.clinic.sync.SyncInterval
import timber.log.Timber
import javax.inject.Inject

class RemoteConfigSync @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig,
    private val cacheExpiration: FirebaseRemoteConfigCacheExpiration
) : ModelSync {

  override fun sync(): Completable = pull()

  override fun push(): Completable = Completable.complete()

  override fun pull(): Completable {
    return Completable.fromAction {
      remoteConfig.fetch(cacheExpiration.value.seconds)
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
