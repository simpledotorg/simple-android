package org.simple.clinic.newentry

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Next
import com.spotify.mobius.Next.next
import com.spotify.mobius.Next.noChange

fun patientEntryInit(
    model: PatientEntryModel
): First<PatientEntryModel, PatientEntryEffect> =
    first(model, setOf(FetchPatientEntry))

fun patientEntryUpdate(
    model: PatientEntryModel,
    event: PatientEntryEvent
): Next<PatientEntryModel, PatientEntryEffect> {
  return when (event) {
    is OngoingEntryFetched -> next(model.patientEntryFetched(event.patientEntry), setOf(PrefillFields(event.patientEntry)))

    is GenderChanged -> {
      val updatedModel = model.withGender(event.gender)

      return if (event.gender.isNotEmpty() && model.isSelectingGenderForTheFirstTime) {
        next(updatedModel.copy(isSelectingGenderForTheFirstTime = false), setOf(ScrollFormToBottom))
      } else {
        next(updatedModel)
      }
    }

    is AgeChanged -> next(model.withAge(event.age), setOf(HideEmptyDateOfBirthAndAgeError))

    is DateOfBirthChanged -> next(model.withDateOfBirth(event.dateOfBirth), setOf(HideDateOfBirthErrors))

    is FullNameChanged -> next(model.withFullName(event.fullName), setOf(ShowEmptyFullNameError(false)))

    is PhoneNumberChanged -> next(model.withPhoneNumber(event.phoneNumber), setOf(HidePhoneLengthErrors))

    else -> noChange()
  }
}
