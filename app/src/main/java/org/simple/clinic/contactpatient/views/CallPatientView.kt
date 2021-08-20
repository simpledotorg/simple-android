package org.simple.clinic.contactpatient.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import org.simple.clinic.R
import org.simple.clinic.contactpatient.PatientDetails
import org.simple.clinic.databinding.ContactpatientCallpatientBinding
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.patient.displayLetterRes
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.toLocalDateAtZone
import org.simple.clinic.widgets.visibleOrGone
import java.time.format.DateTimeFormatter

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

  private val registeredFacilityLabel
    get() = binding!!.registeredFacilityLabel

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

  private val lastVisitedLabel
    get() = binding!!.lastVisitedLabel

  private val patientWithCallResultGroup
    get() = binding!!.patientWithCallResultGroup

  private val patientWithNoPhoneNumberResultsGroup
    get() = binding!!.patientWithNoPhoneNumberResultGroup

  var secureCallingSectionVisible: Boolean = false
    set(value) {
      field = value
      secureCallingGroup.visibility = if (field) View.VISIBLE else View.GONE
    }

  var showPatientWithPhoneNumberLayout: Boolean = false
    set(value) {
      field = value
      patientWithPhoneNumberGroup.visibility = if (field) View.VISIBLE else View.GONE
    }

  var showPatientWithNoPhoneNumberLayout: Boolean = false
    set(value) {
      field = value
      patientWithoutPhoneNumberGroup.visibility = if (field) View.VISIBLE else View.GONE
    }

  var setResultOfCallLabelText: String = resources.getString(R.string.contactpatient_result_of_call)
    set(value) {
      field = value
      resultOfCallLabelTextView.text = field
    }

  var setRegisteredFacilityLabelText: String = resources.getString(R.string.contactpatient_patient_registered_at)
    set(value) {
      field = value
      registeredFacilityLabel.text = field
    }

  var showPatientWithCallResultLayout: Boolean = false
    set(value) {
      field = value
      patientWithCallResultGroup.visibility = if (field) View.VISIBLE else View.GONE
    }

  var showPatientWithNoPhoneNumberResults: Boolean = false
    set(value) {
      field = value
      patientWithNoPhoneNumberResultsGroup.visibility = if (field) View.VISIBLE else View.GONE
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
      patientDetails: PatientDetails,
      dateTimeFormatter: DateTimeFormatter,
      userClock: UserClock
  ) {
    val genderLetter = resources.getString(patientDetails.gender.displayLetterRes)
    val diagnosis = diagnosisText(patientDetails.diagnosedWithDiabetes, patientDetails.diagnosedWithHypertension)

    nameTextView.text = resources.getString(R.string.contactpatient_patientdetails, patientDetails.name, genderLetter, patientDetails.age.toString())
    phoneNumberTextView.text = patientDetails.phoneNumber
    patientAddressTextView.text = patientDetails.patientAddress
    diagnosisTextView.text = diagnosis

    registeredFacilityTextView.text = patientDetails.registeredFacility
    registeredFacilityLabel.visibleOrGone(patientDetails.registeredFacility != null)
    registeredFacilityTextView.visibleOrGone(patientDetails.registeredFacility != null)

    showLastVisitedIfNotNull(dateTimeFormatter, patientDetails, userClock)
  }

  private fun showLastVisitedIfNotNull(
      dateTimeFormatter: DateTimeFormatter,
      patientDetails: PatientDetails,
      userClock: UserClock
  ) {
    if (patientDetails.lastVisited != null) {
      lastVisitedTextView.visibility = View.VISIBLE
      lastVisitedTextView.text = dateTimeFormatter.format(patientDetails.lastVisited.toLocalDateAtZone(userClock.zone))
    } else {
      lastVisitedTextView.visibility = View.GONE
      lastVisitedLabel.visibility = View.GONE
    }
  }

  private fun diagnosisText(
      diagnosedWithDiabetes: Answer?,
      diagnosedWithHypertension: Answer?
  ): String {
    return listOf(
        diagnosedWithDiabetes to resources.getString(R.string.contactpatient_diagnosis_diabetes),
        diagnosedWithHypertension to resources.getString(R.string.contactpatient_diagnosis_hypertension)
    )
        .filter { (answer, _) -> answer is Answer.Yes }
        .map { (_, diagnosisTitle) -> diagnosisTitle }
        .ifEmpty { listOf(resources.getString(R.string.contactpatient_diagnosis_none)) }
        .joinToString()
  }
}
