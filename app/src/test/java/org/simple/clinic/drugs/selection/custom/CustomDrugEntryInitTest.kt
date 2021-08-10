package org.simple.clinic.drugs.selection.custom

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.drugs.search.DrugFrequency
import java.util.UUID

class CustomDrugEntryInitTest {
  private val initSpec = InitSpec(CustomDrugEntryInit())
  private val drugName = "Amolodipine"

  @Test
  fun `when sheet is created in create mode from a drug list, then set the dosage, frequency and sheet title from the drug object`() {
    val frequency = DrugFrequency.OD
    val drug = TestData.drug(id = UUID.fromString("6106544f-2b18-410d-992b-81860a08f02a"), name = drugName, frequency = frequency)
    val model = CustomDrugEntryModel.default(openAs = OpenAs.New.FromDrugList(patientUuid = UUID.fromString("13008153-beda-475a-909c-793d03e654fb"), drug))

    initSpec
        .whenInit(model)
        .then(
            assertThatFirst(
                hasModel(model.drugNameLoaded(drugName).dosageEdited(drug.dosage).frequencyEdited(frequency)),
                hasEffects(SetSheetTitle(name = drugName, dosage = drug.dosage, frequency = frequency), SetDrugFrequency(frequency), SetDrugDosage(drug.dosage))
            )
        )
  }

  @Test
  fun `when sheet is created in create mode from a drug name, then set the drug name, frequency and sheet title`() {
    val model = CustomDrugEntryModel.default(openAs = OpenAs.New.FromDrugName(patientUuid = UUID.fromString("13008153-beda-475a-909c-793d03e654fb"), drugName))

    initSpec
        .whenInit(model)
        .then(
            assertThatFirst(
                hasModel(model.drugNameLoaded(drugName)),
                hasEffects(SetSheetTitle(name = drugName, dosage = null, frequency = null), SetDrugFrequency(null))
            )
        )
  }
}
