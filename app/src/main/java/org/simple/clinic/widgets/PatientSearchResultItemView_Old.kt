package org.simple.clinic.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.google.android.material.card.MaterialCardView
import org.simple.clinic.R
import org.simple.clinic.databinding.ViewPatientSearchResultOldBinding
import org.simple.clinic.di.injector
import org.simple.clinic.patient.Age
import org.simple.clinic.patient.DateOfBirth
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.patient.displayIconRes
import org.simple.clinic.patient.displayLetterRes
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.toLocalDateAtZone
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

class PatientSearchResultItemView_Old(
    context: Context,
    attributeSet: AttributeSet
) : MaterialCardView(context, attributeSet) {

  @Inject
  @Named("full_date")
  lateinit var dateTimeFormatter: DateTimeFormatter

  @Inject
  lateinit var userClock: UserClock

  private lateinit var binding: ViewPatientSearchResultOldBinding

  private val lastSeenContainer
    get() = binding.lastSeenContainer

  private val lastSeenTextView
    get() = binding.lastSeenTextView

  private val phoneNumberContainer
    get() = binding.phoneNumberContainer

  private val phoneNumberTextView
    get() = binding.phoneNumberTextView

  private val dateOfBirthContainer
    get() = binding.dateOfBirthContainer

  private val dateOfBirthTextView
    get() = binding.dateOfBirthTextView

  private val addressLabel
    get() = binding.addressLabel

  private val genderLabel
    get() = binding.genderLabel

  private val patientNameAgeGenderLabel
    get() = binding.patientNameAgeGenderLabel

  override fun onFinishInflate() {
    super.onFinishInflate()
    val layoutInflater = LayoutInflater.from(context)
    binding = ViewPatientSearchResultOldBinding.inflate(layoutInflater, this)
    if (isInEditMode) {
      return
    }

    context.injector<Injector>().inject(this)
  }

  fun render(model: PatientSearchResultViewModel, currentFacilityUuid: UUID) {
    renderPatientNameAgeAndGender(model.fullName, model.gender, DateOfBirth.fromPatientSearchResultViewModel_Old(model, userClock))
    renderPatientAddress(model.address)
    renderPatientDateOfBirth(model.dateOfBirth)
    renderPatientPhoneNumber(model.phoneNumber)
    renderLastSeen(model.lastSeen, currentFacilityUuid)
  }

  private fun renderLastSeen(
      lastSeen: PatientSearchResult.LastSeen?,
      currentFacilityUuid: UUID
  ) {
    lastSeenContainer.visibleOrGone(lastSeen != null)
    if (lastSeen == null) {
      lastSeenContainer.visibility = View.GONE
    } else {
      lastSeenContainer.visibility = View.VISIBLE

      val lastSeenDate = lastSeen.lastSeenOn.toLocalDateAtZone(userClock.zone)
      val formattedLastSeenDate = dateTimeFormatter.format(lastSeenDate)

      val isCurrentFacility = lastSeen.lastSeenAtFacilityUuid == currentFacilityUuid
      if (isCurrentFacility) {
        lastSeenTextView.text = formattedLastSeenDate
      } else {
        lastSeenTextView.text = resources.getString(
            R.string.patientsearchresults_item_last_seen_date_with_facility,
            formattedLastSeenDate,
            lastSeen.lastSeenAtFacilityName)
      }
    }
  }

  private fun renderPatientPhoneNumber(phoneNumber: String?) {
    phoneNumberContainer.visibleOrGone(phoneNumber.isNullOrBlank().not())
    if (phoneNumber != null) {
      phoneNumberTextView.text = phoneNumber
    }
  }

  private fun renderPatientDateOfBirth(dateOfBirth: LocalDate?) {
    dateOfBirthContainer.visibleOrGone(dateOfBirth != null)
    if (dateOfBirth != null) {
      dateOfBirthTextView.text = dateTimeFormatter.format(dateOfBirth)
    }
  }

  private fun renderPatientAddress(address: PatientAddress) {
    addressLabel.text = address.completeAddress
  }

  private fun renderPatientNameAgeAndGender(
      fullName: String,
      gender: Gender,
      dateOfBirth: DateOfBirth
  ) {
    genderLabel.setImageResource(gender.displayIconRes)

    val ageValue = dateOfBirth.estimateAge(userClock)

    val genderLetter = resources.getString(gender.displayLetterRes)
    patientNameAgeGenderLabel.text = resources.getString(R.string.patientsummary_toolbar_title, fullName, genderLetter, ageValue.toString())
  }

  data class PatientSearchResultViewModel(
      val uuid: UUID,
      val fullName: String,
      val gender: Gender,
      val age: Age?,
      val dateOfBirth: LocalDate?,
      val address: PatientAddress,
      val phoneNumber: String?,
      val lastSeen: PatientSearchResult.LastSeen?
  )

  interface Injector {
    fun inject(target: PatientSearchResultItemView_Old)
  }
}
