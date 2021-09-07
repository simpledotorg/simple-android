package org.simple.clinic.drugs.selection.custom

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.drugs.search.DrugFrequency
import org.simple.clinic.drugs.selection.custom.drugfrequency.country.DrugFrequencyChoiceItem
import java.util.UUID

class CustomDrugEntryUiRendererTest {

  private val ui = mock<CustomDrugEntryUi>()
  private val uiRenderer = CustomDrugEntryUiRenderer(ui, dosagePlaceholder = "mg")
  private val drugName = "Amlodipine"
  private val dosagePlaceholder = "mg"

  private val drugFrequencyToFrequencyChoiceItemMap = mapOf(
      null to DrugFrequencyChoiceItem(drugFrequency = null, label = "None"),
      DrugFrequency.OD to DrugFrequencyChoiceItem(drugFrequency = DrugFrequency.OD, label = "OD"),
      DrugFrequency.BD to DrugFrequencyChoiceItem(drugFrequency = DrugFrequency.BD, label = "BD"),
      DrugFrequency.TDS to DrugFrequencyChoiceItem(drugFrequency = DrugFrequency.TDS, label = "TDS"),
      DrugFrequency.QDS to DrugFrequencyChoiceItem(drugFrequency = DrugFrequency.QDS, label = "QDS")
  )
  
  private val defaultModel = CustomDrugEntryModel.default(openAs = OpenAs.New.FromDrugName(drugName), dosagePlaceholder).drugFrequencyToFrequencyChoiceItemMapLoaded(drugFrequencyToFrequencyChoiceItemMap)

  @Test
  fun `when drug dosage focus is changed and dosage is null, then set drug dosage text with the placeholder and move the cursor to the beginning`() {
    // given
    val placeholder = "mg"
    val drugDosageChangedModel = defaultModel.drugNameLoaded(drugName).dosageFocusChanged(hasFocus = true)
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
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when drug dosage focus is changed and dosage is not null but only contains the placeholder, then set drug dosage text as an empty string and update the sheet title`() {
    // given
    val dosageText = "mg"
    val drugDosageChangedModel = defaultModel.drugNameLoaded(drugName).dosageEdited(dosageText).dosageFocusChanged(hasFocus = false)
    val frequencyLabel = "None"

    // when
    uiRenderer.render(drugDosageChangedModel)

    //then
    verify(ui).hideRemoveButton()
    verify(ui).setButtonTextAsAdd()
    verify(ui).setDrugDosageText("")
    verify(ui).setSheetTitle(drugName, dosageText, frequencyLabel)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the screen is loaded in update mode, then render the drug name and setup ui for updating drug entry`() {
    // given
    val prescribedDrugUuid = UUID.fromString("96633994-6e4d-4528-b796-f03ae016553a")
    val defaultModel = CustomDrugEntryModel.default(openAs = OpenAs.Update(prescribedDrugUuid), dosagePlaceholder).drugFrequencyToFrequencyChoiceItemMapLoaded(drugFrequencyToFrequencyChoiceItemMap)
    val frequencyLabel = "None"

    // when
    uiRenderer.render(defaultModel)

    // then
    verify(ui).showRemoveButton()
    verify(ui).setButtonTextAsSave()
    verify(ui).setSheetTitle(null, null, frequencyLabel)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when drug name, dosage and frequency is loaded, then update the sheet title`() {
    val drugDosage = "15mg"
    val frequency = DrugFrequency.OD
    val frequencyLabel = "OD"

    // when
    uiRenderer.render(defaultModel.drugNameLoaded(drugName).dosageEdited(drugDosage).frequencyEdited(frequency))

    // then
    verify(ui).hideRemoveButton()
    verify(ui).setButtonTextAsAdd()
    verify(ui).setSheetTitle(drugName, drugDosage, frequencyLabel)
    verifyNoMoreInteractions(ui)
  }
}
