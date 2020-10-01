package org.simple.clinic.teleconsultlog.shareprescription

import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import java.time.LocalDate
import java.util.UUID

class TeleconsultSharePrescriptionUpdateTest {

  private val updateSpec = UpdateSpec(TeleconsultSharePrescriptionUpdate())
  private val patientUuid: UUID = UUID.fromString("b0d1047f-4d76-4518-b6d9-daa5c4bb1c7e")
  private val prescriptionDate = LocalDate.parse("2020-10-01")
  private val model = TeleconsultSharePrescriptionModel
      .create(patientUuid = patientUuid, prescriptionDate = prescriptionDate)

  @Test
  fun `when patient details are loaded, update the model`() {
    val patient = TestData.patient(patientUuid)
    updateSpec
        .given(model)
        .whenEvents(PatientDetailsLoaded(patient = patient))
        .then(
            assertThatNext(
                hasModel(model.patientLoaded(patient)),
                hasNoEffects()
            )
        )
  }

}
