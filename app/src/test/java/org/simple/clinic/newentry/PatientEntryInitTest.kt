package org.simple.clinic.newentry

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BangladeshSharedHealthRecordId

class PatientEntryInitTest {
  private val initSpec = InitSpec(PatientEntryInit())
  private val defaultModel = PatientEntryModel.DEFAULT

  @Test
  fun `when screen is created, then load initial data`() {
    initSpec.whenInit(defaultModel).then(assertThatFirst(
        hasModel(defaultModel),
        hasEffects(
            FetchPatientEntry, LoadInputFields, FetchColonyOrVillagesEffect
        )
    ))
  }

  @Test
  fun `when screen is restored, then don't fetch colony or villages`() {
    val colonyOrVillages = listOf("Colony1", "Colony2", "Colony3", "Colony4")

    val updatedVillageOrColonyNamesModel = defaultModel.colonyOrVillageListUpdated(colonyOrVillages)

    initSpec.whenInit(updatedVillageOrColonyNamesModel).then(assertThatFirst(
        hasModel(updatedVillageOrColonyNamesModel),
        hasEffects(
            FetchPatientEntry, LoadInputFields
        )
    ))
  }

  @Test
  fun `when screen is restored after the patient entry was fetched, then do not fetch it again`() {
    val shrId = Identifier("91234567890", BangladeshSharedHealthRecordId)
    val restoredModel = defaultModel
        .patientEntryFetched(OngoingNewPatientEntry())
        .shrIdAdded(shrId)
        .colonyOrVillageListUpdated(listOf("Colony1"))

    initSpec.whenInit(restoredModel).then(assertThatFirst(
        hasModel(restoredModel),
        hasEffects(LoadInputFields)
    ))
  }
}
