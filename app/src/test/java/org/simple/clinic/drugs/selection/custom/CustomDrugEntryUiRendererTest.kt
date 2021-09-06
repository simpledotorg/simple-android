package org.simple.clinic.drugs.selection.custom

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.R
import org.simple.clinic.drugs.search.DrugFrequency
import org.simple.clinic.drugs.selection.custom.drugfrequency.country.CommonDrugFrequencyProvider
import org.simple.clinic.drugs.selection.custom.drugfrequency.country.DrugFrequencyChoiceItem
import org.simple.clinic.drugs.selection.custom.drugfrequency.country.DrugFrequencyFactory
import java.util.UUID

class CustomDrugEntryUiRendererTest {

  private val ui = mock<CustomDrugEntryUi>()
  private val uiRenderer = CustomDrugEntryUiRenderer(ui, dosagePlaceholder = "mg")
  private val drugName = "Amlodipine"
  private val dosagePlaceholder = "mg"
  private val drugFrequencyChoiceItems = listOf(
      DrugFrequencyChoiceItem(drugFrequency = null, label = "None"),
      DrugFrequencyChoiceItem(drugFrequency = DrugFrequency.OD, label = "OD"),
      DrugFrequencyChoiceItem(drugFrequency = DrugFrequency.BD, label = "BD"),
      DrugFrequencyChoiceItem(drugFrequency = DrugFrequency.TDS, label = "TDS"),
      DrugFrequencyChoiceItem(drugFrequency = DrugFrequency.QDS, label = "QDS")
  )
  private val defaultModel = CustomDrugEntryModel.default(openAs = OpenAs.New.FromDrugName(drugName), dosagePlaceholder).drugFrequencyChoiceItemsLoaded(drugFrequencyChoiceItems)

  @Test
  fun `when drug dosage focus is changed and dosage is null, then set drug dosage text with the placeholder and move the cursor to the beginning`() {
    // given
    val placeholder = "mg"
    val drugDosageChangedModel = defaultModel.drugNameLoaded(drugName).dosageFocusChanged(hasFocus = true)
    val drugName = "Amlodipine"
    val frequencyLabelResId = R.string.custom_drug_entry_sheet_frequency_none

    // when
    uiRenderer.render(drugDosageChangedModel)

    //then
    verify(ui).hideRemoveButton()
    verify(ui).setButtonTextAsAdd()
    verify(ui).setDrugDosageText(placeholder)
    verify(ui).moveDrugDosageCursorToBeginning()
    verify(ui).setSheetTitle(drugName, null, frequencyLabelResId)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when drug dosage focus is changed and dosage is not null but only contains the placeholder, then set drug dosage text as an empty string and update the sheet title`() {
    // given
    val dosageText = "mg"
    val drugDosageChangedModel = defaultModel.drugNameLoaded(drugName).dosageEdited(dosageText).dosageFocusChanged(hasFocus = false).drugFrequencyChoiceItemsLoaded(drugFrequencyChoiceItems)
    val frequencyLabelResId = R.string.custom_drug_entry_sheet_frequency_none

    // when
    uiRenderer.render(drugDosageChangedModel)

    //then
    verify(ui).hideRemoveButton()
    verify(ui).setButtonTextAsAdd()
    verify(ui).setDrugDosageText("")
    verify(ui).setSheetTitle(drugName, dosageText, frequencyLabelResId)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the screen is loaded in update mode, then render the drug name and setup ui for updating drug entry`() {
    // given
    val prescribedDrugUuid = UUID.fromString("96633994-6e4d-4528-b796-f03ae016553a")
    val defaultModel = CustomDrugEntryModel.default(openAs = OpenAs.Update(prescribedDrugUuid), dosagePlaceholder).drugFrequencyChoiceItemsLoaded(drugFrequencyChoiceItems)
    val frequencyLabelResId = R.string.custom_drug_entry_sheet_frequency_none

    // when
    uiRenderer.render(defaultModel)

    // then
    verify(ui).showRemoveButton()
    verify(ui).setButtonTextAsSave()
    verify(ui).setSheetTitle(null, null, frequencyLabelResId)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when drug name, dosage and frequency is loaded, then update the sheet title`() {
    val drugDosage = "15mg"
    val frequency = DrugFrequency.OD
    val frequencyLabelResId = R.string.custom_drug_entry_sheet_frequency_OD

    // when
    uiRenderer.render(defaultModel.drugNameLoaded(drugName).dosageEdited(drugDosage).frequencyEdited(frequency))

    // then
    verify(ui).hideRemoveButton()
    verify(ui).setButtonTextAsAdd()
    verify(ui).setSheetTitle(drugName, drugDosage, frequencyLabelResId)
    verifyNoMoreInteractions(ui)
  }
}
