package org.simple.clinic.remoteconfig.firebase

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import io.reactivex.Completable
import org.simple.clinic.platform.crash.CrashReporter
import org.simple.clinic.remoteconfig.ConfigReader
import org.simple.clinic.remoteconfig.RemoteConfigService
import org.simple.clinic.util.toCompletable
import timber.log.Timber
import javax.inject.Inject

class FirebaseRemoteConfigService @Inject constructor(
    private val firebaseRemoteConfig: FirebaseRemoteConfig,
    // This is marked as Lazy to avoid a cyclic dependency between FirebaseRemoteConfigService and
    // SentryCrashReporter (which happens only in release builds).
    // TODO(vs): 2020-01-17 Change CrashReporter to be something like Timber, which can live outside the DI graph
    private val crashReporter: dagger.Lazy<CrashReporter>
) : RemoteConfigService {

  override fun reader(): ConfigReader = FirebaseConfigReader(firebaseRemoteConfig)

  override fun update(): Completable {
    return firebaseRemoteConfig
        .fetchAndActivate()
        .toCompletable(onTaskUnsuccessful = { Timber.w("Failed to update Firebase remote config") })
        .doOnComplete { Timber.i("Firebase remote config updated successfully") }
        .doOnError { crashReporter.get().report(it) }
  }
}
