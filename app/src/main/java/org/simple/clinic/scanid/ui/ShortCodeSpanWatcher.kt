package org.simple.clinic.scanid.ui

import android.text.Editable
import android.text.Spannable
import org.simple.clinic.text.style.TrailingSpaceForCharacterSpan
import org.simple.clinic.util.TextWatcherAdapter

private const val CHARS_REQUIRED_TO_INSERT_SPACE = 4
private const val INSERT_SPACE_AT_INDEX = 2

class ShortCodeSpanWatcher : TextWatcherAdapter() {
  private val trailingSpaceSpan = TrailingSpaceForCharacterSpan()

  override fun afterTextChanged(editable: Editable) {
    with(editable) {
      removeSpan(trailingSpaceSpan)

      if (length >= CHARS_REQUIRED_TO_INSERT_SPACE) {
        setSpan(trailingSpaceSpan, INSERT_SPACE_AT_INDEX, INSERT_SPACE_AT_INDEX + 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
      }
    }
  }
}
