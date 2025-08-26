package org.simple.clinic.widgets

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.core.content.withStyledAttributes
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

  private lateinit var buttonState: ButtonState
  private var buttonText: String? = null
  private var buttonIcon: Drawable? = null
  private var buttonIconGravity: Int

  private val progressDrawable: AnimatedVectorDrawableCompat = AnimatedVectorDrawableCompat.create(context, R.drawable.avd_progress)!!

  init {
    buttonText = text.toString()
    buttonIcon = icon
    buttonIconGravity = iconGravity

    context.withStyledAttributes(attrs, R.styleable.ProgressMaterialButton) {
      buttonState = ButtonState.entries.toTypedArray()[getInt(R.styleable.ProgressMaterialButton_buttonState, 0)]

      setButtonState(buttonState)

    }
  }

  fun setButtonState(buttonState: ButtonState) {
    when (buttonState) {
      ButtonState.InProgress -> {
        isEnabled = true
        isClickable = false
        icon = progressDrawable
        text = null
        iconGravity = ICON_GRAVITY_TEXT_START

        progressDrawable.start()
      }

      ButtonState.Enabled -> {
        isEnabled = true
        isClickable = true
        icon = buttonIcon
        text = buttonText
        iconGravity = buttonIconGravity

        progressDrawable.stop()
      }

      ButtonState.Disabled -> {
        isEnabled = false
        icon = buttonIcon
        text = buttonText
        iconGravity = buttonIconGravity

        progressDrawable.stop()
      }
    }
    this.buttonState = buttonState
  }
}
