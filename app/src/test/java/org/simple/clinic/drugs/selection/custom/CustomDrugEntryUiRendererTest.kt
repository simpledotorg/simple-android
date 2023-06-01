package org.simple.clinic.drugs.selection.custom

import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.drugs.search.DrugFrequency
import org.simple.clinic.drugs.selection.custom.ButtonState.SAVING
import org.simple.clinic.drugs.selection.custom.drugfrequency.country.DrugFrequencyLabel
import java.util.UUID

class CustomDrugEntryUiRendererTest {

  private val ui = mock<CustomDrugEntryUi>()
  private val uiRenderer = CustomDrugEntryUiRenderer(ui, dosagePlaceholder = "mg")
  private val drugName = "Amlodipine"
  private val dosagePlaceholder = "mg"

  private val drugFrequencyToLabelMap = mapOf(
      null to DrugFrequencyLabel(label = "None"),
      DrugFrequency.OD to DrugFrequencyLabel(label = "OD"),
      DrugFrequency.BD to DrugFrequencyLabel(label = "BD"),
      DrugFrequency.TDS to DrugFrequencyLabel(label = "TDS"),
      DrugFrequency.QDS to DrugFrequencyLabel(label = "QDS")
  )

  private val defaultModel = CustomDrugEntryModel
      .default(openAs = OpenAs.New.FromDrugName(drugName), dosagePlaceholder)
      .drugFrequencyToLabelMapLoaded(drugFrequencyToLabelMap)

  @Test
  fun `when drug dosage focus is changed and dosage is null, then set drug dosage text with the placeholder and move the cursor to the beginning`() {
    // given
    val placeholder = "mg"
    val drugDosageChangedModel = defaultModel.drugNameLoaded(drugName).dosageFocusChanged(hasFocus = true).drugInfoProgressStateLoaded()
    val drugName = "Amlodipine"
    val frequencyLabel = "None"

    // when
    uiRenderer.render(drugDosageChangedModel)

    //then
    verify(ui).hideRemoveButton()
    verify(ui).setButtonTextAsAdd()
    verify(ui).setDrugDosageText(placeholder)
    verify(ui).moveDrugDosageCursorToBeginning()
    verify(ui).setSheetTitle(drugName, null, frequencyLabel)
    verify(ui).hideProgressBar()
    verify(ui).showCustomDrugEntryUi()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when drug dosage focus is changed and dosage is not null but only contains the placeholder, then set drug dosage text as an empty string and update the sheet title`() {
    // given
    val dosageText = "mg"
    val drugDosageChangedModel = defaultModel
        .drugNameLoaded(drugName)
        .dosageEdited(dosageText)
        .dosageFocusChanged(hasFocus = false)
        .drugInfoProgressStateLoaded()
    val frequencyLabel = "None"

    // when
    uiRenderer.render(drugDosageChangedModel)

    //then
    verify(ui).hideRemoveButton()
    verify(ui).setButtonTextAsAdd()
    verify(ui).setDrugDosageText("")
    verify(ui).setSheetTitle(drugName, dosageText, frequencyLabel)
    verify(ui).hideProgressBar()
    verify(ui).showCustomDrugEntryUi()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the screen is loaded in update mode, then render the drug name and setup ui for updating drug entry`() {
    // given
    val prescribedDrugUuid = UUID.fromString("96633994-6e4d-4528-b796-f03ae016553a")
    val defaultModel = CustomDrugEntryModel
        .default(openAs = OpenAs.Update(prescribedDrugUuid), dosagePlaceholder)
        .drugFrequencyToLabelMapLoaded(drugFrequencyToLabelMap)
        .drugInfoProgressStateLoaded()
    val frequencyLabel = "None"

    // when
    uiRenderer.render(defaultModel)

    // then
    verify(ui).showRemoveButton()
    verify(ui).setButtonTextAsSave()
    verify(ui).setSheetTitle(null, null, frequencyLabel)
    verify(ui).hideProgressBar()
    verify(ui).showCustomDrugEntryUi()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when drug name, dosage and frequency is loaded, then update the sheet title`() {
    val drugDosage = "15mg"
    val frequency = DrugFrequency.OD
    val frequencyLabel = "OD"

    // when
    uiRenderer.render(defaultModel.drugNameLoaded(drugName).dosageEdited(drugDosage).frequencyEdited(frequency).drugInfoProgressStateLoaded())

    // then
    verify(ui).hideRemoveButton()
    verify(ui).setButtonTextAsAdd()
    verify(ui).setSheetTitle(drugName, drugDosage, frequencyLabel)
    verify(ui).hideProgressBar()
    verify(ui).showCustomDrugEntryUi()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when custom drug entry sheet info is not loaded, then show progress bar and hide custom drug entry sheet ui`() {
    // when
    uiRenderer.render(defaultModel.drugInfoProgressStateLoading())

    // then
    verify(ui).showProgressBar()
    verify(ui).hideCustomDrugEntryUi()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when custom drug entry sheet info is loaded, then hide progress bar, show custom drug entry ui and show keyboard`() {
    // given
    val frequencyLabel = "None"

    // when
    uiRenderer.render(defaultModel.drugInfoProgressStateLoaded())

    // then
    verify(ui).hideProgressBar()
    verify(ui).showCustomDrugEntryUi()
    verify(ui).hideRemoveButton()
    verify(ui).setButtonTextAsAdd()
    verify(ui).setSheetTitle(null, null, frequencyLabel)
    verifyNoMoreInteractions(ui)
  }


  @Test
  fun `when add button is clicked and info is being added or updated, then show progress state in the save button`() {
    // given
    val frequencyLabel = "None"

    // when
    uiRenderer.render(defaultModel.drugInfoProgressStateLoaded().saveButtonStateChanged(SAVING))

    // then
    verify(ui).hideProgressBar()
    verify(ui).showCustomDrugEntryUi()
    verify(ui).hideRemoveButton()
    verify(ui).setButtonTextAsAdd()
    verify(ui).setSheetTitle(null, null, frequencyLabel)
    verify(ui).showSaveButtonProgressState()
    verifyNoMoreInteractions(ui)
  }
}
