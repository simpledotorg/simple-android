package org.simple.clinic.teleconsultlog.teleconsultrecord.screen

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import java.util.UUID

class TeleconsultRecordInitTest {

  @Test
  fun `when screen is created, then load teleconsult record with prescribed drugs`() {
    val patientUuid = UUID.fromString("cb0d01f9-c311-467d-9ab2-bab983253898")
    val teleconsultRecordId = UUID.fromString("20dccc72-9697-46cb-a4dc-817d71675716")
    val defaultModel = TeleconsultRecordModel.create(patientUuid, teleconsultRecordId)

    InitSpec(TeleconsultRecordInit())
        .whenInit(defaultModel)
        .then(assertThatFirst(
            hasModel(defaultModel),
            hasEffects(LoadTeleconsultRecordWithPrescribedDrugs(teleconsultRecordId))
        ))
  }
}
