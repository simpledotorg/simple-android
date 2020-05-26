package org.simple.clinic.remoteconfig.firebase

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import io.reactivex.Completable
import org.simple.clinic.remoteconfig.ConfigReader
import org.simple.clinic.remoteconfig.RemoteConfigService
import org.simple.clinic.util.toCompletable
import javax.inject.Inject

class FirebaseRemoteConfigService @Inject constructor(
    private val firebaseRemoteConfig: FirebaseRemoteConfig
) : RemoteConfigService {

  override fun reader(): ConfigReader = FirebaseConfigReader(firebaseRemoteConfig)

  override fun update(): Completable {
    return firebaseRemoteConfig
        .fetchAndActivate()
        .toCompletable()
  }
}
