package org.simple.clinic.remoteconfig

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Module
import dagger.Provides
import org.simple.clinic.platform.crash.CrashReporter
import org.simple.clinic.remoteconfig.firebase.FirebaseRemoteConfigService

@Module
class RemoteConfigModule {

  @Provides
  fun provideRemoteConfigService(
      firebaseRemoteConfig: FirebaseRemoteConfig,
      // This is marked as Lazy to avoid a cyclic dependency between FirebaseRemoteConfigService and
      // SentryCrashReporter (which happens only in release builds).
      // TODO(vs): 2020-01-17 Change CrashReporter to be something like Timber, which can live outside the DI graph
      crashReporter: dagger.Lazy<CrashReporter>
  ): RemoteConfigService {
    return FirebaseRemoteConfigService(firebaseRemoteConfig, crashReporter)
  }

  @Provides
  fun remoteConfigReader(service: RemoteConfigService): ConfigReader {
    return service.reader()
  }
}
