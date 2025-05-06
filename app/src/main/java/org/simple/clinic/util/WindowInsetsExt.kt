package org.simple.clinic.util

import android.view.View
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.Type
import androidx.core.view.WindowInsetsCompat.Type.InsetsType
import androidx.core.view.updatePadding
import com.google.android.material.appbar.AppBarLayout

data class InsetsInitialPadding(
    val start: Int,
    val top: Int,
    val end: Int,
    val bottom: Int,
)

fun View.applyStatusBarPadding() {
  updateInsetPadding(typeMask = Type.statusBars()) { view, insets, initialPadding ->
    view.updatePadding(top = initialPadding.top + insets.top)
  }
}

fun View.applyInsetsBottomPadding() {
  updateInsetPadding(typeMask = Type.navigationBars() or Type.ime()) { view, insets, initialPadding ->
    view.updatePadding(bottom = initialPadding.bottom + insets.bottom)
  }
}

fun View.updateInsetPadding(
    @InsetsType typeMask: Int,
    block: (View, Insets, InsetsInitialPadding) -> Unit
) {
  val initialPadding = InsetsInitialPadding(
      start = paddingStart,
      top = paddingTop,
      end = paddingEnd,
      bottom = paddingBottom,
  )

  setOnApplyWindowInsetsListener { view, systemInsets ->
    val insetsCompat = WindowInsetsCompat.toWindowInsetsCompat(systemInsets)
    val insets = insetsCompat.getInsets(typeMask)
    block(view, insets, initialPadding)

    systemInsets
  }

  requestApplyInsetsWhenAttached()
}

fun View.requestApplyInsetsWhenAttached() {
  if (isAttachedToWindow) {
    requestApplyInsets()
  } else {
    addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
      override fun onViewAttachedToWindow(v: View) {
        v.removeOnAttachStateChangeListener(this)
        v.requestApplyInsets()
      }

      override fun onViewDetachedFromWindow(v: View) = Unit
    })
  }
}
