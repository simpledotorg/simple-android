package org.simple.clinic.summary.bloodpressures.newbpsummary

import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.patient.PatientMocker
import java.util.UUID

class NewBloodPressureSummaryViewUpdateTest {
  private val patientUuid = UUID.fromString("2871447a-571f-43c6-b946-ba8133f583be")
  private val defaultModel = NewBloodPressureSummaryViewModel.create(patientUuid)

  private val updateSpec = UpdateSpec<NewBloodPressureSummaryViewModel, NewBloodPressureSummaryViewEvent, NewBloodPressureSummaryViewEffect>(NewBloodPressureSummaryViewUpdate())

  @Test
  fun `when blood pressures are loaded, then show blood pressures`() {
    val bloodPressure1 = PatientMocker.bp(UUID.fromString("8815d0fc-73cc-44a2-a4b3-473c4c0989aa"))
    val bloodPressure2 = PatientMocker.bp(UUID.fromString("ddf87db7-1034-4618-bc0e-879d7d357adf"))
    val bloodPressures = listOf(bloodPressure1, bloodPressure2)

    updateSpec
        .given(defaultModel)
        .whenEvent(BloodPressuresLoaded(bloodPressures))
        .then(
            assertThatNext(
                hasModel(defaultModel.bloodPressuresLoaded(bloodPressures)),
                hasNoEffects()
            )
        )
  }

  @Test
  fun `when blood pressures count is loaded, then change the blood pressures count in model`() {
    val bloodPressuresCount = 4

    updateSpec
        .given(defaultModel)
        .whenEvent(BloodPressuresCountLoaded(bloodPressuresCount))
        .then(
            assertThatNext(
                hasModel(defaultModel.bloodPressuresCountLoaded(bloodPressuresCount)),
                hasNoEffects()
            )
        )
  }
}
