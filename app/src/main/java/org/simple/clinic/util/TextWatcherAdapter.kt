package org.simple.clinic.util

import android.text.Editable
import android.text.TextWatcher

abstract class TextWatcherAdapter : TextWatcher {
  override fun afterTextChanged(editable: Editable) {
    // No-op
  }

  override fun beforeTextChanged(charSequence: CharSequence, start: Int, count: Int, after: Int) {
    // No-op
  }

  override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
    // No-op
  }
}
