package org.simple.clinic.drugs

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import java.util.UUID

class EditMedicinesInitTest {
  @Test
  fun `when sheet is created, then fetch prescribed and protocol drugs and load drug frequency choice items`() {
    val patientUuid = UUID.fromString("bcdd3e43-4404-49b5-90dc-f58d3a31e431")
    val model = EditMedicinesModel.create(patientUuid)
    val initSpec = InitSpec(EditMedicinesInit())

    initSpec
        .whenInit(model)
        .then(
            assertThatFirst(
                hasModel(model),
                hasEffects(FetchPrescribedAndProtocolDrugs(patientUuid), LoadDrugFrequencyChoiceItems)
            )
        )
  }
}
