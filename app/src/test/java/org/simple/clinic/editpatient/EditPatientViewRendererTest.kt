package org.simple.clinic.editpatient

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.newentry.country.BangladeshInputFieldsProvider
import org.simple.clinic.newentry.country.InputFields
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BangladeshNationalId
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.IndiaNationalHealthId
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility.DATE_OF_BIRTH_VISIBLE
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
      dateOfBirth = LocalDate.parse("1990-03-09")
  )

  private val model = EditPatientModel.from(
      patient = patientProfile.patient,
      address = patientProfile.address,
      phoneNumber = patientProfile.phoneNumbers.first(),
      dateOfBirthFormatter = dateOfBirthFormat,
      bangladeshNationalId = null,
      saveButtonState = EditPatientState.SAVING_PATIENT,
      isUserCountryIndia = false,
      isAddingHealthIDsFromEditPatientEnabled = false
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
    val inputFieldsList = BangladeshInputFieldsProvider(dateOfBirthFormat, LocalDate.now(userClock))
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
    val bpPassportsLoadedModel = model.bpPassportsLoaded(bpPassports)

    // when
    uiRenderer.render(bpPassportsLoadedModel)

    // then
    val expectedIdentifiers = listOf("090 2319", "236 9244")

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
    verify(ui).displayBpPassports(expectedIdentifiers)
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
        isUserCountryIndia = false,
        isAddingHealthIDsFromEditPatientEnabled = false
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
  fun `when indian national health id is present, then display alternate id container`() {
    // given
    val identifier = TestData.identifier(
        value = "1234567",
        type = IndiaNationalHealthId
    )
    val model = EditPatientModel.from(
        patient = patientProfile.patient,
        address = patientProfile.address,
        phoneNumber = patientProfile.phoneNumbers.first(),
        dateOfBirthFormatter = dateOfBirthFormat,
        bangladeshNationalId = TestData.businessId(
            uuid = UUID.fromString("ed74922a-3909-4084-a667-1f2bf1c36322"),
            identifier = identifier
        ),
        saveButtonState = EditPatientState.SAVING_PATIENT,
        isUserCountryIndia = false,
        isAddingHealthIDsFromEditPatientEnabled = false
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
    verify(ui).setAlternateIdContainer(identifier, false)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when NHID is added after qr code scan and feature flag is enabled, then display NHID with highligh in the ui and hide add NHID button`() {
    // given
    val indiaNHID = "23432123456434"
    val identifier = Identifier(indiaNHID, IndiaNationalHealthId)
    val model = EditPatientModel.from(
        patient = patientProfile.patient,
        address = patientProfile.address,
        phoneNumber = patientProfile.phoneNumbers.first(),
        dateOfBirthFormatter = dateOfBirthFormat,
        bangladeshNationalId = null,
        saveButtonState = EditPatientState.SAVING_PATIENT,
        isUserCountryIndia = true,
        isAddingHealthIDsFromEditPatientEnabled = true
    ).updateAlternativeId(indiaNHID)

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
    verify(ui).setAlternateIdContainer(identifier, true)
    verify(ui).hideAddNHIDButton()
    verify(ui).showBPPassportButton()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when there is no NHID in edit screen, the user country is India and feature flag is enabled, then show add NHID button`() {
    // given
    val model = EditPatientModel.from(
        patient = patientProfile.patient,
        address = patientProfile.address,
        phoneNumber = patientProfile.phoneNumbers.first(),
        dateOfBirthFormatter = dateOfBirthFormat,
        bangladeshNationalId = null,
        saveButtonState = EditPatientState.SAVING_PATIENT,
        isUserCountryIndia = true,
        isAddingHealthIDsFromEditPatientEnabled = true
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
    verify(ui).showAddNHIDButton()
    verify(ui).showIndiaNHIDLabel()
    verify(ui).showBPPassportButton()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when adding health ids from edit patient is enabled, then show bp passport button`() {
    // given
    val model = EditPatientModel.from(
        patient = patientProfile.patient,
        address = patientProfile.address,
        phoneNumber = patientProfile.phoneNumbers.first(),
        dateOfBirthFormatter = dateOfBirthFormat,
        bangladeshNationalId = null,
        saveButtonState = EditPatientState.SAVING_PATIENT,
        isUserCountryIndia = false,
        isAddingHealthIDsFromEditPatientEnabled = true)

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
    verify(ui).showBPPassportButton()
    verifyNoMoreInteractions(ui)
  }
}
