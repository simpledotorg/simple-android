package org.simple.clinic.newentry

import com.spotify.mobius.First
import com.spotify.mobius.First.first

fun patientEntryInit(
    model: PatientEntryModel
): First<PatientEntryModel, PatientEntryEffect> =
    first(model, setOf(FetchPatientEntry))
