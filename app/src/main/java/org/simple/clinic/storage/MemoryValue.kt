package org.simple.clinic.storage

/**
 * A class that can be used to store a reference to a single, mutable
 * value for the duration of a session. Not meant for persisting to disk
 * and there are better options for those.
 **/
data class MemoryValue<T>(
    private val defaultValue: T,
    private var currentValue: T? = null
) {

  val hasValue: Boolean
    get() = currentValue != null

  fun clear() {
    synchronized(this) {
      currentValue = null
    }
  }

  fun set(newValue: T?) {
    synchronized(this) {
      currentValue = newValue
    }
  }

  fun get(): T = currentValue ?: defaultValue
}
