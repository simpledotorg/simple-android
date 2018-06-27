package org.simple.clinic.deeplink

import android.app.Activity
import android.os.Bundle
import org.simple.clinic.TheActivity
import org.simple.clinic.login.phone.LoginPhoneScreen

class DeepLinkActivity : Activity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val otp = intent.data.getQueryParameter("otp")!!
    startActivity(TheActivity.intentWithInitialScreen(this, LoginPhoneScreen.KEY(otp)))

    finish()
  }
}
