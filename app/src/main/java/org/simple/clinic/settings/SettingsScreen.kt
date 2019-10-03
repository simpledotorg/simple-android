package org.simple.clinic.settings

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.screen_settings.view.*

class SettingsScreen(
    context: Context,
    attributeSet: AttributeSet
) : LinearLayout(context, attributeSet), SettingsUi {

  override fun displayUserDetails(name: String, phoneNumber: String) {
    userName.text = name
    userNumber.text = phoneNumber
  }
}
