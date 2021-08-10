package org.simple.clinic.drugs.selection.custom

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import java.util.UUID

class CustomDrugEntryUiRendererTest {

  private val ui = mock<CustomDrugEntryUi>()
  private val uiRenderer = CustomDrugEntryUiRenderer(ui, dosagePlaceholder = "mg")
  private val drugName = "Amlodipine"
  private val patientUuid = UUID.fromString("77f1d870-5c60-49f7-a4e2-2f1d60e4218c")
  private val defaultModel = CustomDrugEntryModel.default(openAs = OpenAs.New.FromDrugName(patientUuid, drugName))

  @Test
  fun `when drug dosage focus is changed and dosage is null, then set drug dosage text with the placeholder and move the cursor to the beginning`() {
    // given
    val placeholder = "mg"
    val drugDosageChangedModel = defaultModel.dosageFocusChanged(hasFocus = true)

    // when
    uiRenderer.render(drugDosageChangedModel)

    //then
    verify(ui).hideRemoveButton()
    verify(ui).setButtonTextAsAdd()
    verify(ui).setDrugDosageText(placeholder)
    verify(ui).moveDrugDosageCursorToBeginning()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when drug dosage focus is changed and dosage is not null but only contains the placeholder, then set drug dosage text as an empty string`() {
    // given
    val dosageText = "mg"
    val drugDosageChangedModel = defaultModel.dosageEdited(dosageText).dosageFocusChanged(hasFocus = false)

    // when
    uiRenderer.render(drugDosageChangedModel)

    //then
    verify(ui).hideRemoveButton()
    verify(ui).setButtonTextAsAdd()
    verify(ui).setDrugDosageText("")
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the screen is loaded in update mode, then render the drug name and setup ui for updating drug entry`() {
    // given
    val prescribedDrugUuid = UUID.fromString("96633994-6e4d-4528-b796-f03ae016553a")
    val defaultModel = CustomDrugEntryModel.default(openAs = OpenAs.Update(patientUuid, prescribedDrugUuid), drug = null, drugName = drugName)

    // when
    uiRenderer.render(defaultModel)

    // then
    verify(ui).showRemoveButton()
    verify(ui).setButtonTextAsSave()
    verifyNoMoreInteractions(ui)
  }
}
