package org.simple.clinic.remoteconfig.firebase

import com.google.android.gms.tasks.Tasks.await
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import org.simple.clinic.remoteconfig.ConfigReader
import org.simple.clinic.remoteconfig.RemoteConfigService
import javax.inject.Inject

class FirebaseRemoteConfigService @Inject constructor(
    private val firebaseRemoteConfig: FirebaseRemoteConfig
) : RemoteConfigService {

  override fun reader(): ConfigReader = FirebaseConfigReader(firebaseRemoteConfig)

  override fun update() {
    await(firebaseRemoteConfig.fetchAndActivate())
  }
}
