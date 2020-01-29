package org.simple.clinic.bloodsugar.history

import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.patient.PatientMocker
import java.util.UUID

class BloodSugarHistoryScreenUpdateTest {
  @Test
  fun `when patient is loaded, then show patient information`() {
    val patientUuid = UUID.fromString("871a2f40-2bda-488c-9443-7dc708c3743a")
    val patient = PatientMocker.patient(uuid = patientUuid)
    val defaultModel = BloodSugarHistoryScreenModel.create(patientUuid)
    val updateSpec = UpdateSpec<BloodSugarHistoryScreenModel, BloodSugarHistoryScreenEvent, BloodSugarHistoryScreenEffect>(BloodSugarHistoryScreenUpdate())

    updateSpec
        .given(defaultModel)
        .whenEvent(PatientLoaded(patient))
        .then(assertThatNext(
            hasModel(defaultModel.patientLoaded(patient)),
            hasNoEffects()
        ))
  }
}
