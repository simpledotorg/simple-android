package org.simple.clinic.bp.entry

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.widget.LinearLayout

class LinearLayoutWithPreImeKeyEventListener(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

  lateinit var backKeyPressInterceptor: () -> Unit

  override fun dispatchKeyEventPreIme(event: KeyEvent): Boolean {
    return if (event.action == KeyEvent.ACTION_UP && event.keyCode == KeyEvent.KEYCODE_BACK) {
      backKeyPressInterceptor()
      true
    } else {
      super.dispatchKeyEventPreIme(event)
    }
  }
}
