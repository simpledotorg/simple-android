package org.simple.clinic.editpatient

import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.simple.clinic.newentry.country.BangladeshInputFieldsProvider
import org.simple.clinic.newentry.country.InputFields
import org.simple.clinic.patient.PatientAgeDetails
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BangladeshNationalId
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BangladeshSharedHealthRecordId
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility.DATE_OF_BIRTH_VISIBLE
import org.simple.clinic.TestData
import org.simple.clinic.util.TestUserClock
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

class EditPatientViewRendererTest {

  private val ui = mock<EditPatientUi>()
  private val uiRenderer = EditPatientViewRenderer(ui)

  private val dateOfBirthFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)

  private val patientProfile = TestData.patientProfile(
      patientUuid = UUID.fromString("f8193c3b-20d3-4fae-be3a-0029969db624"),
      patientAddressUuid = UUID.fromString("b7119eca-c3ab-4f28-ba88-038f737687d9"),
      generatePhoneNumber = true,
      generateBusinessId = true,
      generateDateOfBirth = true,
      patientAgeDetails = PatientAgeDetails(
          ageValue = null,
          ageUpdatedAt = null,
          dateOfBirth = LocalDate.parse("1990-03-09")
      )
  )

  private val model = EditPatientModel.from(
      patient = patientProfile.patient,
      address = patientProfile.address,
      phoneNumber = patientProfile.phoneNumbers.first(),
      dateOfBirthFormatter = dateOfBirthFormat,
      bangladeshNationalId = null,
      saveButtonState = EditPatientState.SAVING_PATIENT,
      isUserCountryBangladesh = false,
  )

  @Test
  fun `when edit patient screen has colony or villages list, then show them in the screen`() {
    // given
    val colonyOrVillages = listOf("colony1", "colony2", "colony3", "colony4")
    val colonyOrVillagesFetchedState = model.updateColonyOrVillagesList(colonyOrVillages).updateDateOfBirth("2018-03-09")

    // when
    uiRenderer.render(colonyOrVillagesFetchedState)

    // then
    verify(ui).setPatientName(patientProfile.patient.fullName)
    verify(ui).setGender(patientProfile.patient.gender)
    verify(ui).setState(patientProfile.address.state)
    verify(ui).setDistrict(patientProfile.address.district)
    verify(ui).setStreetAddress(patientProfile.address.streetAddress)
    verify(ui).setZone(patientProfile.address.zone)
    verify(ui).setColonyOrVillage(patientProfile.address.colonyOrVillage!!)
    verify(ui).setPatientPhoneNumber(patientProfile.phoneNumbers.first().number)
    verify(ui).setPatientDateOfBirth("2018-03-09")
    verify(ui).setDateOfBirthAndAgeVisibility(DATE_OF_BIRTH_VISIBLE)
    verify(ui).setColonyOrVillagesAutoComplete(colonyOrVillages)
    verify(ui).showProgress()
    verify(ui).displayBpPassports(emptyList())
    verify(ui).setAlternateIdTextField("")
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when input fields are loaded, then setup the ui`() {
    // given
    val date = LocalDate.parse("2018-01-01")
    val userClock = TestUserClock(date)
    val inputFieldsList = BangladeshInputFieldsProvider()
        .provide()
    val inputFields = InputFields(inputFieldsList)

    val inputFieldsLoadedModel = model.inputFieldsLoaded(inputFields)

    // when
    uiRenderer.render(inputFieldsLoadedModel)

    // then
    verify(ui).setPatientName(patientProfile.patient.fullName)
    verify(ui).setGender(patientProfile.patient.gender)
    verify(ui).setState(patientProfile.address.state)
    verify(ui).setDistrict(patientProfile.address.district)
    verify(ui).setStreetAddress(patientProfile.address.streetAddress)
    verify(ui).setZone(patientProfile.address.zone)
    verify(ui).setColonyOrVillage(patientProfile.address.colonyOrVillage!!)
    verify(ui).setPatientPhoneNumber(patientProfile.phoneNumbers.first().number)
    verify(ui).setPatientDateOfBirth("09/03/1990")
    verify(ui).setDateOfBirthAndAgeVisibility(DATE_OF_BIRTH_VISIBLE)
    verify(ui).showProgress()
    verify(ui).setupUi(inputFields)
    verify(ui).displayBpPassports(emptyList())
    verify(ui).setAlternateIdTextField("")
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when bp passports are loaded, then display identifier`() {
    // given
    val bpPassports = listOf(
        TestData.businessId(
            uuid = UUID.fromString("59b367d7-d8b9-4d8b-b1c5-f702a15bc78d"),
            identifier = Identifier(
                value = "09023191-33dd-4bc8-8e60-c13df9e4764b",
                type = Identifier.IdentifierType.BpPassport
            )
        ),
        TestData.businessId(
            uuid = UUID.fromString("47fde07a-43a7-4233-a01f-ae80dd5b09c8"),
            identifier = Identifier(
                value = "c2b36fa9-2f4d-480e-92af-946bdc432b92",
                type = Identifier.IdentifierType.BpPassport
            )
        )
    )

    val newlyScannedBpPassports = listOf(
        TestData.identifier(
            value = "374488dc-43a0-4a8a-9f3b-619096e95546",
            type = Identifier.IdentifierType.BpPassport
        )
    )

    val bpPassportsLoadedModel = model.bpPassportsLoaded(bpPassports).addBpPassports(newlyScannedBpPassports)

    // when
    uiRenderer.render(bpPassportsLoadedModel)

    // then
    val expectedIdentifiers = listOf(BPPassportListItem("090 2319", false), BPPassportListItem("236 9244", false))
    val expectedNewlyScannedBpPassports = listOf(BPPassportListItem("374 4884", true))
    val expectedBpPassports = expectedIdentifiers + expectedNewlyScannedBpPassports

    verify(ui).setPatientName(patientProfile.patient.fullName)
    verify(ui).setGender(patientProfile.patient.gender)
    verify(ui).setState(patientProfile.address.state)
    verify(ui).setDistrict(patientProfile.address.district)
    verify(ui).setStreetAddress(patientProfile.address.streetAddress)
    verify(ui).setZone(patientProfile.address.zone)
    verify(ui).setColonyOrVillage(patientProfile.address.colonyOrVillage!!)
    verify(ui).setPatientPhoneNumber(patientProfile.phoneNumbers.first().number)
    verify(ui).setPatientDateOfBirth("09/03/1990")
    verify(ui).setDateOfBirthAndAgeVisibility(DATE_OF_BIRTH_VISIBLE)
    verify(ui).showProgress()
    verify(ui).displayBpPassports(expectedBpPassports)
    verify(ui).setAlternateIdTextField("")
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when alternate id is present, then display alternate id`() {
    // given
    val identifier = TestData.identifier(
        value = "1234567",
        type = BangladeshNationalId
    )
    val model = EditPatientModel.from(
        patient = patientProfile.patient,
        address = patientProfile.address,
        phoneNumber = patientProfile.phoneNumbers.first(),
        dateOfBirthFormatter = dateOfBirthFormat,
        bangladeshNationalId = TestData.businessId(
            uuid = UUID.fromString("0244d1f1-ec97-4911-b7a1-3a84032b499c"),
            identifier = identifier
        ),
        saveButtonState = EditPatientState.SAVING_PATIENT,
        isUserCountryBangladesh = false,
    )

    // when
    uiRenderer.render(model)

    // then
    verify(ui).setPatientName(patientProfile.patient.fullName)
    verify(ui).setGender(patientProfile.patient.gender)
    verify(ui).setState(patientProfile.address.state)
    verify(ui).setDistrict(patientProfile.address.district)
    verify(ui).setStreetAddress(patientProfile.address.streetAddress)
    verify(ui).setZone(patientProfile.address.zone)
    verify(ui).setColonyOrVillage(patientProfile.address.colonyOrVillage!!)
    verify(ui).setPatientPhoneNumber(patientProfile.phoneNumbers.first().number)
    verify(ui).setPatientDateOfBirth("09/03/1990")
    verify(ui).setDateOfBirthAndAgeVisibility(DATE_OF_BIRTH_VISIBLE)
    verify(ui).showProgress()
    verify(ui).displayBpPassports(emptyList())
    verify(ui).setAlternateIdTextField("1234567")
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when adding health ids from edit patient is enabled, then show bp passport button and label`() {
    // given
    val model = EditPatientModel.from(
        patient = patientProfile.patient,
        address = patientProfile.address,
        phoneNumber = patientProfile.phoneNumbers.first(),
        dateOfBirthFormatter = dateOfBirthFormat,
        bangladeshNationalId = null,
        saveButtonState = EditPatientState.SAVING_PATIENT,
        isUserCountryBangladesh = false)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).setPatientName(patientProfile.patient.fullName)
    verify(ui).setGender(patientProfile.patient.gender)
    verify(ui).setState(patientProfile.address.state)
    verify(ui).setDistrict(patientProfile.address.district)
    verify(ui).setStreetAddress(patientProfile.address.streetAddress)
    verify(ui).setZone(patientProfile.address.zone)
    verify(ui).setColonyOrVillage(patientProfile.address.colonyOrVillage!!)
    verify(ui).setPatientPhoneNumber(patientProfile.phoneNumbers.first().number)
    verify(ui).setPatientDateOfBirth("09/03/1990")
    verify(ui).setDateOfBirthAndAgeVisibility(DATE_OF_BIRTH_VISIBLE)
    verify(ui).showProgress()
    verify(ui).setAlternateIdTextField("")
    verify(ui).displayBpPassports(emptyList())
    verifyNoMoreInteractions(ui)
  }

  private val bangladeshModel = EditPatientModel.from(
      patient = patientProfile.patient,
      address = patientProfile.address,
      phoneNumber = patientProfile.phoneNumbers.first(),
      dateOfBirthFormatter = dateOfBirthFormat,
      bangladeshNationalId = null,
      saveButtonState = EditPatientState.SAVING_PATIENT,
      isUserCountryBangladesh = true,
  )

  private val savedShrId = TestData.businessId(
      uuid = UUID.fromString("0f7a9d2e-31a8-4c39-9d43-0a4f1e2e9b55"),
      identifier = Identifier("91234567890", BangladeshSharedHealthRecordId)
  )

  private val newShrId = Identifier("98146050888", BangladeshSharedHealthRecordId)

  private fun verifyFormFieldsRendered() {
    verify(ui).setPatientName(patientProfile.patient.fullName)
    verify(ui).setGender(patientProfile.patient.gender)
    verify(ui).setState(patientProfile.address.state)
    verify(ui).setDistrict(patientProfile.address.district)
    verify(ui).setStreetAddress(patientProfile.address.streetAddress)
    verify(ui).setZone(patientProfile.address.zone)
    verify(ui).setColonyOrVillage(patientProfile.address.colonyOrVillage!!)
    verify(ui).setPatientPhoneNumber(patientProfile.phoneNumbers.first().number)
    verify(ui).setPatientDateOfBirth("09/03/1990")
    verify(ui).setDateOfBirthAndAgeVisibility(DATE_OF_BIRTH_VISIBLE)
    verify(ui).showProgress()
    verify(ui).setAlternateIdTextField("")
  }

  @Test
  fun `when the user country is Bangladesh and the patient has no SHR ID, then show the add SHR ID button`() {
    // given
    val model = bangladeshModel.patientIdentifiersLoaded(emptyList(), null)

    // when
    uiRenderer.render(model)

    // then
    verifyFormFieldsRendered()
    verify(ui).displayBpPassports(emptyList())
    verify(ui).showAddShrIdButton()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the user country is Bangladesh and the identifiers are not loaded yet, then hide the SHR ID`() {
    // when
    uiRenderer.render(bangladeshModel)

    // then
    verifyFormFieldsRendered()
    verify(ui).displayBpPassports(emptyList())
    verify(ui).hideShrId()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the patient has a saved SHR ID, then display it without highlight`() {
    // given
    val model = bangladeshModel.patientIdentifiersLoaded(emptyList(), savedShrId)

    // when
    uiRenderer.render(model)

    // then
    verifyFormFieldsRendered()
    verify(ui).displayBpPassports(emptyList())
    verify(ui).displayShrId(savedShrId.identifier, false)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when an SHR ID is newly added, then display it with highlight`() {
    // given
    val model = bangladeshModel
        .patientIdentifiersLoaded(emptyList(), null)
        .addShrId(newShrId)

    // when
    uiRenderer.render(model)

    // then
    verifyFormFieldsRendered()
    verify(ui).displayBpPassports(emptyList())
    verify(ui).displayShrId(newShrId, true)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the saved SHR ID is removed, then show the add SHR ID button`() {
    // given
    val model = bangladeshModel
        .patientIdentifiersLoaded(emptyList(), savedShrId)
        .removeShrId()

    // when
    uiRenderer.render(model)

    // then
    verifyFormFieldsRendered()
    verify(ui).displayBpPassports(emptyList())
    verify(ui).showAddShrIdButton()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the patient has both bp passports and an SHR ID, then display both`() {
    // given
    val bpPassport = TestData.businessId(
        uuid = UUID.fromString("59b367d7-d8b9-4d8b-b1c5-f702a15bc78d"),
        identifier = Identifier("09023191-33dd-4bc8-8e60-c13df9e4764b", Identifier.IdentifierType.BpPassport)
    )
    val newBpPassport = Identifier("374488dc-43a0-4a8a-9f3b-619096e95546", Identifier.IdentifierType.BpPassport)
    val model = bangladeshModel
        .patientIdentifiersLoaded(listOf(bpPassport), savedShrId)
        .addBpPassports(listOf(newBpPassport))

    // when
    uiRenderer.render(model)

    // then
    verifyFormFieldsRendered()
    verify(ui).displayBpPassports(listOf(BPPassportListItem("090\u00a02319", false), BPPassportListItem("374\u00a04884", true)))
    verify(ui).displayShrId(savedShrId.identifier, false)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the user country is not Bangladesh and the patient has no SHR ID, then do not render the SHR ID`() {
    // given
    val model = this.model.patientIdentifiersLoaded(emptyList(), null)

    // when
    uiRenderer.render(model)

    // then
    verifyFormFieldsRendered()
    verify(ui).displayBpPassports(emptyList())
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the user country is not Bangladesh but the patient has an SHR ID, then display it`() {
    // given
    val model = this.model.patientIdentifiersLoaded(emptyList(), savedShrId)

    // when
    uiRenderer.render(model)

    // then
    verifyFormFieldsRendered()
    verify(ui).displayBpPassports(emptyList())
    verify(ui).displayShrId(savedShrId.identifier, false)
    verifyNoMoreInteractions(ui)
  }
}
