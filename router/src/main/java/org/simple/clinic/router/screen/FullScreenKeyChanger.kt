package org.simple.clinic.router.screen

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import androidx.annotation.IdRes
import flow.Direction
import flow.KeyChanger
import flow.State
import flow.TraversalCallback
import org.simple.clinic.router.util.resolveColor
import timber.log.Timber

/**
 * Coordinates changes between [FullScreenKey]s.
 *
 * @param [screenLayoutContainerRes] ViewGroup where layouts for [FullScreenKey] will be inflated.
 */
class FullScreenKeyChanger(
    private val activity: Activity,
    @IdRes private val screenLayoutContainerRes: Int,
    @AttrRes private val screenBackgroundAttr: Int,
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

  override fun inflateIncomingView(
      incomingContext: Context,
      incomingKey: FullScreenKey,
      frame: ViewGroup
  ): View {
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
      val backgroundColor = incomingContext.resolveColor(attrRes = screenBackgroundAttr)
      container.setBackgroundColor(backgroundColor)
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
    val outgoingKey = outgoingState?.getKey<FullScreenKey>()
    val incomingKey = incomingState.getKey<FullScreenKey>()

    val outgoingScreenName = outgoingKey?.analyticsName ?: ""
    val incomingScreenName = incomingKey.analyticsName

    Timber.tag("Screen Change").i("Change key [$outgoingScreenName] -> [$incomingScreenName]")

    super.changeKey(outgoingState, incomingState, direction, incomingContexts, callback)

    Timber.tag("Screen Change").i("Key changed [$outgoingScreenName] -> [$incomingScreenName]")

    onKeyChange(outgoingKey, incomingKey)
  }
}
