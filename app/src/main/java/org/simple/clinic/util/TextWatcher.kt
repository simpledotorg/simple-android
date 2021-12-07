package org.simple.clinic.util

import android.text.Editable
import android.text.TextWatcher

inline fun afterTextChangedWatcher(
    crossinline afterTextChanged: (text: Editable?) -> Unit
) = object : TextWatcher {
  override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
  }

  override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
  }

  override fun afterTextChanged(s: Editable?) {
    afterTextChanged(s)
  }
}
