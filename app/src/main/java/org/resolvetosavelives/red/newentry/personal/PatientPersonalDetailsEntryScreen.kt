package org.resolvetosavelives.red.newentry.personal

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import android.widget.RelativeLayout
import kotterknife.bindView
import org.resolvetosavelives.red.R
import org.resolvetosavelives.red.TheActivity
import org.resolvetosavelives.red.newentry.address.PatientAddressEntryScreen

class PatientPersonalDetailsEntryScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  companion object {
    val KEY = PatientPersonalDetailsScreenKey()
  }

  private val nextButton: Button by bindView(R.id.patientpersonaldetails_nextButton)

  override fun onFinishInflate() {
    super.onFinishInflate()

    nextButton.setOnClickListener({
      TheActivity.screenRouter().push(PatientAddressEntryScreen.KEY)
    })
  }
}
