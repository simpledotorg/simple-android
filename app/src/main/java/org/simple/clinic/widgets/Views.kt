package org.simple.clinic.widgets

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.annotation.DimenRes
import android.support.annotation.DrawableRes
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
        Timber.w("View's visibility is set to Gone. It'll never be measured: %s", resources.getResourceEntryName(id))
        viewTreeObserver.removeOnPreDrawListener(this)
      }
      return true
    }
  })
}
