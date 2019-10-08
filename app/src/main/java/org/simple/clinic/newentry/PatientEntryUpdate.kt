package org.simple.clinic.newentry

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.registration.phone.PhoneNumberValidator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator

class PatientEntryUpdate(
    private val phoneNumberValidator: PhoneNumberValidator,
    private val dobValidator: UserInputDateValidator
) : Update<PatientEntryModel, PatientEntryEvent, PatientEntryEffect> {
  override fun update(model: PatientEntryModel, event: PatientEntryEvent): Next<PatientEntryModel, PatientEntryEffect> {
    return when (event) {
      is OngoingEntryFetched -> Next.next(model.patientEntryFetched(event.patientEntry), setOf(PrefillFields(event.patientEntry)))

      is GenderChanged -> {
        val updatedModel = model.withGender(event.gender)

        return if (event.gender.isNotEmpty() && model.isSelectingGenderForTheFirstTime) {
          Next.next(updatedModel.copy(isSelectingGenderForTheFirstTime = false), setOf(HideMissingGenderError, ScrollFormToBottom))
        } else {
          Next.next(updatedModel)
        }
      }

      is AgeChanged -> Next.next(model.withAge(event.age), setOf(HideEmptyDateOfBirthAndAgeError))

      is DateOfBirthChanged -> Next.next(model.withDateOfBirth(event.dateOfBirth), setOf(HideDateOfBirthErrors))

      is FullNameChanged -> Next.next(model.withFullName(event.fullName), setOf(ShowEmptyFullNameError(false)))

      is PhoneNumberChanged -> Next.next(model.withPhoneNumber(event.phoneNumber), setOf(HidePhoneLengthErrors))

      is ColonyOrVillageChanged -> Next.next(model.withColonyOrVillage(event.colonyOrVillage), setOf(HideEmptyColonyOrVillageError))

      is DistrictChanged -> Next.next(model.withDistrict(event.district), setOf(HideEmptyDistrictError))

      is StateChanged -> Next.next(model.withState(event.state), setOf(HideEmptyStateError))

      is DateOfBirthFocusChanged -> {
        val hasDateOfBirth = model.patientEntry?.personalDetails?.dateOfBirth?.isNotBlank() == true
        // TODO(rj): 2019-10-04 Extract the justEffect function and use it instead.
        Next.dispatch<PatientEntryModel, PatientEntryEffect>(setOf(ShowDatePatternInDateOfBirthLabel(event.hasFocus || hasDateOfBirth)))
      }

      is SaveClicked -> {
        val validationErrors = model.patientEntry!!.validationErrors(dobValidator, phoneNumberValidator)
        return if (validationErrors.isEmpty()) {
          Next.dispatch(setOf(SavePatient(model.patientEntry)))
        } else {
          Next.dispatch(setOf(ShowValidationErrors(validationErrors)))
        }
      }
    }
  }
}
