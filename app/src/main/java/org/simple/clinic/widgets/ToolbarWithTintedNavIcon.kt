package org.simple.clinic.widgets

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.drawable.DrawableCompat
import org.simple.clinic.R

class ToolbarWithTintedNavIcon(context: Context, attrs: AttributeSet) : Toolbar(context, attrs) {

  @ColorInt
  private val navigationIconTint: Int

  init {
    // Bright red so that nobody forgets to set the value.
    val defaultTint = Color.RED

    val attributes = context.obtainStyledAttributes(attrs, R.styleable.ToolbarWithTintedNavIcon)
    navigationIconTint = attributes.getColor(R.styleable.ToolbarWithTintedNavIcon_navigationIconTint, defaultTint)
    attributes.recycle()

    val icon = navigationIcon
    if (icon != null) {
      navigationIcon = tint(icon)
    }
  }

  override fun setNavigationIcon(icon: Drawable?) {
    super.setNavigationIcon(if (icon == null) null else tint(icon))
  }

  private fun tint(icon: Drawable): Drawable {
    val tintedIcon = icon.mutate()
    DrawableCompat.setTint(tintedIcon, navigationIconTint)
    return tintedIcon
  }
}
