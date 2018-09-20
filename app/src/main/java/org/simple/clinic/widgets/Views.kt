package org.simple.clinic.widgets

import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.annotation.DimenRes
import android.support.annotation.DrawableRes
import android.support.annotation.IdRes
import android.support.v4.content.ContextCompat
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import timber.log.Timber

fun EditText.showKeyboard() {
  post {
    this.requestFocus()
    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
  }
}

fun ViewGroup.hideKeyboard() {
  post {
    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
  }
}

fun EditText.setTextAndCursor(textToSet: CharSequence?) {
  setText(textToSet)

  // Cannot rely on textToSet. It's possible that the
  // EditText modifies the text using InputFilters.
  setSelection(text.length)
}

fun View.setPadding(@DimenRes paddingRes: Int) {
  val padding = resources.getDimensionPixelSize(paddingRes)
  setPaddingRelative(padding, padding, padding, padding)
}

fun View.setTopMargin(@DimenRes topMarginRes: Int) {
  val marginLayoutParams = layoutParams as ViewGroup.MarginLayoutParams
  marginLayoutParams.topMargin = resources.getDimensionPixelSize(topMarginRes)
  layoutParams = marginLayoutParams
}

fun TextView.setCompoundDrawableStart(@DrawableRes drawableRes: Int) {
  val drawable = ContextCompat.getDrawable(context, drawableRes)
  setCompoundDrawablesRelativeWithIntrinsicBounds(
      drawable,
      compoundDrawablesRelative[1],
      compoundDrawablesRelative[2],
      compoundDrawablesRelative[3])
}

fun TextView.setCompoundDrawableStart(drawable: Drawable?) {
  setCompoundDrawablesRelativeWithIntrinsicBounds(
      drawable,
      compoundDrawablesRelative[1],
      compoundDrawablesRelative[2],
      compoundDrawablesRelative[3])
}

/**
 * Run a function when a View gets measured and laid out on the screen.
 */
fun View.executeOnNextMeasure(runnable: () -> Unit) {
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
        Timber.w("View's visibility is set to Gone. It'll never be measured: %s", resourceName())
        viewTreeObserver.removeOnPreDrawListener(this)
      }
      return true
    }
  })
}

/**
 * Like [View.getTop], but works even when a View is not the immediate child of [superParent].
 */
fun View.topRelativeTo(superParent: ViewGroup): Int {
  var totalDistance = 0
  var nextView = this

  while (true) {
    totalDistance += nextView.top
    if (nextView.parent == superParent) {
      break
    }
    nextView = nextView.parent as View

    if ((nextView.parent as ViewGroup).id == android.R.id.content) {
      throw AssertionError("${resources.getResourceEntryName(superParent.id)} isn't the parent of ${resources.getResourceEntryName(id)}")
    }
  }

  return totalDistance
}

@Suppress("FoldInitializerAndIfToElvis")
fun ViewGroup.indexOfChildId(@IdRes childId: Int): Int {
  val child = findViewById<View>(childId)
  if (child == null) {
    throw AssertionError("${resources.getResourceEntryName(childId)} isn't a part of ${resources.getResourceEntryName(id)}")
  }
  return indexOfChild(child)
}

fun View.resourceName() = resourceNameForId(resources, id)

fun resourceNameForId(resources: Resources, @IdRes id: Int): String {
  var name = "<nameless>"
  try {
    name = resources.getResourceEntryName(id)
  } catch (e: Resources.NotFoundException) {
    // Nothing to see here
  }

  return name
}

fun View.locationRectOnScreen(): Rect {
  val location = IntArray(2)
  getLocationOnScreen(location)

  val left = location[0]
  val top = location[1]
  return Rect(left, top, left + width, top + height)
}

val View.marginLayoutParams: ViewGroup.MarginLayoutParams
  get() = layoutParams as ViewGroup.MarginLayoutParams