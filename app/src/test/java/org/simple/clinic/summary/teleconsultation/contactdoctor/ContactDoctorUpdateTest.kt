package org.simple.clinic.summary.teleconsultation.contactdoctor

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import java.util.UUID

class ContactDoctorUpdateTest {

  private val patientUuid = UUID.fromString("ba46b46a-058a-4694-bb02-69aad1694bc7")
  private val model = ContactDoctorModel.create(patientUuid)
  private val updateSpec = UpdateSpec(ContactDoctorUpdate())

  @Test
  fun `when medical officers are loaded, then update the model`() {
    val medicalOfficers = listOf(
        TestData.medicalOfficer(
            id = UUID.fromString("a07e53e8-959b-4d08-bea7-161932ecc0a5"),
            fullName = "Doctor Doom",
            phoneNumber = "+911111111111"
        )
    )

    updateSpec
        .given(model)
        .whenEvent(MedicalOfficersLoaded(medicalOfficers))
        .then(assertThatNext(
            hasModel(model.medicalOfficersLoaded(medicalOfficers)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when teleconsult request is created, then load patient teleconsult info`() {
    val teleconsultRecordId = UUID.fromString("64ee25e0-8d32-42f5-b3e9-1a34f80e8e56")
    val doctorPhoneNumber = "+911111111111"

    updateSpec
        .given(model)
        .whenEvent(TeleconsultRequestCreated(
            teleconsultRecordId = teleconsultRecordId,
            doctorPhoneNumber = doctorPhoneNumber
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(LoadPatientTeleconsultInfo(
                patientUuid = patientUuid,
                teleconsultRecordId = teleconsultRecordId,
                doctorPhoneNumber = doctorPhoneNumber
            ))
        ))
  }
}
