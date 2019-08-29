package org.simple.clinic.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.cardview.widget.CardView
import kotlinx.android.synthetic.main.view_patient_search_result.view.*
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.patient.Age
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.patient.displayIconRes
import org.simple.clinic.patient.displayLetterRes
import org.simple.clinic.searchresultsview.PhoneNumberObfuscator
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.estimateCurrentAge
import org.simple.clinic.util.toLocalDateAtZone
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

class PatientSearchResultItemView(
    context: Context,
    attributeSet: AttributeSet
) : CardView(context, attributeSet) {

  @field:Inject
  lateinit var phoneObfuscator: PhoneNumberObfuscator

  @field:[Inject Named("date_for_search_results")]
  lateinit var dateTimeFormatter: DateTimeFormatter

  @field:Inject
  lateinit var utcClock: UtcClock

  @field:Inject
  lateinit var userClock: UserClock

  override fun onFinishInflate() {
    super.onFinishInflate()
    inflate(context, R.layout.view_patient_search_result, this)
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)
  }

  fun render(model: PatientSearchResultViewModel, currentFacilityUuid: UUID) {
    renderPatientNameAgeAndGender(model.fullName, model.gender, model.age, model.dateOfBirth)
    renderPatientAddress(model.address)
    renderPatientDateOfBirth(model.dateOfBirth)
    renderPatientPhoneNumber(model.phoneNumber)
    renderLastRecordedBloodPressure(model.lastBp, currentFacilityUuid)
  }

  private fun renderLastRecordedBloodPressure(
      lastBp: PatientSearchResult.LastBp?,
      currentFacilityUuid: UUID
  ) {
    if (lastBp == null) {
      lastBpContainer.visibility = View.GONE
    } else {
      lastBpContainer.visibility = View.VISIBLE

      val lastBpDate = lastBp.takenOn.toLocalDateAtZone(userClock.zone)
      val formattedLastBpDate = dateTimeFormatter.format(lastBpDate)

      val isCurrentFacility = lastBp.takenAtFacilityUuid == currentFacilityUuid
      if (isCurrentFacility) {
        lastBpLabel.text = formattedLastBpDate
      } else {
        lastBpLabel.text = resources.getString(
            R.string.patientsearchresults_item_last_bp_date_with_facility,
            formattedLastBpDate,
            lastBp.takenAtFacilityName)
      }
    }
  }

  private fun renderPatientPhoneNumber(phoneNumber: String?) {
    if (phoneNumber.isNullOrBlank()) {
      phoneNumberLabel.visibility = View.GONE
    } else {
      phoneNumberLabel.visibility = View.VISIBLE
      phoneNumberLabel.text = phoneObfuscator.obfuscate(phoneNumber)
    }
  }

  private fun renderPatientDateOfBirth(dateOfBirth: LocalDate?) {
    if (dateOfBirth == null) {
      dateOfBirthLabel.visibility = View.GONE
    } else {
      dateOfBirthLabel.visibility = View.VISIBLE
      dateOfBirthLabel.text = dateTimeFormatter.format(dateOfBirth)
    }
  }

  private fun renderPatientAddress(address: PatientAddress) {
    if (address.colonyOrVillage.isNullOrEmpty()) {
      addressLabel.text = address.district
    } else {
      addressLabel.text = resources.getString(
          R.string.patientsearchresults_item_address_with_colony_and_district,
          address.colonyOrVillage,
          address.district)
    }
  }

  private fun renderPatientNameAgeAndGender(fullName: String, gender: Gender, age: Age?, dateOfBirth: LocalDate?) {
    genderLabel.setImageResource(gender.displayIconRes)

    val ageString = when (age) {
      null -> {
        estimateCurrentAge(dateOfBirth!!, utcClock)
      }
      else -> {
        val (recordedAge, ageRecordedAtTimestamp) = age
        estimateCurrentAge(recordedAge, ageRecordedAtTimestamp, utcClock)
      }
    }

    patientNameAgeGenderLabel.text = resources.getString(
        R.string.patientsearchresults_item_name_with_gender_and_age,
        fullName,
        resources.getString(gender.displayLetterRes),
        ageString)
  }

  data class PatientSearchResultViewModel(
      val uuid: UUID,
      val fullName: String,
      val gender: Gender,
      val age: Age?,
      val dateOfBirth: LocalDate?,
      val address: PatientAddress,
      val phoneNumber: String?,
      val lastBp: PatientSearchResult.LastBp?
  )
}
