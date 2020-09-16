package org.simple.clinic.teleconsultlog.prescription.medicines

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import java.util.UUID

class TeleconsultMedicinesInitTest {

  private val patientUuid = UUID.fromString("884275a8-a8a5-4062-a23b-78a0bf743e8a")
  private val model = TeleconsultMedicinesModel.create(patientUuid)
  private val initSpec = InitSpec(TeleconsultMedicinesInit())

  @Test
  fun `when screen is created, then load initial data`() {
    initSpec
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasEffects(LoadPatientMedicines(patientUuid))
        ))
  }
}
