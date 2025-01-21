package org.simple.clinic.drugs.selection.custom

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.drugs.search.DrugFrequency.BD
import org.simple.clinic.drugs.search.DrugFrequency.OD
import org.simple.clinic.drugs.search.DrugFrequency.QDS
import org.simple.clinic.drugs.search.DrugFrequency.TDS
import org.simple.clinic.drugs.selection.custom.ButtonState.SAVING
import org.simple.clinic.drugs.selection.custom.drugfrequency.country.DrugFrequencyLabel
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequency
import java.util.UUID

class CustomDrugEntryUpdateTest {

  private val updateSpec = UpdateSpec(CustomDrugEntryUpdate())
  private val drugName = "Amlodipine"
  private val patientUuid = UUID.fromString("77f1d870-5c60-49f7-a4e2-2f1d60e4218c")
  private val dosagePlaceholder = "mg"
  private val defaultModel = CustomDrugEntryModel.default(openAs = OpenAs.New.FromDrugName(drugName), dosagePlaceholder)

  private val drugFrequencyToLabelMap = mapOf(
      null to DrugFrequencyLabel(label = "None"),
      OD to DrugFrequencyLabel(label = "OD"),
      BD to DrugFrequencyLabel(label = "BD"),
      TDS to DrugFrequencyLabel(label = "TDS"),
      QDS to DrugFrequencyLabel(label = "QDS")
  )

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
  fun `when edit frequency is clicked, then show edit frequency dialog and clear focus from dosage edit text`() {
    val frequency = OD
    updateSpec.given(defaultModel.frequencyEdited(frequency).drugFrequencyToLabelMapLoaded(drugFrequencyToLabelMap))
        .whenEvent(EditFrequencyClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowEditFrequencyDialog(frequency), ClearFocusFromDosageEditText)
        ))
  }

  @Test
  fun `when frequency is edited, then update the model and set drug frequency in the ui`() {
    val frequency = OD
    val drugNameLoadedModel = defaultModel.drugNameLoaded(drugName).drugFrequencyToLabelMapLoaded(drugFrequencyToLabelMap)
    val frequencyRes = "OD"

    updateSpec.given(drugNameLoadedModel)
        .whenEvent(FrequencyEdited(frequency))
        .then(assertThatNext(
            hasModel(drugNameLoadedModel.frequencyEdited(frequency)),
            hasEffects(SetDrugFrequency(frequencyRes))
        ))
  }

  @Test
  fun `when frequency is edited with a null value, then update the model and set drug frequency with the frequency in the ui`() {
    val drugNameLoadedModel = defaultModel.drugNameLoaded(drugName).drugFrequencyToLabelMapLoaded(drugFrequencyToLabelMap)
    val frequencyLabel = "None"

    updateSpec.given(drugNameLoadedModel)
        .whenEvent(FrequencyEdited(null))
        .then(assertThatNext(
            hasModel(drugNameLoadedModel.frequencyEdited(null)),
            hasEffects(SetDrugFrequency(frequencyLabel))
        ))
  }

  @Test
  fun `when add button is clicked and the sheet is opened in create mode from drug list with edited dosage and frequency values, then add the drug to the custom drug list and set button progress state to saving`() {
    val dosage = "200 mg"
    val frequency = OD
    val drugUuid = UUID.fromString("6106544f-2b18-410d-992b-81860a08f02a")
    val drug = TestData.drug(id = drugUuid, name = drugName)
    val model = CustomDrugEntryModel
        .default(openAs = OpenAs.New.FromDrugList(drugUuid), dosagePlaceholder)
        .drugNameLoaded(drugName)
        .rxNormCodeEdited(drug.rxNormCode)
        .dosageEdited(dosage)
        .frequencyEdited(frequency)

    updateSpec
        .given(model)
        .whenEvent(AddMedicineButtonClicked(patientUuid))
        .then(assertThatNext(
            hasModel(model.saveButtonStateChanged(SAVING)),
            hasEffects(SaveCustomDrugToPrescription(patientUuid, drugName, dosage, drug.rxNormCode, frequency))
        ))
  }


  @Test
  fun `when add button is clicked and the sheet is opened in create mode from drug name with edited dosage and frequency values, then add the drug to the custom drug list and set button progress state to saving`() {
    val dosage = "200 mg"
    val frequency = OD
    val model = CustomDrugEntryModel
        .default(openAs = OpenAs.New.FromDrugName(drugName), dosagePlaceholder)
        .drugNameLoaded(drugName)
        .dosageEdited(dosage)
        .frequencyEdited(frequency)

    updateSpec
        .given(model)
        .whenEvent(AddMedicineButtonClicked(patientUuid))
        .then(assertThatNext(
            hasModel(model.saveButtonStateChanged(SAVING)),
            hasEffects(SaveCustomDrugToPrescription(patientUuid, drugName, dosage, null, frequency))
        ))
  }

  @Test
  fun `when save button is clicked and dosage is invalid, then update the prescription in the repository without the dosage and set button progress state to saving`() {
    val dosage = "mg"
    val frequency = OD
    val prescribedDrugUuid = UUID.fromString("96633994-6e4d-4528-b796-f03ae016553a")
    val model = CustomDrugEntryModel
        .default(openAs = OpenAs.Update(prescribedDrugUuid), dosagePlaceholder)
        .drugNameLoaded(drugName)
        .dosageEdited(dosage)
        .frequencyEdited(frequency)

    updateSpec
        .given(model)
        .whenEvent(AddMedicineButtonClicked(patientUuid))
        .then(
            assertThatNext(
                hasModel(model.saveButtonStateChanged(SAVING)),
                hasEffects(UpdatePrescription(patientUuid, prescribedDrugUuid, drugName, null, null, frequency))
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
  fun `when the drug is fetched, is not deleted and has a numeric dosage value, then update the model, set frequency, show keyboard and set cursor position`() {
    val prescribedDrugUuid = UUID.fromString("96633994-6e4d-4528-b796-f03ae016553a")
    val drugFrequency = OD
    val dosage = "12mg"
    val position = 2
    val prescribedDrug = TestData.prescription(uuid = prescribedDrugUuid, name = drugName, isDeleted = false, frequency = MedicineFrequency.OD, dosage = dosage)
    val defaultModel = CustomDrugEntryModel
        .default(openAs = OpenAs.Update(prescribedDrugUuid), dosagePlaceholder)
        .drugFrequencyToLabelMapLoaded(drugFrequencyToLabelMap)
    val frequencyRes = "OD"

    val updatedModel = defaultModel
        .drugNameLoaded(drugName)
        .dosageEdited(dosage = dosage)
        .frequencyEdited(frequency = drugFrequency)
        .rxNormCodeEdited(prescribedDrug.rxNormCode)
        .drugInfoProgressStateLoaded()

    updateSpec
        .given(defaultModel)
        .whenEvent(PrescribedDrugFetched(prescribedDrug))
        .then(
            assertThatNext(
                hasModel(updatedModel),
                hasEffects(SetDrugFrequency(frequencyRes), SetDrugDosage(dosage), ShowKeyboard, SetCursorPosition(position)))
        )
  }

  @Test
  fun `when remove button is clicked, then remove the drug from the custom drug list and set the screen in progress state`() {
    val prescribedDrugId = UUID.fromString("59842701-d7dd-4206-88a9-9f6f2460e496")
    val model = CustomDrugEntryModel.default(openAs = OpenAs.Update(prescribedDrugId), dosagePlaceholder)

    updateSpec
        .given(model)
        .whenEvent(RemoveDrugButtonClicked)
        .then(assertThatNext(
            hasModel(model.drugInfoProgressStateLoading()),
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
  fun `when drug is fetched and dosage has numeric values, then update the model with drug values, set drug frequency, dosage, show keyboard and set cursor position`() {
    val drugUuid = UUID.fromString("6bbc5bbe-863c-472a-b962-1fd3198e20d1")
    val dosage = "10 mg/ 150 mg"
    val position = 10
    val drug = TestData.drug(id = drugUuid, frequency = OD, dosage = dosage)
    val frequencyRes = "OD"
    val drugFrequencyChoiceItemsLoaded = defaultModel.drugFrequencyToLabelMapLoaded(drugFrequencyToLabelMap)

    val updateModel = drugFrequencyChoiceItemsLoaded
        .drugNameLoaded(drug.name)
        .dosageEdited(drug.dosage)
        .frequencyEdited(drug.frequency)
        .rxNormCodeEdited(drug.rxNormCode)
        .drugInfoProgressStateLoaded()

    updateSpec
        .given(drugFrequencyChoiceItemsLoaded)
        .whenEvent(DrugFetched(drug))
        .then(
            assertThatNext(
                hasModel(updateModel),
                hasEffects(SetDrugFrequency(frequencyRes), SetDrugDosage(drug.dosage), ShowKeyboard, SetCursorPosition(position))
            )
        )
  }

  @Test
  fun `when drug is fetched and dosage does not have a numeric values, then update the model with drug values, set drug frequency, dosage, show keyboard and set cursor position`() {
    val drugUuid = UUID.fromString("6bbc5bbe-863c-472a-b962-1fd3198e20d1")
    val dosage = "mg"
    val position = 0
    val drug = TestData.drug(id = drugUuid, frequency = OD, dosage = dosage)
    val frequencyRes = "OD"
    val drugFrequencyChoiceItemsLoaded = defaultModel.drugFrequencyToLabelMapLoaded(drugFrequencyToLabelMap)

    val updateModel = drugFrequencyChoiceItemsLoaded
        .drugNameLoaded(drug.name)
        .dosageEdited(drug.dosage)
        .frequencyEdited(drug.frequency)
        .rxNormCodeEdited(drug.rxNormCode)
        .drugInfoProgressStateLoaded()

    updateSpec
        .given(drugFrequencyChoiceItemsLoaded)
        .whenEvent(DrugFetched(drug))
        .then(
            assertThatNext(
                hasModel(updateModel),
                hasEffects(SetDrugFrequency(frequencyRes), SetDrugDosage(drug.dosage), ShowKeyboard, SetCursorPosition(position))
            )
        )
  }

  @Test
  fun `when drug frequency choice items are loaded, then update the model with a map of frequency to frequency choice items`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(DrugFrequencyChoiceItemsLoaded(drugFrequencyToLabelMap))
        .then(
            assertThatNext(
                hasModel(defaultModel.drugFrequencyToLabelMapLoaded(drugFrequencyToLabelMap))
            )
        )
  }

  @Test
  fun `when done is clicked on the keyboard, then hide the keyboard`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(ImeActionDoneClicked)
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(HideKeyboard)
            )
        )
  }
}
