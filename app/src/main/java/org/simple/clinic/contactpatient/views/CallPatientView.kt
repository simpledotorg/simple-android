package org.simple.clinic.contactpatient.views

import android.content.Context
import android.content.res.ColorStateList
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
import org.simple.clinic.util.resolveColor
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

  private val callResultsSeparator
    get() = binding!!.callResultsSeparator

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

  private val patientDiedStatusView
    get() = binding!!.patientDiedStatusView

  private val callResultOutcomeCardView
    get() = binding!!.callResultOutcomeCardView

  private val callResultOutcomeTextView
    get() = binding!!.callResultOutcomeTextView

  private val lastUpdatedDateTextView
    get() = binding!!.lastUpdatedDateTextView

  private val callResultOutcomeIcon
    get() = binding!!.callResultOutcomeIcon

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

  var callResultOutcomeViewVisible: Boolean = false
    set(value) {
      field = value
      callResultOutcomeCardView.visibility = if (field) View.VISIBLE else View.GONE
    }

  var callResultOutcomeText: String = ""
    set(value) {
      field = value
      callResultOutcomeTextView.text = field
    }

  var callResultLastUpdatedDate: String = ""
    set(value) {
      field = value
      lastUpdatedDateTextView.text = field
    }

  var showPatientWithCallResultLayout: Boolean = false
    set(value) {
      field = value
      renderPatientWithPhoneNumberResults(field)
    }

  var showPatientWithNoPhoneNumberResults: Boolean = false
    set(value) {
      field = value
      renderPatientWithNoPhoneNumberResults(field)
    }

  var showPatientDiedStatus: Boolean = false
    set(value) {
      field = value
      patientDiedStatusView.visibility = if (field) View.VISIBLE else View.GONE
    }

  var normalCallButtonText: String = resources.getString(R.string.contactpatient_call_normal)
    set(value) {
      field = value
      normalCallButton.text = field
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

  private fun renderPatientWithPhoneNumberResults(isVisible: Boolean) {
    resultOfCallLabelTextView.visibleOrGone(isVisible)
    agreedToVisitTextView.visibleOrGone(isVisible)
    remindToCallLaterTextView.visibleOrGone(isVisible)
    callResultsSeparator.visibleOrGone(isVisible)
    removeFromOverdueListTextView.visibleOrGone(isVisible)
  }

  private fun renderPatientWithNoPhoneNumberResults(isVisible: Boolean) {
    resultOfCallLabelTextView.visibleOrGone(isVisible)
    agreedToVisitTextView.visibleOrGone(isVisible)
    remindToCallLaterTextView.visibility = View.GONE
    callResultsSeparator.visibility = View.GONE
    removeFromOverdueListTextView.visibleOrGone(isVisible)
  }

  fun setupCallResultViewForAgreedToVisit() {
    callResultOutcomeCardView.setCardBackgroundColor(context.resolveColor(R.color.simple_green_100))
    callResultOutcomeCardView.strokeColor = context.resolveColor(R.color.simple_green_600)
    callResultOutcomeTextView.setTextColor(context.resolveColor(R.color.simple_green_600))
    lastUpdatedDateTextView.setTextColor(context.resolveColor(R.color.simple_green_600))
    callResultOutcomeIcon.imageTintList = ColorStateList.valueOf(
        context.resolveColor(R.color.simple_green_600)
    )
    callResultOutcomeIcon.setImageResource(R.drawable.ic_check_circle_outline)
  }

  fun setupCallResultViewForRemovedFromList() {
    callResultOutcomeCardView.setCardBackgroundColor(context.resolveColor(R.color.simple_red_100))
    callResultOutcomeCardView.strokeColor = context.resolveColor(R.color.simple_red_600)
    callResultOutcomeTextView.setTextColor(context.resolveColor(R.color.simple_red_600))
    lastUpdatedDateTextView.setTextColor(context.resolveColor(R.color.simple_red_600))
    callResultOutcomeIcon.imageTintList = ColorStateList.valueOf(
        context.resolveColor(R.color.simple_red_600)
    )
    callResultOutcomeIcon.setImageResource(R.drawable.ic_remove_circle_outline_24px)
  }

  fun setupCallResultViewForRemindToCallLater() {
    callResultOutcomeCardView.setCardBackgroundColor(context.resolveColor(R.color.simple_yellow_100))
    callResultOutcomeCardView.strokeColor = context.resolveColor(R.color.simple_yellow_600)
    callResultOutcomeTextView.setTextColor(context.resolveColor(R.color.simple_yellow_600))
    lastUpdatedDateTextView.setTextColor(context.resolveColor(R.color.simple_yellow_600))
    callResultOutcomeIcon.imageTintList = ColorStateList.valueOf(
        context.resolveColor(R.color.simple_yellow_600)
    )
    callResultOutcomeIcon.setImageResource(R.drawable.ic_access_alarm_24px)
  }
}
