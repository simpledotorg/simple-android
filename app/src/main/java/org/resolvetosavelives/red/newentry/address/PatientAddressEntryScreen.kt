package org.resolvetosavelives.red.newentry.address

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import android.widget.TextView
import kotterknife.bindView
import org.resolvetosavelives.red.R
import org.resolvetosavelives.red.TheActivity
import org.resolvetosavelives.red.newentry.mobile.PatientMobileEntryScreen

class PatientAddressEntryScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  private val nextButton: TextView by bindView(R.id.patientaddress_next_button)

  companion object {
    val KEY = PatientAddressEntryScreenKey()
  }

  override fun onFinishInflate() {
    super.onFinishInflate()

    nextButton.setOnClickListener({
      TheActivity.screenRouter().push(PatientMobileEntryScreen.KEY)
    })
  }
}
