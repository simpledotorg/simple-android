package org.simple.clinic.widgets.ageanddateofbirth

import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import io.reactivex.Observable
import org.simple.clinic.newentry.MultipleFocusChangeListeners

class DateOfBirthEditText(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs), MultipleFocusChangeListeners {

  override val focusChanges: Observable<Boolean>

  private var formatting = false

  init {
    inputType = InputType.TYPE_CLASS_NUMBER

    filters = arrayOf(
        InputFilter.LengthFilter(MAX_LENGTH)
    )

    addTextChangedListener(object : TextWatcher {

      override fun beforeTextChanged(
          s: CharSequence?,
          start: Int,
          count: Int,
          after: Int
      ) = Unit

      override fun onTextChanged(
          s: CharSequence?,
          start: Int,
          before: Int,
          count: Int
      ) {
        if (formatting || s == null) return

        val digits = s
            .toString()
            .filter(Char::isDigit)
            .take(MAX_DIGITS)

        val formatted = formatDate(digits)

        if (formatted == s.toString()) return

        val cursorDigitPosition = s
            .toString()
            .take(selectionStart.coerceAtLeast(0))
            .count(Char::isDigit)

        formatting = true

        try {
          setText(formatted)

          val newCursorPosition = cursorPositionForDigitIndex(
              formatted,
              cursorDigitPosition
          )

          setSelection(
              newCursorPosition.coerceIn(0, formatted.length)
          )
        } finally {
          formatting = false
        }
      }

      override fun afterTextChanged(s: Editable?) = Unit
    })

    focusChanges = Observable
        .create<Boolean> { emitter ->
          emitter.setCancellable {
            super.setOnFocusChangeListener(null)
          }

          super.setOnFocusChangeListener { _, hasFocus ->
            emitter.onNext(hasFocus)
          }

          emitter.onNext(hasFocus())
        }
        .replay(1)
        .refCount()
  }

  override fun setText(
      text: CharSequence?,
      type: BufferType?
  ) {
    formatting = true

    try {
      val formatted = text
          ?.toString()
          ?.filter(Char::isDigit)
          ?.let(::formatDate)
          .orEmpty()

      super.setText(formatted, type)
    } finally {
      formatting = false
    }
  }

  private fun formatDate(digits: String): String {
    return buildString {
      digits.forEachIndexed { index, digit ->
        if (index == 2 || index == 4) {
          append('/')
        }

        append(digit)
      }
    }
  }

  private fun cursorPositionForDigitIndex(
      formatted: String,
      digitPosition: Int
  ): Int {
    if (digitPosition <= 0) return 0

    var digitsSeen = 0

    formatted.forEachIndexed { index, character ->
      if (character.isDigit()) {
        digitsSeen++

        if (digitsSeen == digitPosition) {
          return index + 1
        }
      }
    }

    return formatted.length
  }

  companion object {
    private const val MAX_DIGITS = 8
    private const val MAX_LENGTH = 10
  }
}
