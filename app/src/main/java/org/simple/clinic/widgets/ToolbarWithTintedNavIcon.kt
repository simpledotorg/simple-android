package org.simple.clinic.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.appcompat.widget.Toolbar
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.drawable.DrawableCompat
import org.simple.clinic.R

@SuppressLint("CustomViewStyleable") class ToolbarWithTintedNavIcon(context: Context, attrs: AttributeSet) : Toolbar(context, attrs) {

  @ColorInt
  private var navigationIconTint: Int = 0

  init {
    // Bright red so that nobody forgets to set the value.
    val defaultTint = Color.RED

    context.withStyledAttributes(attrs, R.styleable.ToolbarWithTintedNavIcon) {
      navigationIconTint = getColor(R.styleable.ToolbarWithTintedNavIcon_navigationIconTint, defaultTint)
    }

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
