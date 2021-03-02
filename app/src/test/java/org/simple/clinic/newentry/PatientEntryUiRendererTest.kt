package org.simple.clinic.newentry

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test

class PatientEntryUiRendererTest {
  @Test
  fun `when edit patient screen has colony or villages list, then show them in the screen`() {
    // given
    val model = PatientEntryModel.DEFAULT

    val ui = mock<PatientEntryUi>()
    val uiRenderer = PatientEntryUiRenderer(ui)

    val colonyOrVillages = listOf("colony1", "colony2", "colony3", "colony4")
    val colonyOrVillagesFetchedState = model.colonyOrVillageListUpdated(colonyOrVillages)

    // when
    uiRenderer.render(colonyOrVillagesFetchedState)

    // then
    verify(ui).showColonyOrVillagesList(colonyOrVillages)
    verify(ui).hideIdentifierSection()
    verifyNoMoreInteractions(ui)
  }
}
