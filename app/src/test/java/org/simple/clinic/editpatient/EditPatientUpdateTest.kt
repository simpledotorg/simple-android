package org.simple.clinic.editpatient

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.newentry.country.BangladeshInputFieldsProvider
import org.simple.clinic.newentry.country.InputFields
import org.simple.clinic.newentry.country.InputFieldsFactory
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.registration.phone.PhoneNumberValidator
import org.simple.clinic.scanid.OpenedFrom
import org.simple.clinic.scanid.OpenedFrom.EditPatientScreen.ToAddBpPassport
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.widgets.ageanddateofbirth.UserInputAgeValidator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

class EditPatientUpdateTest {
  private val clock = TestUserClock(LocalDate.parse("2018-01-01"))
  private val dateOfBirthFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)

  private val numberValidator = PhoneNumberValidator(minimumRequiredLength = 6)
  private val dobValidator = UserInputDateValidator(
      userClock = clock,
      dateOfBirthFormat = dateOfBirthFormat
  )
  private val ageValidator = UserInputAgeValidator(
      userClock = clock,
      dateOfBirthFormat = dateOfBirthFormat
  )
  private val updateSpec = UpdateSpec(EditPatientUpdate(
      numberValidator = numberValidator,
      dobValidator = dobValidator,
      ageValidator = ageValidator
  ))

  private val inputFieldsFactory = InputFieldsFactory(BangladeshInputFieldsProvider(
      dateTimeFormatter = dateOfBirthFormat,
      today = LocalDate.now(clock)
  ))

  private val patientProfile = TestData.patientProfile(
      patientUuid = UUID.fromString("f8193c3b-20d3-4fae-be3a-0029969db624"),
      patientAddressUuid = UUID.fromString("b7119eca-c3ab-4f28-ba88-038f737687d9"),
      generatePhoneNumber = true,
      generateBusinessId = true
  )

  private val model = EditPatientModel.from(
    patient = patientProfile.patient,
    address = patientProfile.address,
    phoneNumber = patientProfile.phoneNumbers.first(),
    dateOfBirthFormatter = dateOfBirthFormat,
    bangladeshNationalId = null,
    saveButtonState = EditPatientState.NOT_SAVING_PATIENT,
    isUserCountryIndia = false,
    isAddingHealthIDsFromEditPatientEnabled = false
  )

  @Test
  fun `when the input fields are loaded, the UI must be setup`() {
    val inputFields = InputFields(inputFieldsFactory.provideFields())

    updateSpec
        .given(model)
        .whenEvent(InputFieldsLoaded(inputFields))
        .then(assertThatNext(
            hasModel(model.inputFieldsLoaded(inputFields)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when the colony or villages list is updated, then update the model`() {
    val colonyOrVillages = listOf("colony1", "colony2", "colony3", "colony4")
    updateSpec
        .given(model)
        .whenEvent(ColonyOrVillagesFetched(colonyOrVillages))
        .then(assertThatNext(
            hasModel(model.updateColonyOrVillagesList(colonyOrVillages)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when bp passports are loaded, then update the model`() {
    val bpPassports = listOf(
        TestData.businessId(
            uuid = UUID.fromString("4585acb4-4e8d-418f-a6da-c15ac4b44837"),
            identifier = TestData.identifier(
                value = "8ccc4438-8158-41f9-9a3a-a96e4a221aa0",
                type = Identifier.IdentifierType.BpPassport
            )
        )
    )

    updateSpec
        .given(model)
        .whenEvent(BpPassportsFetched(bpPassports))
        .then(assertThatNext(
            hasModel(model.bpPassportsLoaded(bpPassports)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when add NHID button is clicked, then open scan simple id screen to add NHID`() {
    updateSpec
        .given(model)
        .whenEvent(AddNHIDButtonClicked())
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(OpenSimpleScanIdScreen(OpenedFrom.EditPatientScreen.ToAddNHID))
            )
        )
  }

  @Test
  fun `when add bp passport button is clicked, then open scan simple id screen`() {
    updateSpec
        .given(model)
        .whenEvent(AddBpPassportButtonClicked())
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(OpenSimpleScanIdScreen(ToAddBpPassport))
            )
        )
  }

  @Test
  fun `when bp passport is added, then update the model with bp passport`() {
    val bpPassport1 = Identifier("a26d7480-ac79-4de6-a120-15e2bdbdc6e7", Identifier.IdentifierType.BpPassport)
    val bpPassport2 = Identifier("a26d7480-ac79-4de6-a120-15e2bdbdc6e7", Identifier.IdentifierType.BpPassport)
    val listOfBpPassports = listOf(bpPassport1, bpPassport2)
    updateSpec
        .given(model)
        .whenEvent(BpPassportAdded(listOfBpPassports))
        .then(
            assertThatNext(
                hasModel(model.addBpPassports(listOfBpPassports)),
                hasNoEffects()
            )
        )
  }
}
