package org.simple.clinic.remoteconfig

class DefaultValueConfigReader : ConfigReader {

  override fun string(name: String, default: String): String = default

  override fun boolean(name: String, default: Boolean): Boolean = default

  override fun double(name: String, default: Double): Double = default

  override fun long(name: String, default: Long): Long = default
}
