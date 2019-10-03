package org.simple.clinic.settings

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout

class SettingsScreen(
    context: Context,
    attributeSet: AttributeSet
) : LinearLayout(context, attributeSet), SettingsUi {

  override fun displayUserDetails(name: String, phoneNumber: String) {
    TODO("not implemented")
  }
}
