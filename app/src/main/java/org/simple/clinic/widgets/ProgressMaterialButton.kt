package org.simple.clinic.widgets

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.google.android.material.button.MaterialButton
import org.simple.clinic.R

class ProgressMaterialButton(
    context: Context,
    attrs: AttributeSet? = null
) : MaterialButton(context, attrs) {

  enum class ButtonState {
    InProgress, Enabled, Disabled
  }

  private var buttonState: ButtonState
  private var buttonText: String? = null
  private var buttonIcon: Drawable? = null
  private var buttonIconGravity: Int

  private var progressDrawable = AnimatedVectorDrawableCompat.create(context, R.drawable.avd_progress)

  private val progressAvdCallback = object : Animatable2Compat.AnimationCallback() {
    override fun onAnimationEnd(drawable: Drawable?) {
      super.onAnimationEnd(drawable)
      progressDrawable?.start()
    }
  }

  init {
    buttonText = text.toString()
    buttonIcon = icon
    buttonIconGravity = iconGravity

    val typeArray = context.obtainStyledAttributes(attrs, R.styleable.ProgressMaterialButton)
    buttonState = ButtonState.values()[typeArray.getInt(R.styleable.ProgressMaterialButton_buttonState, 0)]

    setButtonState(buttonState)

    typeArray.recycle()
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    progressDrawable?.run {
      registerAnimationCallback(progressAvdCallback)
      start()
    }
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    progressDrawable?.unregisterAnimationCallback(progressAvdCallback)
  }

  fun setButtonState(buttonState: ButtonState) {
    when (buttonState) {
      ButtonState.InProgress -> {
        isEnabled = true
        isClickable = false
        icon = progressDrawable
        text = null
        iconGravity = ICON_GRAVITY_TEXT_START
      }
      ButtonState.Enabled -> {
        isEnabled = true
        isClickable = true
        icon = buttonIcon
        text = buttonText
        iconGravity = buttonIconGravity
      }
      ButtonState.Disabled -> {
        isEnabled = false
        icon = buttonIcon
        text = buttonText
        iconGravity = buttonIconGravity
      }
    }
    this.buttonState = buttonState
  }
}
