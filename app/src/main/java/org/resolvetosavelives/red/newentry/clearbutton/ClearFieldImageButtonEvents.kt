package org.resolvetosavelives.red.newentry.clearbutton

import org.resolvetosavelives.red.widgets.UiEvent

data class CleareableFieldTextChanged(val text: String) : UiEvent

data class CleareableFieldFocusChanged(val hasFocus: Boolean) : UiEvent
