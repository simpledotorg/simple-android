package org.simple.clinic.widgets

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.google.android.material.card.MaterialCardView
import org.simple.clinic.R
import org.simple.clinic.databinding.ViewPatientSearchResultBinding
import org.simple.clinic.di.injector
import org.simple.clinic.patient.Age
import org.simple.clinic.patient.DateOfBirth
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.patient.businessid.Identifier
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

  private lateinit var binding: ViewPatientSearchResultBinding

  private val lastSeenContainer
    get() = binding.lastSeenContainer

  private val lastSeenTextView
    get() = binding.lastSeenTextView

  private val visitedContainer
    get() = binding.visitedContainer

  private val visitedTextView
    get() = binding.visitedTextView

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

  @Inject
  @Named("full_date")
  lateinit var dateTimeFormatter: DateTimeFormatter

  @Inject
  lateinit var userClock: UserClock

  override fun onFinishInflate() {
    super.onFinishInflate()
    val layoutInflater = LayoutInflater.from(context)
    binding = ViewPatientSearchResultBinding.inflate(layoutInflater, this)
    if (isInEditMode) {
      return
    }

    context.injector<Injector>().inject(this)
  }

  fun render(model: PatientSearchResultViewModel, currentFacilityId: UUID, searchQuery: String?) {
    renderPatientNameAgeAndGender(searchQuery, model)
    renderPatientAddress(model.address)
    renderPatientDateOfBirth(model.dateOfBirth)
    renderPatientPhoneNumber(searchQuery, model)
    renderVisited(model.lastSeen)
    renderLastSeen(model.lastSeen, currentFacilityId)
  }

  private fun getPatientPhoneNumber(
      phoneNumber: String,
      searchQuery: String?
  ): PhoneNumber {
    val canHighlightNumber = !searchQuery.isNullOrBlank() && phoneNumber.contains(searchQuery)

    return if (canHighlightNumber) {
      val indexOfSearchedPhoneNumber = phoneNumber.indexOf(searchQuery!!)
      PhoneNumber.Highlighted(
          patientNumber = phoneNumber,
          highlightStart = indexOfSearchedPhoneNumber,
          highlightEnd = indexOfSearchedPhoneNumber + searchQuery.length)
    } else {
      PhoneNumber.Plain(phoneNumber)
    }
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

  private fun renderPatientPhoneNumber(
      searchQuery: String?,
      patientSearchResult: PatientSearchResultViewModel
  ) {
    if (patientSearchResult.phoneNumber == null) {
      phoneNumberContainer.visibility = View.GONE
      return
    }

    phoneNumberContainer.visibility = View.VISIBLE

    val patientPhoneNumber = when (val number = getPatientPhoneNumber(patientSearchResult.phoneNumber, searchQuery)) {
      is PhoneNumber.Highlighted -> {
        val highlightNumber = SpannableStringBuilder(number.patientNumber)
        val highlightColor = context.resolveColor(colorRes = R.color.search_query_highlight)
        highlightNumber.setSpan(BackgroundColorSpan(highlightColor), number.highlightStart, number.highlightEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        highlightNumber
      }
      is PhoneNumber.Plain -> {
        number.patientNumber
      }
    }
    phoneNumberTextView.text = patientPhoneNumber
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

  private fun getPatientName(
      searchQuery: String?,
      patientSearchResult: PatientSearchResultViewModel,
      dateOfBirth: DateOfBirth
  ): Name {
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

  private fun renderPatientNameAgeAndGender(
      searchQuery: String?,
      model: PatientSearchResultViewModel
  ) {

    val patientName = when (val name = getPatientName(searchQuery, model, DateOfBirth.fromPatientSearchResultViewModel(model, userClock))) {
      is Name.Highlighted -> {
        val highlightName = SpannableStringBuilder(name.patientName)
        val highlightColor = context.resolveColor(colorRes = R.color.search_query_highlight)
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
      val lastSeen: PatientSearchResult.LastSeen?,
      val identifier: Identifier?
  )

  sealed class Name(open val patientName: String) {
    data class Highlighted(
        override val patientName: String,
        val highlightStart: Int,
        val highlightEnd: Int
    ) : Name(patientName)

    data class Plain(override val patientName: String) : Name(patientName)
  }

  sealed class PhoneNumber(open val patientNumber: String?) {
    data class Highlighted(
        override val patientNumber: String,
        val highlightStart: Int,
        val highlightEnd: Int
    ) : PhoneNumber(patientNumber)

    data class Plain(override val patientNumber: String) : PhoneNumber(patientNumber)
  }

  sealed class Id(open val value: String) {

    data class Highlighted(
        override val value: String,
        val highlightStart: Int,
        val highlightEnd: Int
    ) : Id(value)

    data class Plain(override val value: String) : Id(value)
  }

  interface Injector {
    fun inject(target: PatientSearchResultItemView)
  }
}
