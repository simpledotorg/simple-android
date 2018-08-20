package org.simple.clinic.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.ViewFlipper

import org.simple.clinic.R

/** Exposes a way to set the displayed child in layout preview. */
class ViewFlipperWithDebugPreview(context: Context, attrs: AttributeSet) : ViewFlipper(context, attrs) {

  private var childToDisplayPostInflate: Int = 0

  init {
    if (isInEditMode) {
      val attributes = context.obtainStyledAttributes(attrs, R.styleable.ViewFlipperWithDebugPreview)
      childToDisplayPostInflate = attributes.getInt(R.styleable.ViewFlipperWithDebugPreview_debug_displayedChild, 0)
      attributes.recycle()
    }
  }

  override fun onFinishInflate() {
    super.onFinishInflate()

    if (isInEditMode) {
      if (childToDisplayPostInflate >= childCount) {
        throw IllegalStateException("displayed child index is greater than child count")
      }
      displayedChild = childToDisplayPostInflate
    }
  }
}
