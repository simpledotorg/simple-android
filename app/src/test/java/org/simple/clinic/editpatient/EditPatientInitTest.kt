package org.simple.clinic.editpatient

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import org.simple.clinic.TestData
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

class EditPatientInitTest {
  private val patientUuid = UUID.fromString("e40f42f4-0867-4891-ac77-95df5fe1fdef")
  private val patient = TestData.patient(patientUuid, fullName = "TestName")
  private val patientAddress = TestData.patientAddress(UUID.fromString("0d19e592-ae4d-4d4f-8881-8764709a17dd"), streetAddress = "Street Address", colonyOrVillage = "Colony")
  private val patientPhoneNumber = TestData.patientPhoneNumber(UUID.fromString("f204b770-83c5-4145-9ca7-c2273be2bbdc"), number = "9999999999")
  private val bangladeshNationalId = TestData.businessId(UUID.fromString("c9be5e5d-770c-4f37-a9de-2c304dfebfcd"))
  private val dateOfBirthFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)
  private val initSpec = InitSpec(EditPatientInit(patient = patient,
      address = patientAddress,
      phoneNumber = patientPhoneNumber,
      bangladeshNationalId = bangladeshNationalId,
      isVillageTypeAheadEnabled = true))

  @Test
  fun `when screen is created, then load initial data`() {
    val patientUuid = UUID.fromString("e40f42f4-0867-4891-ac77-95df5fe1fdef")
    val defaultModel = EditPatientModel.from(patient, patientAddress, patientPhoneNumber, dateOfBirthFormat, bangladeshNationalId, EditPatientState.NOT_SAVING_PATIENT)

    initSpec.whenInit(defaultModel).then(assertThatFirst(
        hasModel(defaultModel),
        hasEffects(PrefillFormEffect(patient, patientAddress, patientPhoneNumber, bangladeshNationalId),
            FetchBpPassportsEffect(patientUuid),
            LoadInputFields,
            FetchColonyOrVillagesEffect
        )
    ))
  }

  @Test
  fun `when screen is created and village type ahead is not enabled, then do not fetch colony or villages`() {
    val patientUuid = UUID.fromString("e40f42f4-0867-4891-ac77-95df5fe1fdef")
    val defaultModel = EditPatientModel.from(patient, patientAddress, patientPhoneNumber, dateOfBirthFormat, bangladeshNationalId, EditPatientState.NOT_SAVING_PATIENT)
    val initSpec = InitSpec(EditPatientInit(patient = patient,
        address = patientAddress,
        phoneNumber = patientPhoneNumber,
        bangladeshNationalId = bangladeshNationalId,
        isVillageTypeAheadEnabled = false))

    initSpec.whenInit(defaultModel).then(assertThatFirst(
        hasModel(defaultModel),
        hasEffects(PrefillFormEffect(patient, patientAddress, patientPhoneNumber, bangladeshNationalId),
            FetchBpPassportsEffect(patientUuid),
            LoadInputFields
        )
    ))
  }

  @Test
  fun `when screen is restored, then don't fetch colony or villages`() {
    val colonyOrVillages = listOf("Colony1", "Colony2", "Colony3", "Colony4")

    val updatedVillageOrColonyNamesModel = EditPatientModel.from(patient, patientAddress, patientPhoneNumber, dateOfBirthFormat, bangladeshNationalId, EditPatientState.NOT_SAVING_PATIENT).updateColonyOrVillagesList(colonyOrVillages)

    initSpec.whenInit(updatedVillageOrColonyNamesModel).then(assertThatFirst(
        hasModel(updatedVillageOrColonyNamesModel),
        hasEffects(PrefillFormEffect(patient, patientAddress, patientPhoneNumber, bangladeshNationalId),
            FetchBpPassportsEffect(patientUuid),
            LoadInputFields
        )
    ))
  }

}
