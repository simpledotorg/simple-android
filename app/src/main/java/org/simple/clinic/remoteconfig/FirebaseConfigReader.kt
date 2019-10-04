package org.simple.clinic.remoteconfig

import com.google.android.gms.tasks.Task
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigValue
import io.reactivex.Completable
import io.reactivex.CompletableEmitter
import timber.log.Timber

class FirebaseConfigReader(
    private val remoteConfig: FirebaseRemoteConfig,
    private val cacheExpiration: FirebaseRemoteConfigCacheExpiration
) : ConfigReader {

  private inline fun <T : Any> read(name: String, default: T, converter: (FirebaseRemoteConfigValue) -> T): T {
    val remoteValue = remoteConfig.getValue(name)
    return when {
      remoteValue.source == FirebaseRemoteConfig.VALUE_SOURCE_STATIC -> default
      else -> converter(remoteValue)
    }
  }

  override fun string(name: String, default: String): String {
    return read(name, default) { it.asString() }
  }

  override fun boolean(name: String, default: Boolean): Boolean {
    return read(name, default) { it.asBoolean() }
  }

  override fun double(name: String, default: Double): Double {
    return read(name, default) { it.asDouble() }
  }

  override fun long(name: String, default: Long): Long {
    return read(name, default) { it.asLong() }
  }

  override fun update(): Completable {
    return Completable
        .create(::fetchRemoteConfigFromFirebase)
        .doOnComplete { Timber.i("Firebase remote config updated successfully") }
        .doOnComplete { remoteConfig.activateFetched() }
  }

  private fun fetchRemoteConfigFromFirebase(emitter: CompletableEmitter) {
    val task: Task<Void> = remoteConfig.fetch(cacheExpiration.value.seconds)

    val handler = CompletableEmitterGmsTaskHandler<Void>()

    handler.bind(emitter, task) { Timber.w("Failed to update Firebase remote config") }
  }
}
