package org.simple.clinic.widgets

import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import io.github.inflationx.viewpump.InflateResult
import io.github.inflationx.viewpump.Interceptor

/**
 * Makes it easy to work with EditText on emulators by mapping the system
 * keyboard's enter key to the text field's ime option (ACTION_DONE, etc.).
 */
class ProxySystemKeyboardEnterToImeOption : Interceptor {

  private val enterToImeMapper: (View, Int, KeyEvent) -> Boolean = { view, _, event ->
    if (event.action == KeyEvent.ACTION_UP && event.keyCode == KeyEvent.KEYCODE_ENTER) {
      val editText = view as EditText
      editText.onEditorAction(editText.imeOptions)
      true

    } else {
      false
    }
  }

  override fun intercept(chain: Interceptor.Chain): InflateResult {
    val inflateResult = chain.proceed(chain.request())
    val inflatedView = inflateResult.view

    if (inflatedView is EditText) {
      inflatedView.setOnKeyListener(enterToImeMapper)
    }

    return inflateResult
  }
}
