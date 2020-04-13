package org.simple.clinic.patientcontact

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.patientcontact_view_callpatient.view.*
import org.simple.clinic.R
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.displayLetterRes

class CallPatientView(
    context: Context,
    attributeSet: AttributeSet
) : ConstraintLayout(context, attributeSet) {

  var callResultSectionVisible: Boolean = false
    set(value) {
      field = value
      callResultsGroup.visibility = if (field) View.VISIBLE else View.GONE
    }

  var secureCallingSectionVisible: Boolean = false
    set(value) {
      field = value
      secureCallingGroup.visibility = if (field) View.VISIBLE else View.GONE
    }

  override fun onFinishInflate() {
    super.onFinishInflate()

    View.inflate(context, R.layout.patientcontact_view_callpatient, this)
  }

  fun renderPatientDetails(name: String, gender: Gender, age: Int, phoneNumber: String) {
    val genderLetter = resources.getString(gender.displayLetterRes)
    nameTextView.text = resources.getString(R.string.patientcontact_patientdetails, name, genderLetter, age.toString())
    phoneNumberTextView.text = phoneNumber
  }
}
