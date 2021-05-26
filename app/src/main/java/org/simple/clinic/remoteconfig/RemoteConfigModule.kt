package org.simple.clinic.remoteconfig

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Module
import dagger.Provides
import org.simple.clinic.remoteconfig.firebase.FirebaseRemoteConfigService

@Module
class RemoteConfigModule {

  @Provides
  fun provideRemoteConfigService(
      firebaseRemoteConfig: FirebaseRemoteConfig
  ): RemoteConfigService {
    return FirebaseRemoteConfigService(firebaseRemoteConfig)
  }

  @Provides
  fun remoteConfigReader(service: RemoteConfigService): ConfigReader {
    return service.reader()
  }
}
