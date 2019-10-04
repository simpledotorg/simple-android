package org.simple.clinic.remoteconfig

import io.reactivex.Completable

interface ConfigReader {
  fun string(name: String, default: String): String
  fun boolean(name: String, default: Boolean): Boolean
  fun double(name: String, default: Double): Double
  fun long(name: String, default: Long): Long
  fun update(): Completable
}
