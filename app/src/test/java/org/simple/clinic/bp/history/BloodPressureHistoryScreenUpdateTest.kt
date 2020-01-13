package org.simple.clinic.bp.history

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.patient.PatientMocker
import java.util.UUID

class BloodPressureHistoryScreenUpdateTest {
  private val patientUuid = UUID.fromString("6b455aea-1580-4900-9524-c19a4d3db676")
  private val model = BloodPressureHistoryScreenModel.create(patientUuid)
  private val updateSpec = UpdateSpec<BloodPressureHistoryScreenModel, BloodPressureHistoryScreenEvent, BloodPressureHistoryScreenEffect>(BloodPressureHistoryScreenUpdate())

  @Test
  fun `when blood pressure history is loaded, then show blood pressures`() {
    val bloodPressure1 = PatientMocker.bp(UUID.fromString("8815d0fc-73cc-44a2-a4b3-473c4c0989aa"))
    val bloodPressure2 = PatientMocker.bp(UUID.fromString("ddf87db7-1034-4618-bc0e-879d7d357adf"))
    val bloodPressures = listOf(bloodPressure1, bloodPressure2)

    updateSpec
        .given(model)
        .whenEvent(BloodPressureHistoryLoaded(bloodPressures))
        .then(
            assertThatNext(
                hasModel(model.historyLoaded(bloodPressures)),
                hasNoEffects()
            )
        )
  }

  @Test
  fun `when add new blood pressure is clicked, then open entry sheet`() {
    updateSpec
        .given(model)
        .whenEvent(NewBloodPressureClicked)
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(OpenBloodPressureEntrySheet as BloodPressureHistoryScreenEffect)
            )
        )
  }
}
