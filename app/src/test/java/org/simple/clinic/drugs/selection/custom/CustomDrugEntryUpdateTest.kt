package org.simple.clinic.drugs.selection.custom

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.R
import org.simple.clinic.TestData
import org.simple.clinic.drugs.search.DrugFrequency
import org.simple.clinic.drugs.selection.custom.drugfrequency.country.CommonDrugFrequencyProvider
import org.simple.clinic.drugs.selection.custom.drugfrequency.country.DrugFrequencyChoiceItems
import org.simple.clinic.drugs.selection.custom.drugfrequency.country.DrugFrequencyFactory
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequency
import java.util.UUID

class CustomDrugEntryUpdateTest {

  private val updateSpec = UpdateSpec(CustomDrugEntryUpdate())
  private val drugName = "Amlodipine"
  private val patientUuid = UUID.fromString("77f1d870-5c60-49f7-a4e2-2f1d60e4218c")
  private val dosagePlaceholder = "mg"
  private val defaultModel = CustomDrugEntryModel.default(openAs = OpenAs.New.FromDrugName(drugName), dosagePlaceholder)

  private val drugFrequencyFactory = DrugFrequencyFactory(CommonDrugFrequencyProvider())
  private val drugFrequencyChoiceItems = drugFrequencyFactory.provideFields()

  @Test
  fun `when dosage is edited, then update the model with the new dosage`() {
    val dosage = "200 mg"
    val drugNameLoadedModel = defaultModel.drugNameLoaded(drugName)

    updateSpec.given(drugNameLoadedModel)
        .whenEvent(DosageEdited(dosage = dosage))
        .then(assertThatNext(
            hasModel(drugNameLoadedModel.dosageEdited(dosage = dosage)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when dosage edit text focus is changed, then update the model`() {
    val hasFocus = true

    updateSpec.given(defaultModel)
        .whenEvent(DosageFocusChanged(hasFocus))
        .then(
            assertThatNext(
                hasModel(defaultModel.dosageFocusChanged(hasFocus)),
                hasNoEffects()
            )
        )
  }

  @Test
  fun `when edit frequency is clicked, then show edit frequency dialog and pass drug frequency choice list`() {
    val frequency = DrugFrequency.OD

    updateSpec.given(defaultModel.frequencyEdited(frequency).drugFrequencyChoiceItemsLoaded(drugFrequencyChoiceItems))
        .whenEvent(EditFrequencyClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowEditFrequencyDialog(frequency, drugFrequencyChoiceItems))
        ))
  }

  @Test
  fun `when frequency is edited, then update the model and set drug frequency in the ui`() {
    val frequency = DrugFrequency.OD
    val drugNameLoadedModel = defaultModel.drugNameLoaded(drugName).drugFrequencyChoiceItemsLoaded(drugFrequencyChoiceItems)
    val frequencyResId = R.string.custom_drug_entry_sheet_frequency_OD

    updateSpec.given(drugNameLoadedModel)
        .whenEvent(FrequencyEdited(frequency))
        .then(assertThatNext(
            hasModel(drugNameLoadedModel.frequencyEdited(frequency)),
            hasEffects(SetDrugFrequency(frequencyResId))
        ))
  }

  @Test
  fun `when frequency is edited with a null value, then update the model and set drug frequency with the frequency in the ui`() {
    val drugNameLoadedModel = defaultModel.drugNameLoaded(drugName).drugFrequencyChoiceItemsLoaded(drugFrequencyChoiceItems)
    val frequencyLabelRes = R.string.custom_drug_entry_sheet_frequency_none

    updateSpec.given(drugNameLoadedModel)
        .whenEvent(FrequencyEdited(null))
        .then(assertThatNext(
            hasModel(drugNameLoadedModel.frequencyEdited(null)),
            hasEffects(SetDrugFrequency(frequencyLabelRes))
        ))
  }

  @Test
  fun `when add button is clicked and the sheet is opened in create mode from drug list with edited dosage and frequency values, then add the drug to the custom drug list`() {
    val dosage = "200 mg"
    val frequency = DrugFrequency.OD
    val drugUuid = UUID.fromString("6106544f-2b18-410d-992b-81860a08f02a")
    val drug = TestData.drug(id = drugUuid, name = drugName)
    val defaultModel = CustomDrugEntryModel.default(openAs = OpenAs.New.FromDrugList(drugUuid), dosagePlaceholder).drugNameLoaded(drugName).rxNormCodeEdited(drug.rxNormCode)

    updateSpec
        .given(defaultModel.dosageEdited(dosage).frequencyEdited(frequency))
        .whenEvent(AddMedicineButtonClicked(patientUuid))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(SaveCustomDrugToPrescription(patientUuid, drugName, dosage, drug.rxNormCode, frequency))
        ))
  }


  @Test
  fun `when add button is clicked and the sheet is opened in create mode from drug name with edited dosage and frequency values, then add the drug to the custom drug list`() {
    val dosage = "200 mg"
    val frequency = DrugFrequency.OD
    val defaultModel = CustomDrugEntryModel.default(openAs = OpenAs.New.FromDrugName(drugName), dosagePlaceholder).drugNameLoaded(drugName)

    updateSpec
        .given(defaultModel.dosageEdited(dosage).frequencyEdited(frequency))
        .whenEvent(AddMedicineButtonClicked(patientUuid))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(SaveCustomDrugToPrescription(patientUuid, drugName, dosage, null, frequency))
        ))
  }

  @Test
  fun `when save button is clicked, then update the prescription in the repository`() {
    val dosage = "200 mg"
    val frequency = DrugFrequency.OD
    val prescribedDrugUuid = UUID.fromString("96633994-6e4d-4528-b796-f03ae016553a")
    val defaultModel = CustomDrugEntryModel.default(openAs = OpenAs.Update(prescribedDrugUuid), dosagePlaceholder)

    updateSpec
        .given(defaultModel.drugNameLoaded(drugName).dosageEdited(dosage).frequencyEdited(frequency))
        .whenEvent(AddMedicineButtonClicked(patientUuid))
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(UpdatePrescription(patientUuid, prescribedDrugUuid, drugName, dosage, null, frequency))
            )
        )
  }

  @Test
  fun `when the new drug is added to the list, then close the bottom sheet and go to edit medicine screen`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(CustomDrugSaved)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(CloseSheetAndGoToEditMedicineScreen)
        ))
  }

  @Test
  fun `when the drug is fetched and is not deleted, then update the model and set frequency`() {
    val prescribedDrugUuid = UUID.fromString("96633994-6e4d-4528-b796-f03ae016553a")
    val drugFrequency = DrugFrequency.OD
    val dosage = "12mg"
    val prescribedDrug = TestData.prescription(uuid = prescribedDrugUuid, name = drugName, isDeleted = false, frequency = MedicineFrequency.OD, dosage = dosage)
    val defaultModel = CustomDrugEntryModel.default(openAs = OpenAs.Update(prescribedDrugUuid), dosagePlaceholder).drugFrequencyChoiceItemsLoaded(drugFrequencyChoiceItems)
    val frequencyResId = R.string.custom_drug_entry_sheet_frequency_OD

    updateSpec
        .given(defaultModel)
        .whenEvent(PrescribedDrugFetched(prescribedDrug))
        .then(
            assertThatNext(
                hasModel(defaultModel
                    .drugNameLoaded(drugName)
                    .dosageEdited(dosage = dosage)
                    .frequencyEdited(frequency = drugFrequency)
                    .rxNormCodeEdited(prescribedDrug.rxNormCode)),
                hasEffects(SetDrugFrequency(frequencyResId), SetDrugDosage(dosage)))
        )
  }

  @Test
  fun `when remove button is clicked, then remove the drug from the custom drug list`() {
    val prescribedDrugId = UUID.fromString("59842701-d7dd-4206-88a9-9f6f2460e496")
    val model = CustomDrugEntryModel.default(openAs = OpenAs.Update(prescribedDrugId), dosagePlaceholder)

    updateSpec
        .given(model)
        .whenEvent(RemoveDrugButtonClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(RemoveDrugFromPrescription(drugUuid = prescribedDrugId))
        ))
  }

  @Test
  fun `when the drug is removed from the custom drug list, then close the bottom sheet and go to edit medicine screen`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(ExistingDrugRemoved)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(CloseSheetAndGoToEditMedicineScreen)
        ))
  }

  @Test
  fun `when drug is fetched, then update the model with drug values and set drug frequency and dosage`() {
    val drugUuid = UUID.fromString("6bbc5bbe-863c-472a-b962-1fd3198e20d1")
    val drug = TestData.drug(id = drugUuid, frequency = DrugFrequency.OD)
    val frequencyResId = R.string.custom_drug_entry_sheet_frequency_OD
    val drugFrequencyChoiceItemsLoaded = defaultModel.drugFrequencyChoiceItemsLoaded(drugFrequencyChoiceItems)

    updateSpec
        .given(drugFrequencyChoiceItemsLoaded)
        .whenEvent(DrugFetched(drug))
        .then(
            assertThatNext(
                hasModel(drugFrequencyChoiceItemsLoaded.drugNameLoaded(drug.name).dosageEdited(drug.dosage).frequencyEdited(drug.frequency).rxNormCodeEdited(drug.rxNormCode)),
                hasEffects(SetDrugFrequency(frequencyResId), SetDrugDosage(drug.dosage))
            )
        )
  }

  @Test
  fun `when drug frequency choice items are loaded, then update the model`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(DrugFrequencyChoiceItemsLoaded(DrugFrequencyChoiceItems(drugFrequencyChoiceItems)))
        .then(
            assertThatNext(
                hasModel(defaultModel.drugFrequencyChoiceItemsLoaded(drugFrequencyChoiceItems))
            )
        )
  }
}
