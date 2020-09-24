package org.simple.clinic.teleconsultlog.prescription

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.FirstMatchers.hasNoEffects
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import org.simple.clinic.TestData
import java.util.UUID

class TeleconsultPrescriptionInitTest {

  private val teleconsultRecordId = UUID.fromString("38c95d84-8d5d-44c8-a084-63dbe1be89a1")
  private val patientUuid = UUID.fromString("cda5707d-4bb5-499d-92da-a135ea420905")
  private val model = TeleconsultPrescriptionModel.create(teleconsultRecordId = teleconsultRecordId, patientUuid = patientUuid)

  private val initSpec = InitSpec(TeleconsultPrescriptionInit())

  @Test
  fun `when screen is created, the load patient details`() {
    initSpec
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasEffects(LoadPatientDetails(patientUuid))
        ))
  }

  @Test
  fun `when screen is restored, then don't load patient`() {
    val patient = TestData.patient(uuid = patientUuid)
    val patientLoadedModel = model.patientLoaded(patient)

    initSpec
        .whenInit(patientLoadedModel)
        .then(assertThatFirst(
            hasModel(patientLoadedModel),
            hasNoEffects()
        ))
  }
}
