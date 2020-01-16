package org.simple.clinic.remoteconfig

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Module
import dagger.Provides
import org.simple.clinic.platform.crash.CrashReporter
import org.simple.clinic.remoteconfig.firebase.FirebaseRemoteConfigService
import org.threeten.bp.Duration
import javax.inject.Named

@Module
class RemoteConfigModule {

  @Provides
  fun provideRemoteConfigService(
      firebaseRemoteConfig: FirebaseRemoteConfig,
      @Named("firebase_cache_expiration_duration") cacheExpirationDuration: Duration
      crashReporter: dagger.Lazy<CrashReporter>
  ): RemoteConfigService {
    return FirebaseRemoteConfigService(firebaseRemoteConfig, crashReporter, cacheExpirationDuration)
  }

  @Provides
  fun remoteConfigReader(service: RemoteConfigService): ConfigReader {
    return service.reader()
  }
}
