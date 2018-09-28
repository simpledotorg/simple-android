package org.simple.clinic.wip

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import android.support.annotation.ColorInt
import android.util.DisplayMetrics
import timber.log.Timber

/**
 * WIP.
 *
 * This drawable supports reading drawables from Xml selectors, making it compatible
 * with the standard way of applying different Drawable states. The current implementation of
 * [PrimarySolidButton] involves manually setting the enabled and disabled colors as
 * direct attributes on the button.
 */
class PrimarySolidButtonDrawable(delegate: StateListDrawable) : StateListDrawable() {

  /**
   * These insets are applied as extra padding around the button shape. They are set
   * in such a way that if we ever go back to platform buttons that have elevation,
   * the spacings in usages will not change.
   */
  private val insets = RectF(dpToPx(4), dpToPx(5), dpToPx(4), dpToPx(3))

  private val shapeBounds = RectF()
  private val cornerRadiusPx = dpToPx(2)
  private val backgroundColorPaint = Paint(Paint.ANTI_ALIAS_FLAG)

  init {
    val stateCountMethod = StateListDrawable::class.java.getDeclaredMethod("getStateCount")
    val count = stateCountMethod.invoke(delegate) as Int

    val getStateDrawableMethod = StateListDrawable::class.java.getDeclaredMethod("getStateDrawable", Int::class.java)
    val getStateSetMethod = StateListDrawable::class.java.getDeclaredMethod("getStateSet", Int::class.java)

    for (i in 0 until count) {
      val stateSet = getStateSetMethod.invoke(delegate, i) as IntArray
      val drawable = getStateDrawableMethod.invoke(delegate, i) as Drawable
      addState(stateSet, drawable)
    }
  }

  override fun onBoundsChange(bounds: Rect) {
    super.onBoundsChange(bounds)

    shapeBounds.set(
        bounds.left.toFloat() + insets.left,
        bounds.top.toFloat() + insets.top,
        bounds.right.toFloat() - insets.right,
        bounds.bottom.toFloat() - insets.bottom)
  }

  override fun draw(canvas: Canvas) {
    backgroundColorPaint.color = colorFrom(current)
    canvas.drawRoundRect(shapeBounds, cornerRadiusPx, cornerRadiusPx, backgroundColorPaint)
  }

  private fun dpToPx(dp: Int): Float {
    val metrics = Resources.getSystem().displayMetrics
    return dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
  }

  @ColorInt
  private fun colorFrom(drawable: Drawable?): Int {
    return when (drawable) {
      is ColorDrawable -> drawable.color

      is GradientDrawable -> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
          drawable.color.defaultColor
        } else {
          TODO("VERSION.SDK_INT < N")
          //val field = GradientDrawable::class.java.getDeclaredField("mGradientState")
          //field.isAccessible = true
          //val state = field.get(drawable)
        }
      }

      else -> {
        if (drawable == null) {
          Color.MAGENTA

        } else {
          //throw AssertionError("Non-solid backgrounds aren't supported: $drawable")
          Timber.w("Non-solid drawable: $drawable")
          Color.CYAN
        }
      }
    }
  }
}
