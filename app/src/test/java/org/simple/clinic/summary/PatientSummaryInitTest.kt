package org.simple.clinic.summary

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.FirstMatchers.hasNoEffects
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.summary.OpenIntention.*
import java.util.UUID

class PatientSummaryInitTest {

  private val patientUuid = UUID.fromString("fca8c3ad-75ca-4053-ba2f-e5c8ffda8991")
  private val defaultModel = PatientSummaryModel.from(ViewExistingPatient, patientUuid)

  private val initSpec = InitSpec(PatientSummaryInit())

  @Test
  fun `when the screen is created, load the current facility and patient profile`() {
    initSpec
        .whenInit(defaultModel)
        .then(
            assertThatFirst(
                hasModel(defaultModel),
                hasEffects(
                    LoadPatientSummaryProfile(patientUuid),
                    LoadCurrentFacility,
                    CheckForInvalidPhone(patientUuid)
                )
            )
        )
  }

  @Test
  fun `when the screen is restored, do not load the current facility and patient profile`() {
    val addressUuid = UUID.fromString("27f25667-44de-4717-b235-f75f5456af1d")

    val profile = PatientSummaryProfile(
        patient = PatientMocker.patient(uuid = patientUuid, addressUuid = addressUuid),
        address = PatientMocker.address(uuid = addressUuid),
        phoneNumber = null,
        bpPassport = null,
        bangladeshNationalId = null
    )
    val facility = PatientMocker.facility(uuid = UUID.fromString("fc5b49de-0e07-4d33-8b77-6611b47cb403"))

    val model = defaultModel
        .completedCheckForInvalidPhone()
        .patientSummaryProfileLoaded(profile)
        .currentFacilityLoaded(facility)

    initSpec
        .whenInit(model)
        .then(
            assertThatFirst(
                hasModel(model),
                hasNoEffects()
            )
        )
  }
}
