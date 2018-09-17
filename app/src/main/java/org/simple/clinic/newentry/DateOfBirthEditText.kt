package org.simple.clinic.newentry

import android.content.Context
import android.util.AttributeSet
import br.com.sapereaude.maskedEditText.MaskedEditText
import io.reactivex.Observable

class DateOfBirthEditText(context: Context, attrs: AttributeSet) : MaskedEditText(context, attrs), MultipleFocusChangeListeners {

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

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    // MaskedEditText has a bug where it ends up showing the
    // hint as the formatted text even when used inside a
    // TextInputLayout. So a hint of "Date of birth" shows up
    // as "Da/te/ of". This seems to fix it.
    text = text
  }

  override fun setOnFocusChangeListener(listener: OnFocusChangeListener) {
    throw AssertionError("Use focusChanges instead")
  }
}
