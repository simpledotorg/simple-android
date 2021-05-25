package org.simple.clinic.widgets.ageanddateofbirth

import android.content.Context
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import br.com.sapereaude.maskedEditText.MaskedEditText
import io.reactivex.Observable
import org.simple.clinic.newentry.MultipleFocusChangeListeners
import timber.log.Timber
import java.lang.reflect.Field

class DateOfBirthEditText(
    context: Context,
    attrs: AttributeSet
) : MaskedEditText(context, attrs), MultipleFocusChangeListeners {

  companion object {
    val keepHintField: Field by lazy {
      val keepHintField = MaskedEditText::class.java.getDeclaredField("keepHint")
      keepHintField.isAccessible = true
      keepHintField
    }
  }

  override val focusChanges: Observable<Boolean>

  init {
    focusChanges = Observable
        .create<Boolean> { emitter ->
          emitter.setCancellable { super.setOnFocusChangeListener(null) }
          super.setOnFocusChangeListener { _, hasFocus -> emitter.onNext(hasFocus) }
          emitter.onNext(hasFocus())
        }
        .replay(1)
        .refCount()
  }

  override fun setText(text: CharSequence?, type: BufferType?) {
    // MaskedEditText has a bug where it ends up showing the hint as
    // the formatted text even when used inside a TextInputLayout. So
    // a hint of "Date of birth" shows up as "Da/te/ of". From my
    // limited research, MaskedEditText calls makeMaskedTextWithHint()
    // from init(), even when keepHint attribute is set to false.
    try {
      val keepHint = keepHintField.get(this) as Boolean
      if (keepHint.not() && text is Spannable) {
        // makeMaskedTextWithHint() creates a
        // ForegroundColorSpan with the current hint color.
        val spans = text.getSpans(0, text.length, ForegroundColorSpan::class.java)
        if (spans.isNotEmpty() && spans.first().foregroundColor == currentHintTextColor) {
          return
        }
      }
    } catch (e: Throwable) {
      Timber.e(e)
    }

    super.setText(text, type)
  }

  @Deprecated(message = "Cannot use setOnFocusChangeListener, use focusChanges instead")
  override fun setOnFocusChangeListener(listener: OnFocusChangeListener) {
    super.setOnFocusChangeListener(listener)
  }
}
