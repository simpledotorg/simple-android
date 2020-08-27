package org.simple.clinic.summary.teleconsultation.contactdoctor

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.FirstMatchers.hasNoEffects
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import org.simple.clinic.TestData
import java.util.UUID

class ContactDoctorInitTest {

  private val model = ContactDoctorModel.create()
  private val initSpec = InitSpec(ContactDoctorInit())

  @Test
  fun `when screen is created, then load the medical officers`() {
    initSpec
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasEffects(LoadMedicalOfficers)
        ))
  }

  @Test
  fun `when screen is restored and medical officers are loaded, then don't load medical officers again`() {
    val medicalOfficers = listOf(
        TestData.medicalOfficer(
            id = UUID.fromString("f89ef5e2-71a7-4c76-963b-ab8599bc3217"),
            fullName = "Dr Sunil Dhar",
            phoneNumber = "+911111111111"
        )
    )
    val modelWithDoctors = model.medicalOfficersLoaded(medicalOfficers)

    initSpec
        .whenInit(modelWithDoctors)
        .then(assertThatFirst(
            hasModel(modelWithDoctors),
            hasNoEffects()
        ))
  }
}
