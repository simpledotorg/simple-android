package org.resolvetosavelives.red.newentry

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
          super.setOnFocusChangeListener({ _, hasFocus -> emitter.onNext(hasFocus) })
          emitter.onNext(hasFocus())
        }
        .replay(1)
        .refCount()
  }

  override fun setOnFocusChangeListener(listener: OnFocusChangeListener) {
    throw AssertionError("Use focusChanges instead")
  }
}
