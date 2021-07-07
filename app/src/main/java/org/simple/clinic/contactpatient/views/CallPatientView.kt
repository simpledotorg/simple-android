package org.simple.clinic.contactpatient.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import org.simple.clinic.R
import org.simple.clinic.databinding.ContactpatientCallpatientBinding
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

  private var binding: ContactpatientCallpatientBinding? = null

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

  private val patientAddressTextView
    get() = binding!!.patientAddressTextView

  private val registeredFacilityTextView
    get() = binding!!.registeredFacilityTextView

  private val diagnosisTextView
    get() = binding!!.diagnosisTextView

  private val lastVisitedTextView
    get() = binding!!.lastVisitedTextView

  private val patientWithPhoneNumberGroup
    get() = binding!!.patientWithPhoneNumberGroup

  private val patientWithoutPhoneNumberGroup
    get() = binding!!.patientWithNoPhoneNumberGroup

  private val resultOfCallLabelTextView
    get() = binding!!.resultOfCallLabel

  var secureCallingSectionVisible: Boolean = false
    set(value) {
      field = value
      secureCallingGroup.visibility = if (field) View.VISIBLE else View.GONE
    }

  var showPatientWithPhoneNumberLayout : Boolean = false
  set(value){
    field = value
    patientWithPhoneNumberGroup.visibility = if(field) View.VISIBLE else View.GONE
  }

  var showPatientWithNoPhoneNumberLayout : Boolean = false
    set(value){
      field = value
      patientWithoutPhoneNumberGroup.visibility = if(field) View.VISIBLE else View.GONE
    }

  var agreedToVisitClicked: AgreedToVisitClicked? = null

  var remindToCallLaterClicked: RemindToCallLaterClicked? = null

  var removeFromOverdueListClicked: RemoveFromOverdueListClicked? = { Toast.makeText(context, "WIP", Toast.LENGTH_SHORT).show() }

  var normalCallButtonClicked: NormalCallButtonClicked? = null

  var secureCallButtonClicked: SecureCallButtonClicked? = null

  override fun onFinishInflate() {
    super.onFinishInflate()

    val layoutInflater = LayoutInflater.from(context)
    binding = ContactpatientCallpatientBinding.inflate(layoutInflater, this)

    agreedToVisitTextView.setOnClickListener { agreedToVisitClicked?.invoke() }
    remindToCallLaterTextView.setOnClickListener { remindToCallLaterClicked?.invoke() }
    removeFromOverdueListTextView.setOnClickListener { removeFromOverdueListClicked?.invoke() }
    normalCallButton.setOnClickListener { normalCallButtonClicked?.invoke() }
    secureCallButton.setOnClickListener { secureCallButtonClicked?.invoke() }
  }

  override fun onDetachedFromWindow() {
    binding = null
    super.onDetachedFromWindow()
  }

  fun renderPatientDetails(
      name: String,
      gender: Gender,
      age: Int,
      phoneNumber: String,
      patientAddress: String,
      registeredFacility: String,
      diagnosis: String,
      lastVisited: String,
      resultOfCallLabel: String
  ) {
    val genderLetter = resources.getString(gender.displayLetterRes)
    nameTextView.text = resources.getString(R.string.contactpatient_patientdetails, name, genderLetter, age.toString())
    phoneNumberTextView.text = phoneNumber
    patientAddressTextView.text = patientAddress
    registeredFacilityTextView.text = registeredFacility
    diagnosisTextView.text = diagnosis
    lastVisitedTextView.text = lastVisited
    resultOfCallLabelTextView.text = resultOfCallLabel
  }
}
