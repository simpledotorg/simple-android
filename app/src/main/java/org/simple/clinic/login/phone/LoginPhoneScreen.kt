package org.simple.clinic.login.phone

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import org.simple.clinic.TheActivity

class LoginPhoneScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  companion object {
    val KEY: (otp: String) -> LoginPhoneScreenKey = ::LoginPhoneScreenKey
  }

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
