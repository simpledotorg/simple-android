package org.resolvetosavelives.red.newentry.search

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import kotterknife.bindView
import org.resolvetosavelives.red.R
import org.resolvetosavelives.red.TheActivity
import org.resolvetosavelives.red.newentry.personal.PatientPersonalDetailsEntryScreen

class PatientSearchByMobileScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  companion object {
    val KEY = PatientSearchByMobileScreenKey()
  }

  private val mobileNumberEditText: EditText by bindView(R.id.patientsearch_mobile_number)
  private val newPatientButton: Button by bindView(R.id.patientsearch_new_patient)

  override fun onFinishInflate() {
    super.onFinishInflate()

    newPatientButton.setOnClickListener({

      val ongoingEntry = OngoingPatientEntry(null, mobileNumberEditText.text.toString())
      TheActivity.patientRepository()
          .save(ongoingEntry)
          .subscribe({
            TheActivity.screenRouter().push(PatientPersonalDetailsEntryScreen.KEY)
          })
    })
  }
}
