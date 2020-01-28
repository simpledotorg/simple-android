package org.simple.clinic.medicalhistory.newentry

import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.patient.PatientMocker
import java.util.UUID

class NewMedicalHistoryUpdateTest {

  @Test
  fun `when the current facility is loaded, update the ui`() {
    val model = NewMedicalHistoryModel.default()
    val facility = PatientMocker.facility(uuid = UUID.fromString("3c7bc1c8-1bb6-4c3a-b6d0-52700bdaac5c"))

    val updateSpec = UpdateSpec(NewMedicalHistoryUpdate())

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
