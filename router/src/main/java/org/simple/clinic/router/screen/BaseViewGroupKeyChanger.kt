package org.simple.clinic.router.screen

import android.content.Context
import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import flow.Flow
import flow.KeyChanger
import flow.State
import flow.TraversalCallback
import timber.log.Timber

/**
 * Base class for key-changers that change screens in a single ViewGroup.
 *
 * @param [T] Type of key that this key-changer can handle. E.g., [FullScreenKey].
 */
abstract class BaseViewGroupKeyChanger<T : Any> : KeyChanger {

  override fun changeKey(
      outgoingState: State?,
      incomingState: State,
      direction: flow.Direction,
      incomingContexts: Map<Any, Context>,
      callback: TraversalCallback
  ) {
    if (!canHandleKey(incomingState.getKey())) {
      return
    }

    val frame = screenLayoutContainer()
    val incomingKey = incomingState.getKey<T>()

    if (outgoingState == null && direction == flow.Direction.REPLACE) {
      // Short circuit if we would just be showing the same view again. Flow
      // intentionally calls changeKey() again on onResume() with the same values.
      // See: https://github.com/square/flow/issues/173.
      if (isScreenAlreadyActive(frame.getChildAt(0), incomingKey)) {
        callback.onTraversalCompleted()
        return
      }
    }

    val outgoingView: View? = if (frame.childCount > 0) {
      frame.getChildAt(0)
    } else {
      null
    }

    val incomingContext = incomingContexts[incomingKey]!!

    val incomingView = inflateIncomingView(incomingContext, incomingKey, frame)
    throwIfIdIsMissing(incomingView, incomingKey)

    frame.addView(incomingView)
    incomingState.restore(incomingView)

    callback.onTraversalCompleted()

    incomingView.executeOnMeasure {
      animate(
          outgoingState?.getKey(),
          outgoingView,
          incomingKey,
          incomingView,
          direction,
          onCompleteListener = {
            if (outgoingView != null) {
              outgoingState?.save(outgoingView)
              frame.removeView(outgoingView)
            }
          }
      )
    }
  }

  open fun inflateIncomingView(incomingContext: Context, incomingKey: T, frame: ViewGroup): View {
    return LayoutInflater.from(incomingContext).inflate(layoutResForKey(incomingKey), frame, false)
  }

  private fun throwIfIdIsMissing(incomingView: View, incomingKey: T) {
    if (incomingView.id == View.NO_ID) {
      val layoutResName = incomingView.resources.getResourceName(layoutResForKey(incomingKey))
      throw AssertionError("Screen's layout ($layoutResName) doesn't have an ID set on its root ViewGroup. " +
          "An ID is required for persisting View state.")
    }
  }

  private fun isScreenAlreadyActive(view: View?, initialKey: Any?): Boolean {
    return view?.let {
      Flow.getKey<Any?>(view) == initialKey
    }
        ?: false
  }

  /**
   * Run a function when a View gets measured and laid out on the screen.
   * Duplicate of Views.kt in app module. Should use android-ktx in the future instead.
   */
  private fun View.executeOnMeasure(runnable: () -> Unit) {
    if (isInEditMode || isLaidOut) {
      runnable()
      return
    }

    viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
      override fun onPreDraw(): Boolean {
        if (isLaidOut) {
          viewTreeObserver.removeOnPreDrawListener(this)
          runnable()

        } else if (visibility == View.GONE) {
          Timber.w("View's visibility is set to Gone. It'll never be measured: " + resources.getResourceEntryName(id))
          viewTreeObserver.removeOnPreDrawListener(this)
        }
        return true
      }
    })
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

  abstract fun animate(
      outgoingKey: T?,
      outgoingView: View?,
      incomingKey: T,
      incomingView: View,
      direction: flow.Direction,
      onCompleteListener: () -> Unit
  )
}
