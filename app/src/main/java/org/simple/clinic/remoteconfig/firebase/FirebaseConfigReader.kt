package org.simple.clinic.remoteconfig.firebase

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigValue
import org.simple.clinic.remoteconfig.ConfigReader

class FirebaseConfigReader(private val remoteConfig: FirebaseRemoteConfig) : ConfigReader {

  private inline fun <T : Any> read(
      name: String,
      default: T,
      converter: (FirebaseRemoteConfigValue) -> T
  ): T {
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
}
