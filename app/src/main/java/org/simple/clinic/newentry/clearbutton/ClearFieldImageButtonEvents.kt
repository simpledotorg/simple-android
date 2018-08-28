package org.simple.clinic.newentry.clearbutton

import org.simple.clinic.widgets.UiEvent

data class CleareableFieldTextChanged(val text: String, val fieldName: String = "") : UiEvent {

  override val analyticsName = if (fieldName.isNotBlank()) "Change Text Field:$fieldName" else ""
}

data class CleareableFieldFocusChanged(val hasFocus: Boolean, val fieldName: String = "") : UiEvent {

  override val analyticsName = when {
    fieldName.isNotBlank() && hasFocus -> "Focused Text Field:$fieldName"
    else -> ""
  }
}
