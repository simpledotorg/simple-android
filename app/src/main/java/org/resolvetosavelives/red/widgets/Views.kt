package org.resolvetosavelives.red.widgets

import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

fun EditText.showKeyboard() {
  post({
    this.requestFocus()
    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
  })
}

fun EditText.setTextAndCursor(textToSet: CharSequence?) {
  setText(textToSet)

  // Cannot rely on textToSet. It's possible that the
  // EditText modifies the text using InputFilters.
  setSelection(text.length)
}
