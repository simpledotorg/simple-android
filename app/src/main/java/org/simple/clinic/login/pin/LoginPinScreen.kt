package org.simple.clinic.login.pin

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import org.simple.clinic.TheActivity

class LoginPinScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  override fun onFinishInflate() {
    super.onFinishInflate()
    if(isInEditMode) {
      return
    }

    TheActivity.component.inject(this)
  }

  fun showPhoneNumber(phoneNumber: String) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  fun enableSubmitButton(state: Boolean) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  fun showProgressBar() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  fun hideProgressBar() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  fun showNetworkError() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  fun showServerError() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  fun showUnexpectedError() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  fun openHomeScreen() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

}
