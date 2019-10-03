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
  if (event is OngoingEntryFetched) {
    return next(
        model.patientEntryFetched(event.patientEntry),
        setOf(PrefillFields(event.patientEntry))
    )
  } else if (event is GenderChanged) {
    val updatedModel = model.withGender(event.gender)

    if (event.gender.isNotEmpty() && model.isSelectingGenderForTheFirstTime) {
      return next(updatedModel.copy(isSelectingGenderForTheFirstTime = false), setOf(ScrollFormToBottom))
    } else {
      return next(updatedModel)
    }
  } else if (event is AgeChanged) {
    return next(model.withAge(event.age))
  } else if (event is DateOfBirthChanged) {
    return next(model.withDateOfBirth(event.dateOfBirth))
  }

  return noChange()
}
