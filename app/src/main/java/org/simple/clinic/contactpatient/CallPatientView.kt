package org.simple.clinic.contactpatient

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.patientcontact_view_callpatient.view.*
import org.simple.clinic.R
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.displayLetterRes

private typealias AgreedToVisitClicked = () -> Unit
private typealias RemindToCallLaterClicked = () -> Unit
private typealias RemoveFromOverdueListClicked = () -> Unit
private typealias NormalCallButtonClicked = () -> Unit
private typealias SecureCallButtonClicked = () -> Unit

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

  var agreedToVisitClicked: AgreedToVisitClicked? = { Toast.makeText(context, "WIP", Toast.LENGTH_SHORT).show() }

  var remindToCallLaterClicked: RemindToCallLaterClicked? = { Toast.makeText(context, "WIP", Toast.LENGTH_SHORT).show() }

  var removeFromOverdueListClicked: RemoveFromOverdueListClicked? = { Toast.makeText(context, "WIP", Toast.LENGTH_SHORT).show() }

  var normalCallButtonClicked: NormalCallButtonClicked? = null

  var secureCallButtonClicked: SecureCallButtonClicked? = null

  override fun onFinishInflate() {
    super.onFinishInflate()

    View.inflate(context, R.layout.patientcontact_view_callpatient, this)

    agreedToVisitTextView.setOnClickListener { agreedToVisitClicked?.invoke() }
    remindToCallLaterTextView.setOnClickListener { remindToCallLaterClicked?.invoke() }
    removeFromOverdueListTextView.setOnClickListener { removeFromOverdueListClicked?.invoke() }
    normalCallButton.setOnClickListener { normalCallButtonClicked?.invoke() }
    secureCallButton.setOnClickListener { secureCallButtonClicked?.invoke() }
  }

  fun renderPatientDetails(name: String, gender: Gender, age: Int, phoneNumber: String) {
    val genderLetter = resources.getString(gender.displayLetterRes)
    nameTextView.text = resources.getString(R.string.contactpatient_patientdetails, name, genderLetter, age.toString())
    phoneNumberTextView.text = phoneNumber
  }
}
