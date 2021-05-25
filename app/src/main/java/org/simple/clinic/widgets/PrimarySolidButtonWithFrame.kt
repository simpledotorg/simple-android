package org.simple.clinic.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import org.simple.clinic.R

class PrimarySolidButtonWithFrame(
    context: Context,
    attributeSet: AttributeSet
) : FrameLayout(context, attributeSet) {

  val button by lazy { getChildAt(0) as Button }

  @ColorInt
  private var frameBackgroundEnabledResId: Int = 0

  @ColorInt
  private var frameBackgroundDisabledResId: Int = 0

  @ColorInt
  private var buttonBackgroundEnabledResId: Int = 0

  @ColorInt
  private var buttonBackgroundDisabledResId: Int = 0

  init {
    val attrs = context.obtainStyledAttributes(attributeSet, R.styleable.PrimarySolidButtonWithFrame)

    frameBackgroundEnabledResId = attrs.getColor(R.styleable.PrimarySolidButtonWithFrame_frameBackgroundEnabled, frameBackgroundEnabledResId)
    frameBackgroundDisabledResId = attrs.getColor(R.styleable.PrimarySolidButtonWithFrame_frameBackgroundDisabled, frameBackgroundDisabledResId)
    buttonBackgroundEnabledResId = attrs.getColor(R.styleable.PrimarySolidButtonWithFrame_buttonBackgroundEnabled, buttonBackgroundEnabledResId)
    buttonBackgroundDisabledResId = attrs.getColor(R.styleable.PrimarySolidButtonWithFrame_buttonBackgroundDisabled, buttonBackgroundDisabledResId)

    attrs.recycle()

    verifyColorsSet()
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    updateBackgrounds()
  }

  private fun verifyColorsSet() {
    if (frameBackgroundEnabledResId == 0 ||
        frameBackgroundDisabledResId == 0 ||
        buttonBackgroundEnabledResId == 0 ||
        buttonBackgroundDisabledResId == 0) {

      throw AssertionError("All color attributes must be set!")
    }
  }

  override fun setEnabled(enabled: Boolean) {
    super.setEnabled(enabled)
    updateBackgrounds()
  }

  private fun updateBackgrounds() {
    if (isEnabled.not()) {
      setBackgroundColor(frameBackgroundDisabledResId)
      button.setBackgroundColor(buttonBackgroundDisabledResId)
    } else {
      setBackgroundColor(frameBackgroundEnabledResId)
      button.setBackgroundColor(buttonBackgroundEnabledResId)
    }
  }
}
