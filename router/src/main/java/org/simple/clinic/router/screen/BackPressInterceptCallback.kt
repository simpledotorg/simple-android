package org.simple.clinic.router.screen

class BackPressInterceptCallback {

  var intercepted: Boolean = false

  /**
   * Mark this back-press as intercepted to disallow other interceptors
   * and the host Activity from receiving it.
   */
  fun markBackPressIntercepted() {
    intercepted = true
  }
}
