package org.simple.clinic.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.cardview.widget.CardView
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.view_patient_search_result.view.*
import org.simple.clinic.R
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

class PatientSearchResultItemView(
    context: Context,
    attributeSet: AttributeSet
) : MaterialCardView(context, attributeSet) {

  @Inject
  @Named("full_date")
  lateinit var dateTimeFormatter: DateTimeFormatter

  @Inject
  lateinit var userClock: UserClock

  override fun onFinishInflate() {
    super.onFinishInflate()
    inflate(context, R.layout.view_patient_search_result, this)
    if (isInEditMode) {
      return
    }

    context.injector<Injector>().inject(this)
  }

  fun render(model: PatientSearchResultViewModel, currentFacilityId: UUID) {
    renderPatientNameAgeAndGender(model.fullName, model.gender, DateOfBirth.fromPatientSearchResultViewModel(model, userClock))
    renderPatientAddress(model.address)
    renderPatientDateOfBirth(model.dateOfBirth)
    renderPatientPhoneNumber(model.phoneNumber)
    renderVisited(model.lastSeen)
    renderLastSeen(model.lastSeen, currentFacilityId)
  }

  private fun renderLastSeen(lastSeen: PatientSearchResult.LastSeen?, currentFacilityId: UUID) {
    val isAtCurrentFacility = currentFacilityId == lastSeen?.lastSeenAtFacilityUuid
    lastSeenContainer.visibleOrGone(lastSeen != null && !isAtCurrentFacility)
    if (lastSeen != null) {
      lastSeenTextView.text = lastSeen.lastSeenAtFacilityName
    }
  }

  private fun renderVisited(
      lastSeen: PatientSearchResult.LastSeen?
  ) {
    visitedContainer.visibleOrGone(lastSeen != null)
    if (lastSeen != null) {
      val lastSeenDate = lastSeen.lastSeenOn.toLocalDateAtZone(userClock.zone)
      val formattedLastSeenDate = dateTimeFormatter.format(lastSeenDate)

      visitedTextView.text = formattedLastSeenDate
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

  private fun renderPatientNameAgeAndGender(fullName: String, gender: Gender, dateOfBirth: DateOfBirth) {
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
    fun inject(target: PatientSearchResultItemView)
  }
}
