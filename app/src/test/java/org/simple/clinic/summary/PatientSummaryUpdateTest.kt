package org.simple.clinic.summary

import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.facility.FacilityConfig
import org.simple.clinic.patient.PatientMocker
import java.util.UUID

class PatientSummaryUpdateTest {

  @Test
  fun `when the current facility is loaded, update the UI`() {
    val facility = PatientMocker.facility(
        uuid = UUID.fromString("abe86f8e-1828-48fe-afb5-d697b3ce36bb"),
        facilityConfig = FacilityConfig(diabetesManagementEnabled = true)
    )

    val model = PatientSummaryModel.from(UUID.fromString("93a131b0-890e-41a3-88ec-b35b48efc6c5"))

    val updateSpec = UpdateSpec(PatientSummaryUpdate())

    updateSpec
        .given(model)
        .whenEvent(CurrentFacilityLoaded(facility))
        .then(
            assertThatNext(
                hasModel(model.currentFacilityLoaded(facility)),
                hasNoEffects()
            )
        )
  }
}
