package org.simple.clinic.widgets

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import android.util.AttributeSet
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
import org.simple.clinic.router.util.resolveColor
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

  fun render(model: PatientSearchResultViewModel, currentFacilityId: UUID, searchQuery: String?) {
    renderPatientNameAgeAndGender(searchQuery, model)
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

  private fun getPatientName(searchQuery: String?, patientSearchResult: PatientSearchResultViewModel, dateOfBirth: DateOfBirth): Name {
    val canHighlight = !searchQuery.isNullOrBlank() && patientSearchResult.fullName.contains(searchQuery, ignoreCase = true)
    genderLabel.setImageResource(patientSearchResult.gender.displayIconRes)
    val ageValue = dateOfBirth.estimateAge(userClock)
    val genderLetter = resources.getString(patientSearchResult.gender.displayLetterRes)
    val patientNameAgeAndGender = resources.getString(R.string.patientsummary_toolbar_title, patientSearchResult.fullName, genderLetter, ageValue.toString())
    return if (canHighlight) {
      val indexOfPatientName = patientSearchResult.fullName.indexOf(searchQuery!!, ignoreCase = true)
      Name.Highlighted(
          patientName = patientNameAgeAndGender,
          highlightStart = indexOfPatientName,
          highlightEnd = indexOfPatientName + searchQuery.length)
    } else {
      Name.Plain(patientNameAgeAndGender)
    }
  }

  private fun renderPatientNameAgeAndGender(searchQuery: String?, model: PatientSearchResultViewModel) {

    val patientName = when (val name = getPatientName(searchQuery, model, DateOfBirth.fromPatientSearchResultViewModel(model, userClock))) {
      is Name.Highlighted -> {
        val highlightName = SpannableStringBuilder(name.patientName)
        val highlightColor = context.resolveColor(colorRes = R.color.simple_light_blue_100)
        highlightName.setSpan(BackgroundColorSpan(highlightColor), name.highlightStart, name.highlightEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        highlightName
      }
      is Name.Plain -> {
        name.patientName
      }
    }
    patientNameAgeGenderLabel.text = patientName
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

  sealed class Name(open val patientName: String) {
    data class Highlighted(override val patientName: String, val highlightStart: Int, val highlightEnd: Int) : Name(patientName)
    data class Plain(override val patientName: String) : Name(patientName)
  }

  interface Injector {
    fun inject(target: PatientSearchResultItemView)
  }
}
