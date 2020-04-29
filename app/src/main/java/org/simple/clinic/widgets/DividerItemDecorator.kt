package org.simple.clinic.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.simple.clinic.R
import kotlin.math.roundToInt

class DividerItemDecorator(
    val context: Context,
    val marginStart: Int,
    val marginEnd: Int,
    @DrawableRes val dividerDrawable: Int = R.drawable.divider
) : RecyclerView.ItemDecoration() {

  private val mBounds = Rect()

  private var divider: Drawable? = null

  init {
    divider = ContextCompat.getDrawable(context, dividerDrawable)
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

  override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
    if (divider == null) {
      outRect[0, 0, 0] = 0
      return
    }
    outRect[0, 0, 0] = divider!!.intrinsicHeight
  }
}
