package org.resolvetosavelives.red.newentry.mobile

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.screen_patient_mobile_entry.view.*
import org.resolvetosavelives.red.R.id.newPatientButton
import org.resolvetosavelives.red.TheActivity
import org.resolvetosavelives.red.newentry.personal.PatientPersonalDetailsEntryScreen

class PatientMobileEntryScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  companion object {
    val KEY = PatientMobileEntryScreenKey()
  }

  override fun onFinishInflate() {
    super.onFinishInflate()

    newPatientButton.setOnClickListener({
      TheActivity.screenRouter().push(PatientPersonalDetailsEntryScreen.KEY)
    })
  }
}
