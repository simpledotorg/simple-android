package org.simple.clinic.util

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup

fun View.locationRectOnScreen(): Rect {
  val location = IntArray(2)
  getLocationOnScreen(location)

  val left = location[0]
  val top = location[1]
  return Rect(left, top, left + width, top + height)
}

val View.marginLayoutParams: ViewGroup.MarginLayoutParams
  get() = layoutParams as ViewGroup.MarginLayoutParams
