package org.simple.clinic.router.screen

import android.app.Activity
import android.content.Context
import android.support.annotation.ColorRes
import android.support.annotation.IdRes
import android.support.v4.content.ContextCompat
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import flow.Direction
import flow.KeyChanger
import flow.State
import flow.TraversalCallback

/**
 * Coordinates changes between [FullScreenKey]s.
 *
 * @param [screenLayoutContainerRes] ViewGroup where layouts for [FullScreenKey] will be inflated.
 */
class FullScreenKeyChanger(
    private val activity: Activity,
    @IdRes private val screenLayoutContainerRes: Int,
    @ColorRes private val screenBackgroundRes: Int,
    private val onKeyChange: (FullScreenKey?, FullScreenKey) -> Unit = { _, _ -> }
) : BaseViewGroupKeyChanger<FullScreenKey>(), KeyChanger {

  private val containerIds = mutableMapOf<FullScreenKey, Int>()

  override fun layoutResForKey(screenKey: FullScreenKey): Int {
    return screenKey.layoutRes()
  }

  override fun canHandleKey(incomingKey: Any): Boolean {
    return incomingKey is FullScreenKey
  }

  override fun screenLayoutContainer(): ViewGroup {
    return activity.findViewById(screenLayoutContainerRes)
  }

  override fun inflateIncomingView(incomingContext: Context, incomingKey: FullScreenKey, frame: ViewGroup): View {
    // If the backstack is changed while a screen change animation was ongoing, the screens
    // end up overlapping with each other. It's difficult to debug if it's a problem with Flow
    // or in our code. As a workaround, the window background is applied on every screen.
    val container = FrameLayout(incomingContext)

    // The ID for each screen's container should remain the same for View state restoration to work.
    if (containerIds.containsKey(incomingKey).not()) {
      containerIds[incomingKey] = View.generateViewId()
    }
    container.id = containerIds[incomingKey]!!

    val contentView = super.inflateIncomingView(incomingContext, incomingKey, container)
    container.addView(contentView)

    if (contentView.background == null) {
      container.setBackgroundColor(ContextCompat.getColor(incomingContext, screenBackgroundRes))
    } else {
      container.background = contentView.background
      contentView.background = null
    }

    return container
  }

  override fun changeKey(
      outgoingState: State?,
      incomingState: State,
      direction: Direction,
      incomingContexts: Map<Any, Context>,
      callback: TraversalCallback
  ) {
    super.changeKey(outgoingState, incomingState, direction, incomingContexts, callback)
    val outgoingKey = outgoingState?.getKey<FullScreenKey>()
    val incomingKey = incomingState.getKey<FullScreenKey>()
    onKeyChange(outgoingKey, incomingKey)
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
