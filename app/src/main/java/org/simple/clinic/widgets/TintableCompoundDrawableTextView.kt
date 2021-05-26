package org.simple.clinic.widgets

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.drawable.DrawableCompat

/**
 * A Button which tints its compound drawable with the same color as its text.
 */
class TintableCompoundDrawableTextView : AppCompatTextView {

  constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    init()
  }

  constructor(
      context: Context,
      attrs: AttributeSet,
      defStyleAttr: Int
  ) : super(context, attrs, defStyleAttr) {
    init()
  }

  private fun init() {
    applyColorTintToCompoundDrawables(currentTextColor)
  }

  override fun setCompoundDrawablesRelativeWithIntrinsicBounds(
      start: Drawable?,
      top: Drawable?,
      end: Drawable?,
      bottom: Drawable?
  ) {
    super.setCompoundDrawablesRelativeWithIntrinsicBounds(start, top, end, bottom)
    applyColorTintToCompoundDrawables(currentTextColor)
  }

  override fun setTextColor(color: Int) {
    super.setTextColor(color)
    applyColorTintToCompoundDrawables(color)
  }

  /**
   * Applies color tint to all compound drawables set on Views extending TextView (eg., Button(s)).
   */
  private fun applyColorTintToCompoundDrawables(@ColorInt tintColor: Int) {
    val drawables: Array<Drawable> = compoundDrawablesRelative

    for (i in drawables.indices) {
      @Suppress("SENSELESS_COMPARISON")
      if (drawables[i] != null) {
        // Wrap the drawable so that future tinting calls work on pre-v21 devices. Always use the returned drawable.
        drawables[i] = drawables[i].mutate()
        DrawableCompat.setTint(drawables[i], tintColor)
      }
    }
    super.setCompoundDrawablesRelativeWithIntrinsicBounds(drawables[0], drawables[1], drawables[2], drawables[3])
  }
}
