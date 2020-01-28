package org.simple.clinic.medicalhistory.newentry

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import org.simple.clinic.facility.FacilityConfig
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.PatientMocker
import java.util.UUID

class NewMedicalHistoryInitTest {

  private val defaultModel = NewMedicalHistoryModel.default()

  private val initSpec = InitSpec(NewMedicalHistoryInit())

  @Test
  fun `when the screen is created, the ongoing patient entry and the current facility must be loaded`() {
    initSpec
        .whenInit(defaultModel)
        .then(
            assertThatFirst(
                hasModel(defaultModel),
                hasEffects(LoadOngoingPatientEntry, LoadCurrentFacility)
            )
        )
  }

  @Test
  fun `when the screen is restored, set up the screen for diabetes management if the facility supports it`() {
    val facility = PatientMocker.facility(
        uuid = UUID.fromString("52655f55-5aaa-4879-8db0-58b49be3bff1"),
        facilityConfig = FacilityConfig(diabetesManagementEnabled = true)
    )

    val model = defaultModel
        .ongoingPatientEntryLoaded(OngoingNewPatientEntry.fromFullName("Anish Acharya"))
        .currentFacilityLoaded(facility)

    initSpec
        .whenInit(model)
        .then(
            assertThatFirst(
                hasModel(model),
                hasEffects(SetupUiForDiabetesManagement(true) as NewMedicalHistoryEffect)
            )
        )
  }

  @Test
  fun `when the screen is restored, disable the screen for diabetes management if the facility does not support it`() {
    val facility = PatientMocker.facility(
        uuid = UUID.fromString("52655f55-5aaa-4879-8db0-58b49be3bff1"),
        facilityConfig = FacilityConfig(diabetesManagementEnabled = false)
    )

    val model = defaultModel
        .ongoingPatientEntryLoaded(OngoingNewPatientEntry.fromFullName("Anish Acharya"))
        .currentFacilityLoaded(facility)

    initSpec
        .whenInit(model)
        .then(
            assertThatFirst(
                hasModel(model),
                hasEffects(SetupUiForDiabetesManagement(false) as NewMedicalHistoryEffect)
            )
        )
  }
}
