package org.simple.clinic.newentry

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.ReminderConsent
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.util.Optional
import org.simple.clinic.util.isNotEmpty

@Parcelize
data class PatientEntryModel(
    val patientEntry: OngoingNewPatientEntry = OngoingNewPatientEntry(),
    val isSelectingGenderForTheFirstTime: Boolean = true,
    val nextButtonState: ButtonState? = null,
    val colonyOrVillagesList: List<String>? = null
) : Parcelable {
  companion object {
    val DEFAULT = PatientEntryModel()
  }

  val hasColonyOrVillagesList: Boolean
    get() = !colonyOrVillagesList.isNullOrEmpty()

  fun patientEntryFetched(patientEntry: OngoingNewPatientEntry): PatientEntryModel =
      copy(patientEntry = patientEntry)

  fun genderChanged(gender: Optional<Gender>): PatientEntryModel {
    val patientEntryWithUpdatedGender = patientEntry.withGender(gender)
    return if (gender.isNotEmpty()) {
      copy(patientEntry = patientEntryWithUpdatedGender, isSelectingGenderForTheFirstTime = false)
    } else {
      copy(patientEntry = patientEntryWithUpdatedGender)
    }
  }

  fun ageChanged(age: String): PatientEntryModel =
      copy(patientEntry = patientEntry.withAge(age))

  fun dateOfBirthChanged(dateOfBirth: String): PatientEntryModel =
      copy(patientEntry = patientEntry.withDateOfBirth(dateOfBirth))

  fun fullNameChanged(fullName: String): PatientEntryModel =
      copy(patientEntry = patientEntry.withFullName(fullName))

  fun phoneNumberChanged(phoneNumber: String): PatientEntryModel =
      copy(patientEntry = patientEntry.withPhoneNumber(phoneNumber))

  fun colonyOrVillageChanged(colonyOrVillage: String): PatientEntryModel =
      copy(patientEntry = patientEntry.withColonyOrVillage(colonyOrVillage))

  fun districtChanged(district: String): PatientEntryModel =
      copy(patientEntry = patientEntry.withDistrict(district))

  fun stateChanged(state: String): PatientEntryModel =
      copy(patientEntry = patientEntry.withState(state))

  fun reminderConsentChanged(consent: ReminderConsent): PatientEntryModel =
      copy(patientEntry = patientEntry.withConsent(consent))

  fun alternativeIdChanged(identifier: Identifier): PatientEntryModel =
      copy(patientEntry = patientEntry.withAlternativeId(identifier))

  fun streetAddressChanged(streetAddress: String): PatientEntryModel =
      copy(patientEntry = patientEntry.withStreetAddress(streetAddress))

  fun zoneChanged(zone: String): PatientEntryModel =
      copy(patientEntry = patientEntry.withZone(zone))

  fun buttonStateChanged(buttonState: ButtonState): PatientEntryModel =
      copy(nextButtonState = buttonState)

  fun colonyOrVillageListUpdated(colonyOrVillages: List<String>?): PatientEntryModel =
      copy(colonyOrVillagesList = colonyOrVillages)
}
