package org.simple.clinic.drugs.selection.custom

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test

class CustomDrugEntryUiRendererTest {

  private val ui = mock<CustomDrugEntryUi>()
  private val uiRenderer = CustomDrugEntryUiRenderer(ui, dosagePlaceholder = "mg")

  private val defaultModel = CustomDrugEntryModel.default()

  @Test
  fun `when drug dosage focus is changed and dosage is null, then set drug dosage text with the placeholder and move the cursor to the beginning`() {
    // given
    val placeholder = "mg"
    val drugDosageChangedModel = defaultModel.dosageFocusChanged(hasFocus = true)

    // when
    uiRenderer.render(drugDosageChangedModel)

    //then
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
    verify(ui).setDrugDosageText("")
    verifyNoMoreInteractions(ui)
  }
}
