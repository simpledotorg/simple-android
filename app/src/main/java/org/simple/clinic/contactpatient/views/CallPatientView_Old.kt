package org.simple.clinic.contactpatient.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import org.simple.clinic.R
import org.simple.clinic.databinding.ContactpatientCallpatientOldBinding
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.displayLetterRes

private typealias AgreedToVisitClicked_Old = () -> Unit
private typealias RemindToCallLaterClicked_Old = () -> Unit
private typealias RemoveFromOverdueListClicked_Old = () -> Unit
private typealias NormalCallButtonClicked_Old = () -> Unit
private typealias SecureCallButtonClicked_Old = () -> Unit

class CallPatientView_Old(
    context: Context,
    attributeSet: AttributeSet
) : ConstraintLayout(context, attributeSet) {

  private var binding: ContactpatientCallpatientOldBinding? = null

  private val callResultsGroup
    get() = binding!!.callResultsGroup

  private val secureCallingGroup
    get() = binding!!.secureCallingGroup

  private val agreedToVisitTextView
    get() = binding!!.agreedToVisitTextView

  private val remindToCallLaterTextView
    get() = binding!!.remindToCallLaterTextView

  private val removeFromOverdueListTextView
    get() = binding!!.removeFromOverdueListTextView

  private val normalCallButton
    get() = binding!!.normalCallButton

  private val secureCallButton
    get() = binding!!.secureCallButton

  private val nameTextView
    get() = binding!!.nameTextView

  private val phoneNumberTextView
    get() = binding!!.phoneNumberTextView

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

  var overdueListChangesEnabled: Boolean = false
  set(value) {
    field = value
  }

  var agreedToVisitClickedOld: AgreedToVisitClicked_Old? = null

  var remindToCallLaterClickedOld: RemindToCallLaterClicked_Old? = null

  var removeFromOverdueListClickedOld: RemoveFromOverdueListClicked_Old? = { Toast.makeText(context, "WIP", Toast.LENGTH_SHORT).show() }

  var normalCallButtonClickedOld: NormalCallButtonClicked_Old? = null

  var secureCallButtonClickedOld: SecureCallButtonClicked_Old? = null

  override fun onFinishInflate() {
    super.onFinishInflate()

    val layoutInflater = LayoutInflater.from(context)
    binding = ContactpatientCallpatientOldBinding.inflate(layoutInflater, this)

    agreedToVisitTextView.setOnClickListener { agreedToVisitClickedOld?.invoke() }
    remindToCallLaterTextView.setOnClickListener { remindToCallLaterClickedOld?.invoke() }
    removeFromOverdueListTextView.setOnClickListener { removeFromOverdueListClickedOld?.invoke() }
    normalCallButton.setOnClickListener { normalCallButtonClickedOld?.invoke() }
    secureCallButton.setOnClickListener { secureCallButtonClickedOld?.invoke() }
  }

  override fun onDetachedFromWindow() {
    binding = null
    super.onDetachedFromWindow()
  }

  fun renderPatientDetails(name: String, gender: Gender, age: Int, phoneNumber: String) {
    val genderLetter = resources.getString(gender.displayLetterRes)
    nameTextView.text = resources.getString(R.string.contactpatient_patientdetails, name, genderLetter, age.toString())
    phoneNumberTextView.text = phoneNumber
  }
}
