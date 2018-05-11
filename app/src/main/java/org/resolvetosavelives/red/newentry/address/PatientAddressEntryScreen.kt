package org.resolvetosavelives.red.newentry.address

import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import kotterknife.bindView
import org.resolvetosavelives.red.R
import org.resolvetosavelives.red.TheActivity
import org.resolvetosavelives.red.newentry.mobile.PatientMobileEntryScreen
import org.resolvetosavelives.red.widgets.showKeyboard

class PatientAddressEntryScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  private val streetEditText by bindView<EditText>(R.id.patiententry_address_street)
  private val nextButton by bindView<TextView>(R.id.patientaddress_next_button)

  companion object {
    val KEY = PatientAddressEntryScreenKey()
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    streetEditText.showKeyboard()

    nextButton.setOnClickListener({
      TheActivity.screenRouter().push(PatientMobileEntryScreen.KEY)
    })
  }
}

