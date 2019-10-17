package org.simple.clinic.router.screen

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.annotation.LayoutRes
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
abstract class BaseViewGroupKeyChanger<T : Any>(private val keyChangeAnimator: KeyChangeAnimator<T>) : KeyChanger {

  override fun changeKey(
      outgoingState: State?,
      incomingState: State,
      direction: flow.Direction,
      incomingContexts: Map<Any, Context>,
      callback: TraversalCallback
  ) {
    if (!canHandleKey(incomingState.getKey())) {
      Timber.tag("Screen Router").i("Cannot handle key")
      return
    }

    val frame = screenLayoutContainer()
    val incomingKey = incomingState.getKey<T>()

    if (outgoingState == null && direction == flow.Direction.REPLACE) {
      // Short circuit if we would just be showing the same view again. Flow
      // intentionally calls changeKey() again on onResume() with the same values.
      // See: https://github.com/square/flow/issues/173.
      if (isScreenAlreadyActive(frame.getChildAt(0), incomingKey)) {
        Timber.tag("Screen Router").i("Same view [$incomingKey]; short circuit")
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

    Timber.tag("Screen Router").i("Add new view [$incomingKey]")
    frame.addView(incomingView)
    Timber.tag("Screen Router").i("Restore incoming view state [$incomingKey]")
    incomingState.restore(incomingView)

    outgoingView?.let {
      val outgoingKey = outgoingState?.getKey<T?>()
      Timber.tag("Screen Router").i("Save outgoing view state [$outgoingKey]")
      outgoingState?.save(outgoingView)
      Timber.tag("Screen Router").i("Remove outgoing view [$outgoingKey]")
      frame.removeView(outgoingView)
    }

    callback.onTraversalCompleted()
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
      @SuppressLint("BinaryOperationInTimber")
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

}
