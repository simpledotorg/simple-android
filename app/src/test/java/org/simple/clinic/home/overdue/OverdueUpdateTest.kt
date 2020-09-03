package org.simple.clinic.home.overdue

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import java.time.LocalDate
import java.util.UUID

class OverdueUpdateTest {

  @Test
  fun `when patient name is clicked, then open patient summary screen`() {
    val dateOnClock = LocalDate.parse("2018-01-01")
    val patientUuid = UUID.fromString("1211bce0-0b5d-4203-b5e3-004709059eca")

    UpdateSpec(OverdueUpdate(dateOnClock))
        .given(OverdueModel.create())
        .whenEvent(PatientNameClicked(patientUuid))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenPatientSummary(patientUuid))
        ))
  }
}
