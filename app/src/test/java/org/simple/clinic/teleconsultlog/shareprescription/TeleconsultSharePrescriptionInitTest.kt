package org.simple.clinic.teleconsultlog.shareprescription

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import java.time.LocalDate
import java.util.UUID

class TeleconsultSharePrescriptionInitTest {

  private val patientUuid: UUID = UUID.fromString("d80927dc-e4f2-4224-a897-9352042115a9")
  private val prescriptionDate = LocalDate.parse("2020-10-01")
  private val model = TeleconsultSharePrescriptionModel.create(
      patientUuid = patientUuid,
      prescriptionDate = prescriptionDate
  )
  val initSpec = InitSpec(TeleconsultSharePrescriptionInit())

  @Test
  fun `when screen is created, load the patient details`() {
    initSpec
        .whenInit(model)
        .then(
            assertThatFirst(
                hasModel(model),
                hasEffects(LoadPatientDetails(patientUuid))
            )
        )
  }

  @Test
  fun `when the screen is created, then load the patient medicines`() {
    initSpec
        .whenInit(model)
        .then(
            assertThatFirst(
                hasModel(model),
                hasEffects(LoadPatientMedicines(patientUuid = patientUuid))
            )
        )
  }
}
