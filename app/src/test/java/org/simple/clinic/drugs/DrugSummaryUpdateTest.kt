package org.simple.clinic.drugs;

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.summary.prescribeddrugs.CurrentFacilityLoaded
import org.simple.clinic.summary.prescribeddrugs.DrugSummaryEffect
import org.simple.clinic.summary.prescribeddrugs.DrugSummaryEvent
import org.simple.clinic.summary.prescribeddrugs.DrugSummaryModel
import org.simple.clinic.summary.prescribeddrugs.DrugSummaryUpdate
import org.simple.clinic.summary.prescribeddrugs.OpenUpdatePrescribedDrugScreen
import java.util.UUID

class DrugSummaryUpdateTest {
  private val patientUuid = UUID.fromString("871a2f40-2bda-488c-9443-7dc708c3743a")
  private val updateSpec = UpdateSpec<DrugSummaryModel, DrugSummaryEvent, DrugSummaryEffect>(DrugSummaryUpdate())
  private val defaultModel = DrugSummaryModel.create(patientUuid)
  private val facility = TestData.facility(
      uuid = UUID.fromString("de250445-0ec9-43e4-be33-2a49ca334535"),
      name = "CHC Buchho",
  )

  @Test
  fun `when current facility is loaded and there are prescribed drugs, then open prescribed drug screen with refill medicines open intention`() {
    val prescribedDrugRecords = listOf(
        TestData.prescription(uuid = UUID.fromString("4aec376e-1a8f-11eb-adc1-0242ac120002"), name = "Amlodipine1"),
        TestData.prescription(uuid = UUID.fromString("537a119e-1a8f-11eb-adc1-0242ac120002"), name = "Amlodipine2"),
        TestData.prescription(uuid = UUID.fromString("5ac2a678-1a8f-11eb-adc1-0242ac120002"), name = "Amlodipine3"),
        TestData.prescription(uuid = UUID.fromString("5f9f0fe2-1a8f-11eb-adc1-0242ac120002"), name = "Amlodipine4"),
    )

    updateSpec
        .given(defaultModel.prescribedDrugsLoaded(prescribedDrugRecords))
        .whenEvent(CurrentFacilityLoaded(facility))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenUpdatePrescribedDrugScreen(patientUuid, facility, OpenIntention.RefillMedicine)),
        ))
  }

  @Test
  fun `when current facility is loaded and there are no prescribed drugs, then open prescribed drug screen with add new medicines open intention`() {
    val prescribedDrugRecords = emptyList<PrescribedDrug>()

    updateSpec
        .given(defaultModel.prescribedDrugsLoaded(prescribedDrugRecords))
        .whenEvent(CurrentFacilityLoaded(facility))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenUpdatePrescribedDrugScreen(patientUuid, facility, OpenIntention.AddNewMedicine)),
        ))
  }
}
