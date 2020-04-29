package org.simple.clinic.router.screen

interface BackPressInterceptor {

  /**
   * Call [BackPressInterceptCallback.markBackPressIntercepted] to mark this back-press as intercepted.
   * Otherwise, the back-press will be offered to other intercepters and finally handled by the Activity.
   */
  fun onInterceptBackPress(callback: BackPressInterceptCallback)
}
