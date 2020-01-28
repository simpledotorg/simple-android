package org.simple.clinic.medicalhistory.newentry

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.facility.FacilityConfig
import org.simple.clinic.medicalhistory.Answer.Yes
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DIAGNOSED_WITH_HYPERTENSION
import org.simple.clinic.patient.PatientMocker
import java.util.UUID

class NewMedicalHistoryUpdateTest {

  private val defaultModel = NewMedicalHistoryModel.default()
  private val facilityWithDiabetesManagementEnabled = PatientMocker.facility(
      uuid = UUID.fromString("3c7bc1c8-1bb6-4c3a-b6d0-52700bdaac5c"),
      facilityConfig = FacilityConfig(diabetesManagementEnabled = true)
  )
  private val facilityWithDiabetesManagementDisabled = PatientMocker.facility(
      uuid = UUID.fromString("bbffeac9-296d-4e95-8266-e5c9ac5eedf3"),
      facilityConfig = FacilityConfig(diabetesManagementEnabled = false)
  )

  private val updateSpec = UpdateSpec(NewMedicalHistoryUpdate())

  @Test
  fun `when the current facility is loaded with diabetes management enabled, update the ui`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(CurrentFacilityLoaded(facilityWithDiabetesManagementEnabled))
        .then(
            assertThatNext(
                hasModel(defaultModel.currentFacilityLoaded(facilityWithDiabetesManagementEnabled)),
                hasEffects(SetupUiForDiabetesManagement(true) as NewMedicalHistoryEffect)
            )
        )
  }

  @Test
  fun `when the current facility is loaded with diabetes management disabled, update the UI and set the hypertension history answer as YES`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(CurrentFacilityLoaded(facilityWithDiabetesManagementDisabled))
        .then(
            assertThatNext(
                hasModel(
                    defaultModel
                        .currentFacilityLoaded(facilityWithDiabetesManagementDisabled)
                        .answerChanged(DIAGNOSED_WITH_HYPERTENSION, Yes)
                ),
                hasEffects(SetupUiForDiabetesManagement(false) as NewMedicalHistoryEffect)
            )
        )
  }
}
