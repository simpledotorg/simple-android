package org.resolvetosavelives.red.router.screen

import android.content.Context
import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import flow.*

/**
 * Base class for key-changers that change screens in a single ViewGroup.
 *
 * @param <T> Type of key that this key-changer can handle. E.g., [FullScreenKey].
</T> */
abstract class BaseViewGroupKeyChanger<T : Any> : KeyChanger {

  override fun changeKey(
      outgoingState: State?,
      incomingState: State,
      direction: Direction,
      incomingContexts: Map<Any, Context>,
      callback: TraversalCallback
  ) {
    if (!canHandleKey(incomingState.getKey())) {
      return
    }

    val frame = screenLayoutContainer()
    val incomingKey = incomingState.getKey<T>()

    if (outgoingState == null && direction == Direction.REPLACE) {
      // Short circuit if we would just be showing the same view again. Flow
      // intentionally calls changeKey() again on onResume() with the same values.
      // See: https://github.com/square/flow/issues/173.
      if (isScreenAlreadyActive(frame.getChildAt(0), incomingKey)) {
        callback.onTraversalCompleted()
        return
      }
    }

    if (frame.childCount > 0) {
      val outgoingView = frame.getChildAt(0)
      outgoingState?.save(outgoingView)
      frame.removeAllViews()
    }

    val incomingContext = incomingContexts[incomingKey]

    val incomingView = LayoutInflater.from(incomingContext).inflate(layoutResForKey(incomingKey), frame, false)
    frame.addView(incomingView)
    incomingState.restore(incomingView)

    if (incomingView.id == View.NO_ID) {
      val layoutResName = incomingView.resources.getResourceName(layoutResForKey(incomingKey))
      throw AssertionError("Screen's layout ($layoutResName) doesn't have an ID set on its root ViewGroup. " +
          "An ID is required for persisting View state.")
    }

    callback.onTraversalCompleted()
  }

  private fun isScreenAlreadyActive(view: View?, initialKey: Any?): Boolean {
    return view?.let {
      Flow.getKey<Any?>(view) == initialKey
    }
        ?: false
  }

  /**
   * True if this key-changer recognizes <var>incomingKey</var>.
   */
  abstract fun canHandleKey(incomingKey: Any): Boolean

  /**
   * The ViewGroup where screens will be inflated.
   */
  abstract fun screenLayoutContainer(): ViewGroup

  /**
   * Layout resource Id for <var>screenKey</var> that has to be shown on screen.
   */
  @LayoutRes
  abstract fun layoutResForKey(screenKey: T): Int
}
