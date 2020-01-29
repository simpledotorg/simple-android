package org.simple.clinic.bloodsugar.history

import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.patient.PatientMocker
import java.util.UUID

class BloodSugarHistoryScreenUpdateTest {
  private val patientUuid = UUID.fromString("871a2f40-2bda-488c-9443-7dc708c3743a")
  private val defaultModel = BloodSugarHistoryScreenModel.create(patientUuid)
  private val updateSpec = UpdateSpec<BloodSugarHistoryScreenModel, BloodSugarHistoryScreenEvent, BloodSugarHistoryScreenEffect>(BloodSugarHistoryScreenUpdate())

  @Test
  fun `when patient is loaded, then show patient information`() {
    val patient = PatientMocker.patient(uuid = patientUuid)

    updateSpec
        .given(defaultModel)
        .whenEvent(PatientLoaded(patient))
        .then(assertThatNext(
            hasModel(defaultModel.patientLoaded(patient)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when blood sugars are loaded, then show blood sugars`() {
    val bloodSugar1 = PatientMocker.bloodSugar(
        uuid = UUID.fromString("6ac799f9-1c2f-4586-a670-297efd29f776"),
        patientUuid = patientUuid
    )
    val bloodSugar2 = PatientMocker.bloodSugar(
        uuid = UUID.fromString("955c7cb2-2a31-436a-a599-01f537fb7e0f"),
        patientUuid = patientUuid
    )
    val bloodSugar3 = PatientMocker.bloodSugar(
        uuid = UUID.fromString("7b3da121-0936-4888-8adf-66347d91ab59"),
        patientUuid = patientUuid
    )
    val bloodSugars = listOf(bloodSugar1, bloodSugar2, bloodSugar3)

    updateSpec
        .given(defaultModel)
        .whenEvent(BloodSugarHistoryLoaded(bloodSugars))
        .then(assertThatNext(
            hasModel(defaultModel.bloodSugarsLoaded(bloodSugars)),
            hasNoEffects()
        ))
  }
}
