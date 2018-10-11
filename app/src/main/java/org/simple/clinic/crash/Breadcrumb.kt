package org.simple.clinic.crash

import android.util.Log

data class Breadcrumb(val priority: Priority, val tag: String?, val message: String) {

  /** Matched with [Log] */
  enum class Priority {
    VERBOSE,
    DEBUG,
    INFO,
    WARN,
    ERROR,
    ASSERT,
  }
}
