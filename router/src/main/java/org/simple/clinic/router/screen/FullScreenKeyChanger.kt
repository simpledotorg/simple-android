package org.simple.clinic.router.screen

import android.app.Activity
import android.support.annotation.IdRes
import android.view.ViewGroup

import flow.KeyChanger

/**
 * Coordinates changes between [FullScreenKey]s.
 *
 * @param [screenLayoutContainerRes] ViewGroup where layouts for [FullScreenKey] will be inflated.
 */
class FullScreenKeyChanger(
    private val activity: Activity,
    @IdRes private val screenLayoutContainerRes: Int
) : BaseViewGroupKeyChanger<FullScreenKey>(), KeyChanger {

  override fun layoutResForKey(screenKey: FullScreenKey): Int {
    return screenKey.layoutRes()
  }

  override fun canHandleKey(incomingKey: Any): Boolean {
    return incomingKey is FullScreenKey
  }

  override fun screenLayoutContainer(): ViewGroup {
    return activity.findViewById(screenLayoutContainerRes)
  }
}
