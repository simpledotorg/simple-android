package org.simple.clinic.summary.teleconsultation.contactdoctor

import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import java.util.UUID

class ContactDoctorUpdateTest {

  @Test
  fun `when medical officers are loaded, then update the model`() {
    val medicalOfficers = listOf(
        TestData.medicalOfficer(
            id = UUID.fromString("a07e53e8-959b-4d08-bea7-161932ecc0a5"),
            fullName = "Doctor Doom",
            phoneNumber = "+911111111111"
        )
    )

    val model = ContactDoctorModel.create()
    val updateSpec = UpdateSpec(ContactDoctorUpdate())

    updateSpec
        .given(model)
        .whenEvent(MedicalOfficersLoaded(medicalOfficers))
        .then(assertThatNext(
            hasModel(model.medicalOfficersLoaded(medicalOfficers)),
            hasNoEffects()
        ))
  }
}
