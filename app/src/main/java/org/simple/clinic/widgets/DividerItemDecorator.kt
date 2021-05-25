package org.simple.clinic.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.res.use
import androidx.recyclerview.widget.RecyclerView
import timber.log.Timber
import kotlin.math.roundToInt

private val ATTRS = intArrayOf(android.R.attr.listDivider)

class DividerItemDecorator(
    val context: Context,
    val marginStart: Int,
    val marginEnd: Int,
) : RecyclerView.ItemDecoration() {

  private val mBounds = Rect()

  private var divider: Drawable? = null

  init {
    context.obtainStyledAttributes(ATTRS).use {
      divider = it.getDrawable(0)
    }
    if (divider == null) {
      Timber.w("@android:attr/listDivider was not set in the theme used")
    }
  }

  override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
    if (parent.layoutManager == null || divider == null) {
      return
    }
    drawDividers(c, parent)
  }

  private fun drawDividers(canvas: Canvas, parent: RecyclerView) {
    canvas.save()
    val left = marginStart
    val right = parent.width - marginEnd

    val childCount = parent.childCount
    for (i in 0 until childCount - 1) {
      val child = parent.getChildAt(i)
      parent.getDecoratedBoundsWithMargins(child, mBounds)
      val bottom: Int = mBounds.bottom + child.translationY.roundToInt()
      val top: Int = bottom - divider!!.intrinsicHeight
      divider!!.setBounds(left, top, right, bottom)
      divider!!.draw(canvas)
    }
    canvas.restore()
  }

  override fun getItemOffsets(
      outRect: Rect,
      view: View,
      parent: RecyclerView,
      state: RecyclerView.State
  ) {
    if (divider == null) {
      outRect[0, 0, 0] = 0
      return
    }
    outRect[0, 0, 0] = divider!!.intrinsicHeight
  }
}
