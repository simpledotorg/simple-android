package org.simple.clinic.login.phone

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import org.simple.clinic.TheActivity

class LoginPhoneScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    TheActivity.component.inject(this)
  }

  fun enableSubmitButton(enable: Boolean) {
  }

  fun openLoginPinScreen() {
  }
}
