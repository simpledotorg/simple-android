package org.resolvetosavelives.red.newentry.mobile

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.screen_patient_mobile_entry.view.*

class PatientMobileEntryScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  companion object {
    val KEY = PatientMobileEntryScreenKey()
  }

  override fun onFinishInflate() {
    super.onFinishInflate()

    newPatientButton.setOnClickListener({
      // TODO: Open new patient screen.
    })
  }
}
