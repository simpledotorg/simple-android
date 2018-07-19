package org.simple.clinic.router.screen

import android.app.Activity
import android.support.annotation.IdRes
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.view.View
import android.view.ViewGroup
import flow.Direction
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

  override fun animate(
      outgoingKey: FullScreenKey?,
      outgoingView: View?,
      incomingKey: FullScreenKey,
      incomingView: View,
      direction: flow.Direction,
      onCompleteListener: () -> Unit
  ) {
    if (outgoingView == null || direction == flow.Direction.REPLACE) {
      onCompleteListener.invoke()
      return
    }

    val scaleChange = 0.05f
    val duration = 200L
    val interpolator = FastOutSlowInInterpolator()

    if (direction == Direction.FORWARD || direction == Direction.REPLACE) {
      outgoingView.animate()
          .scaleX(1f - scaleChange)
          .scaleY(1f - scaleChange)
          .alpha(0f)
          .setDuration(duration)
          .setInterpolator(interpolator)
          .start()

    } else {
      outgoingView.animate()
          .scaleX(1f + scaleChange)
          .scaleY(1f + scaleChange)
          .alpha(0f)
          .setDuration(duration)
          .setInterpolator(interpolator)
          .start()
    }

    if (direction == Direction.FORWARD || direction == Direction.REPLACE) {
      incomingView.scaleX = 1f + scaleChange
      incomingView.scaleY = 1f + scaleChange
      incomingView.alpha = 0f
      incomingView.animate()
          .scaleX(1f)
          .scaleY(1f)
          .alpha(1f)
          .setDuration(duration)
          .setInterpolator(interpolator)
          .withEndAction {
            onCompleteListener.invoke()
          }
          .start()

    } else {
      incomingView.scaleX = 1f - scaleChange
      incomingView.scaleY = 1f - scaleChange
      incomingView.alpha = 0f
      incomingView.animate()
          .scaleX(1f)
          .scaleY(1f)
          .alpha(1f)
          .setDuration(duration)
          .setInterpolator(interpolator)
          .withEndAction {
            onCompleteListener.invoke()
          }
          .start()
    }
  }
}
