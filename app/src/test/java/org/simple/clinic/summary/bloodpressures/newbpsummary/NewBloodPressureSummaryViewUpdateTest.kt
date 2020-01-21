package org.simple.clinic.summary.bloodpressures.newbpsummary

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.patient.PatientMocker
import java.util.UUID

class NewBloodPressureSummaryViewUpdateTest {
  private val patientUuid = UUID.fromString("8f1befda-f99e-4d26-aff3-cecb90925df1")
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

  @Test
  fun `when add new blood pressure is clicked, then open blood pressure entry sheet`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(NewBloodPressureClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenBloodPressureEntrySheet(patientUuid) as NewBloodPressureSummaryViewEffect)
        ))
  }

  @Test
  fun `when blood pressure is clicked, then open blood pressure update sheet`() {
    val bloodPressureMeasurement = PatientMocker.bp(
        UUID.fromString("88ed645b-7b00-4a72-81bb-94fba4474523"),
        patientUuid
    )
    val bloodPressures = listOf(bloodPressureMeasurement)

    updateSpec
        .given(defaultModel.bloodPressuresLoaded(bloodPressures))
        .whenEvent(BloodPressureClicked(bloodPressureMeasurement))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenBloodPressureUpdateSheet(bloodPressureMeasurement) as NewBloodPressureSummaryViewEffect)
        ))
  }
}
