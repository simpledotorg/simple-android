package org.simple.clinic.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.widget.LinearLayout

class LinearLayoutWithPreImeKeyEventListener(
    context: Context,
    attrs: AttributeSet
) : LinearLayout(context, attrs) {

  var backKeyPressInterceptor: (() -> Unit)? = null

  override fun dispatchKeyEventPreIme(event: KeyEvent): Boolean {
    return if (event.action == KeyEvent.ACTION_UP && event.keyCode == KeyEvent.KEYCODE_BACK) {
      backKeyPressInterceptor?.invoke()
      true
    } else {
      super.dispatchKeyEventPreIme(event)
    }
  }
}
