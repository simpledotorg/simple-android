package org.simple.clinic.screen

import android.view.View
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import flow.Direction
import flow.Direction.FORWARD
import flow.Direction.REPLACE
import org.simple.clinic.router.screen.FullScreenKey
import org.simple.clinic.router.screen.KeyChangeAnimator
import org.simple.clinic.router.screen.SCREEN_CHANGE_ANIMATION_DURATION
import javax.inject.Inject

class FullScreenKeyChangeAnimator @Inject constructor() : KeyChangeAnimator<FullScreenKey> {

  override fun animate(
      outgoingKey: FullScreenKey?,
      outgoingView: View?,
      incomingKey: FullScreenKey,
      incomingView: View,
      direction: Direction,
      onCompleteListener: () -> Unit
  ) {
    if (outgoingView == null || direction == REPLACE) {
      onCompleteListener()
      return
    }

    val scaleChange = 0.05f
    val duration = SCREEN_CHANGE_ANIMATION_DURATION
    val interpolator = FastOutSlowInInterpolator()

    if (direction == FORWARD || direction == REPLACE) {
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

    if (direction == FORWARD || direction == REPLACE) {
      incomingView.scaleX = 1f + scaleChange
      incomingView.scaleY = 1f + scaleChange
      incomingView.alpha = 0f
      incomingView.animate()
          .scaleX(1f)
          .scaleY(1f)
          .alpha(1f)
          .setDuration(duration)
          .setInterpolator(interpolator)
          .withEndAction(onCompleteListener)
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
          .withEndAction(onCompleteListener)
          .start()
    }
  }
}
