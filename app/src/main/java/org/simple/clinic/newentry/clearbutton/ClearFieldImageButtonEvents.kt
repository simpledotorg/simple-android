package org.simple.clinic.newentry.clearbutton

import org.simple.clinic.widgets.UiEvent

data class CleareableFieldTextChanged(val text: String) : UiEvent

data class CleareableFieldFocusChanged(val hasFocus: Boolean) : UiEvent
