package org.simple.clinic.teleconsultlog.teleconsultrecord.screen

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import java.util.UUID

class TeleconsultRecordInitTest {

  private val patientUuid = UUID.fromString("cb0d01f9-c311-467d-9ab2-bab983253898")
  private val teleconsultRecordId = UUID.fromString("20dccc72-9697-46cb-a4dc-817d71675716")
  private val defaultModel = TeleconsultRecordModel.create(patientUuid, teleconsultRecordId)
  private val initSpec = InitSpec(TeleconsultRecordInit())

  @Test
  fun `when screen is created, then load initial data`() {
    initSpec
        .whenInit(defaultModel)
        .then(assertThatFirst(
            hasModel(defaultModel),
            hasEffects(LoadPatientDetails(patientUuid), LoadTeleconsultRecordWithPrescribedDrugs(teleconsultRecordId))
        ))
  }

  @Test
  fun `when screen is restored, then load teleconsult record with prescribed drugs`() {
    initSpec
        .whenInit(defaultModel)
        .then(assertThatFirst(
            hasModel(defaultModel),
            hasEffects(LoadTeleconsultRecordWithPrescribedDrugs(teleconsultRecordId))
        ))
  }
}
