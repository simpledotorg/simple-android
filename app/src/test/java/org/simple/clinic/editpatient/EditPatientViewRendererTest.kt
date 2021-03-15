package org.simple.clinic.editpatient

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility.DATE_OF_BIRTH_VISIBLE
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

class EditPatientViewRendererTest {
  @Test
  fun `when edit patient screen has colony or villages list, then show them in the screen`() {
    // given
    val dateOfBirthFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)

    val patientProfile = TestData.patientProfile(
        patientUuid = UUID.fromString("f8193c3b-20d3-4fae-be3a-0029969db624"),
        patientAddressUuid = UUID.fromString("b7119eca-c3ab-4f28-ba88-038f737687d9"),
        generatePhoneNumber = true,
        generateBusinessId = true,
    )

    val model = EditPatientModel.from(
        patient = patientProfile.patient,
        address = patientProfile.address,
        phoneNumber = patientProfile.phoneNumbers.first(),
        bangladeshNationalId = null,
        saveButtonState = EditPatientState.SAVING_PATIENT,
        dateOfBirthFormatter = dateOfBirthFormat
    )

    val ui = mock<EditPatientUi>()
    val uiRenderer = EditPatientViewRenderer(ui)

    val colonyOrVillages = listOf("colony1", "colony2", "colony3", "colony4")
    val colonyOrVillagesFetchedState = model.updateColonyOrVillagesList(colonyOrVillages).updateDateOfBirth("2018-03-09")

    // when
    uiRenderer.render(colonyOrVillagesFetchedState)

    // then
    verify(ui).setDateOfBirthAndAgeVisibility(DATE_OF_BIRTH_VISIBLE)
    verify(ui).setColonyOrVillagesAutoComplete(colonyOrVillages)
    verify(ui).showProgress()
    verifyNoMoreInteractions(ui)
  }
}
