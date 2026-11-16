package org.simple.clinic.editpatient

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.NextMatchers.hasNothing
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
import org.simple.clinic.scanid.OpenedFrom.EditPatientScreen.ToAddShrId
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

  private val inputFieldsFactory = InputFieldsFactory(BangladeshInputFieldsProvider())

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
    isUserCountryBangladesh = false,
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
        .whenEvent(PatientIdentifiersFetched(bpPassports, null))
        .then(assertThatNext(
            hasModel(model.patientIdentifiersLoaded(bpPassports, null)),
            hasNoEffects()
        ))
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

  private val bangladeshModel = EditPatientModel.from(
      patient = patientProfile.patient,
      address = patientProfile.address,
      phoneNumber = patientProfile.phoneNumbers.first(),
      dateOfBirthFormatter = dateOfBirthFormat,
      bangladeshNationalId = null,
      saveButtonState = EditPatientState.NOT_SAVING_PATIENT,
      isUserCountryBangladesh = true,
  )

  private val bangladeshModelWithIdentifiersLoaded = bangladeshModel.patientIdentifiersLoaded(emptyList(), null)

  private val savedShrId = TestData.businessId(
      uuid = UUID.fromString("5a6c55d4-4c7b-4f86-8a7e-1d0e6bb5f0a3"),
      identifier = Identifier("91234567890", Identifier.IdentifierType.BangladeshSharedHealthRecordId)
  )

  private val newShrId = Identifier("98146050888", Identifier.IdentifierType.BangladeshSharedHealthRecordId)

  @Test
  fun `when patient identifiers are loaded for the first time, then set the saved SHR ID on the entries`() {
    val bpPassports = listOf(
        TestData.businessId(
            uuid = UUID.fromString("0c8b3b47-2a52-4c9e-9f0b-7d1a0f6b0d11"),
            identifier = Identifier("8ccc4438-8158-41f9-9a3a-a96e4a221aa0", Identifier.IdentifierType.BpPassport)
        )
    )

    val expectedModel = bangladeshModel.copy(
        savedEntry = bangladeshModel.savedEntry.updateShrId(savedShrId.identifier),
        ongoingEntry = bangladeshModel.ongoingEntry.updateShrId(savedShrId.identifier),
        bpPassports = bpPassports,
        savedShrId = savedShrId
    )

    updateSpec
        .given(bangladeshModel)
        .whenEvent(PatientIdentifiersFetched(bpPassports, savedShrId))
        .then(assertThatNext(
            hasModel(expectedModel),
            hasNoEffects()
        ))
  }

  @Test
  fun `when patient identifiers are loaded again after an SHR ID was added, then keep the added SHR ID`() {
    val modelWithAddedShrId = bangladeshModelWithIdentifiersLoaded.addShrId(newShrId)

    updateSpec
        .given(modelWithAddedShrId)
        .whenEvent(PatientIdentifiersFetched(emptyList(), null))
        .then(assertThatNext(
            hasModel(modelWithAddedShrId.copy(bpPassports = emptyList(), savedShrId = null)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when add SHR ID button is clicked, then open scan simple id screen to add SHR ID`() {
    updateSpec
        .given(bangladeshModelWithIdentifiersLoaded)
        .whenEvent(AddShrIdButtonClicked())
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenSimpleScanIdScreen(ToAddShrId))
        ))
  }

  @Test
  fun `when add SHR ID button is clicked and the user country is not Bangladesh, then do nothing`() {
    updateSpec
        .given(model.patientIdentifiersLoaded(emptyList(), null))
        .whenEvent(AddShrIdButtonClicked())
        .then(assertThatNext(hasNothing()))
  }

  @Test
  fun `when add SHR ID button is clicked and the patient already has an SHR ID, then do nothing`() {
    updateSpec
        .given(bangladeshModel.patientIdentifiersLoaded(emptyList(), savedShrId))
        .whenEvent(AddShrIdButtonClicked())
        .then(assertThatNext(hasNothing()))
  }

  @Test
  fun `when a valid SHR ID is added, then update the model with the SHR ID`() {
    updateSpec
        .given(bangladeshModelWithIdentifiersLoaded)
        .whenEvent(ShrIdAdded(newShrId))
        .then(assertThatNext(
            hasModel(bangladeshModelWithIdentifiersLoaded.addShrId(newShrId)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when an SHR ID with an invalid value is added, then do nothing`() {
    val invalidShrIds = listOf("88146050888", "9814605088", "981460508881", "9814605088a")

    invalidShrIds.forEach { value ->
      updateSpec
          .given(bangladeshModelWithIdentifiersLoaded)
          .whenEvent(ShrIdAdded(Identifier(value, Identifier.IdentifierType.BangladeshSharedHealthRecordId)))
          .then(assertThatNext(hasNothing()))
    }
  }

  @Test
  fun `when an identifier of another type is added as an SHR ID, then do nothing`() {
    updateSpec
        .given(bangladeshModelWithIdentifiersLoaded)
        .whenEvent(ShrIdAdded(Identifier("98146050888", Identifier.IdentifierType.BangladeshNationalId)))
        .then(assertThatNext(hasNothing()))
  }

  @Test
  fun `when an SHR ID is added and the patient already has an SHR ID, then do not add a duplicate`() {
    val modelWithSavedShrId = bangladeshModel.patientIdentifiersLoaded(emptyList(), savedShrId)

    updateSpec
        .given(modelWithSavedShrId)
        .whenEvent(ShrIdAdded(savedShrId.identifier))
        .then(assertThatNext(hasNothing()))

    updateSpec
        .given(modelWithSavedShrId)
        .whenEvent(ShrIdAdded(newShrId))
        .then(assertThatNext(hasNothing()))
  }

  @Test
  fun `when remove SHR ID is clicked, then remove the SHR ID from the ongoing entry`() {
    val modelWithSavedShrId = bangladeshModel.patientIdentifiersLoaded(emptyList(), savedShrId)

    updateSpec
        .given(modelWithSavedShrId)
        .whenEvent(RemoveShrIdClicked)
        .then(assertThatNext(
            hasModel(modelWithSavedShrId.removeShrId()),
            hasNoEffects()
        ))
  }

  @Test
  fun `when both a bp passport and an SHR ID are added, then keep both in the model`() {
    val bpPassport = Identifier("a26d7480-ac79-4de6-a120-15e2bdbdc6e7", Identifier.IdentifierType.BpPassport)
    val modelWithShrId = bangladeshModelWithIdentifiersLoaded.addShrId(newShrId)

    updateSpec
        .given(modelWithShrId)
        .whenEvent(BpPassportAdded(listOf(bpPassport)))
        .then(assertThatNext(
            hasModel(modelWithShrId.addBpPassports(listOf(bpPassport))),
            hasNoEffects()
        ))
  }

  @Test
  fun `when back is clicked after removing the saved SHR ID, then show discard changes alert`() {
    val modelWithRemovedShrId = bangladeshModel
        .patientIdentifiersLoaded(emptyList(), savedShrId)
        .removeShrId()

    updateSpec
        .given(modelWithRemovedShrId)
        .whenEvent(BackClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowDiscardChangesAlertEffect)
        ))
  }

  @Test
  fun `when back is clicked after loading the saved SHR ID without changes, then go back`() {
    updateSpec
        .given(bangladeshModel.patientIdentifiersLoaded(emptyList(), savedShrId))
        .whenEvent(BackClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(GoBackEffect)
        ))
  }
}
