package org.simple.clinic.newentry

import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.newentry.country.BangladeshInputFieldsProvider
import org.simple.clinic.newentry.country.InputFields
import org.simple.clinic.newentry.country.InputFieldsFactory
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BangladeshSharedHealthRecordId
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport

class PatientEntryUiRendererTest {

  private val inputFieldsFactory = InputFieldsFactory(BangladeshInputFieldsProvider())

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
    verify(ui).setColonyOrVillagesAutoComplete(colonyOrVillages)
    verify(ui).hideIdentifierSection()
    verify(ui).hideShrId()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when edit patient screen has input fields, then show them in the screen`() {
    // given
    val model = PatientEntryModel.DEFAULT

    val ui = mock<PatientEntryUi>()
    val uiRenderer = PatientEntryUiRenderer(ui)

    val inputFields = InputFields(inputFieldsFactory.provideFields())
    val inputFieldsFetchedState = model.inputFieldsLoaded(inputFields)

    // when
    uiRenderer.render(inputFieldsFetchedState)

    // then
    verify(ui).setupUi(inputFields)
    verify(ui).hideIdentifierSection()
    verify(ui).hideShrId()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the patient entry has an SHR ID, then display it`() {
    // given
    val shrId = Identifier("91234567890", BangladeshSharedHealthRecordId)
    val model = PatientEntryModel.DEFAULT
        .copy(isSharedHealthRecordEnabled = true)
        .shrIdAdded(shrId)

    val ui = mock<PatientEntryUi>()
    val uiRenderer = PatientEntryUiRenderer(ui)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).hideIdentifierSection()
    verify(ui).displayShrId(shrId)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when SHR is enabled and the patient entry has no SHR ID, then show the add SHR ID button`() {
    // given
    val model = PatientEntryModel.DEFAULT.copy(isSharedHealthRecordEnabled = true)

    val ui = mock<PatientEntryUi>()
    val uiRenderer = PatientEntryUiRenderer(ui)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).hideIdentifierSection()
    verify(ui).showAddShrIdButton()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the patient entry has both a bp passport and an SHR ID, then display both`() {
    // given
    val bpPassport = Identifier("9ba2d8c1-5c4e-4a6f-8e2d-1f0b7a3c5d91", BpPassport)
    val shrId = Identifier("91234567890", BangladeshSharedHealthRecordId)
    val model = PatientEntryModel.DEFAULT
        .copy(isSharedHealthRecordEnabled = true)
        .patientEntryFetched(OngoingNewPatientEntry().withIdentifier(bpPassport).withShrId(shrId))

    val ui = mock<PatientEntryUi>()
    val uiRenderer = PatientEntryUiRenderer(ui)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).showIdentifierSection(bpPassport)
    verify(ui).displayShrId(shrId)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the screen is recreated, then render the bp passport again`() {
    // given
    val bpPassport = Identifier("9ba2d8c1-5c4e-4a6f-8e2d-1f0b7a3c5d91", BpPassport)
    val model = PatientEntryModel.DEFAULT
        .patientEntryFetched(OngoingNewPatientEntry().withIdentifier(bpPassport))

    val uiBeforeRecreation = mock<PatientEntryUi>()
    val uiAfterRecreation = mock<PatientEntryUi>()

    // when
    PatientEntryUiRenderer(uiBeforeRecreation).render(model)
    PatientEntryUiRenderer(uiAfterRecreation).render(model)

    // then
    verify(uiBeforeRecreation).showIdentifierSection(bpPassport)
    verify(uiAfterRecreation).showIdentifierSection(bpPassport)
  }
}
