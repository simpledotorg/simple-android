package org.simple.clinic.newentry

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange

fun patientEntryInit(
    model: PatientEntryModel
): First<PatientEntryModel, PatientEntryEffect> {
  return first(model)
}

fun patientEntryUpdate(
    model: PatientEntryModel,
    event: PatientEntryEvent
): Next<PatientEntryModel, PatientEntryEffect> {
  return noChange()
}
