package org.simple.clinic.teleconsultlog.prescription

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import java.util.UUID

class TeleconsultPrescriptionUpdateTest {

  private val teleconsultRecordId = UUID.fromString("572be681-770c-41cf-8d4d-4df988e34e72")
  private val patientUuid = UUID.fromString("11c91ee8-2165-4429-962b-70c4951eddd0")
  private val model = TeleconsultPrescriptionModel.create(teleconsultRecordId, patientUuid)

  private val updateSpec = UpdateSpec(TeleconsultPrescriptionUpdate())

  @Test
  fun `when patient details are loaded, then update the model`() {
    val patient = TestData.patient(uuid = patientUuid)

    updateSpec
        .given(model)
        .whenEvent(PatientDetailsLoaded(patient))
        .then(assertThatNext(
            hasModel(model.patientLoaded(patient)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when back is clicked, then go back to previous screen`() {
    updateSpec
        .given(model)
        .whenEvent(BackClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(GoBack)
        ))
  }

  @Test
  fun `when signature is not added then show signature error`() {
    val medicalInstructions = "This is a medical instructions"
    val medicalRegistrationId = "ABC12345"

    updateSpec
        .given(model)
        .whenEvent(DataForNextClickLoaded(
            medicalInstructions = medicalInstructions,
            medicalRegistrationId = medicalRegistrationId,
            hasSignatureBitmap = false
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowSignatureRequiredError)
        ))
  }

  @Test
  fun `when next button is clicked, then load data for next click`() {
    val medicalInstructions = "This is a medical instructions"
    val medicalRegistrationId = "ABC12345"

    updateSpec
        .given(model)
        .whenEvent(NextButtonClicked(medicalInstructions, medicalRegistrationId))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(LoadDataForNextClick(
                teleconsultRecordId = teleconsultRecordId,
                medicalInstructions = medicalInstructions,
                medicalRegistrationId = medicalRegistrationId
            ))
        ))
  }

  @Test
  fun `when prescription is created, then open share prescription screen`() {
    val medicalInstructions = "This is a medical instructions"

    updateSpec
        .given(model)
        .whenEvent(PrescriptionCreated(medicalInstructions))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenSharePrescriptionScreen(
                teleconsultRecordId = teleconsultRecordId,
                medicalInstructions = medicalInstructions
            ))
        ))
  }

  @Test
  fun `when data for next click is loaded, then create prescription`() {
    val medicalInstructions = "This is a medical instructions"
    val medicalRegistrationId = "ABC12345"

    updateSpec
        .given(model)
        .whenEvent(DataForNextClickLoaded(
            medicalInstructions = medicalInstructions,
            medicalRegistrationId = medicalRegistrationId,
            hasSignatureBitmap = true
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(
                SaveMedicalRegistrationId(medicalRegistrationId = medicalRegistrationId),
                UpdateTeleconsultRecordMedicalRegistrationId(
                    teleconsultRecordId = teleconsultRecordId,
                    medicalRegistrationId = medicalRegistrationId
                ),
                AddTeleconsultIdToPrescribedDrugs(
                    patientUuid = patientUuid,
                    teleconsultRecordId = teleconsultRecordId,
                    medicalInstructions = medicalInstructions,
                    medicalRegistrationId = medicalRegistrationId
                )
            )
        ))
  }
}
