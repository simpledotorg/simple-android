package org.simple.clinic.summary

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.FirstMatchers.hasNoEffects
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.summary.OpenIntention.ViewExistingPatient
import org.simple.clinic.summary.teleconsultation.api.TeleconsultInfo
import org.simple.clinic.user.User
import java.util.UUID

class PatientSummaryInitTest {

  private val patientUuid = UUID.fromString("fca8c3ad-75ca-4053-ba2f-e5c8ffda8991")
  private val defaultModel = PatientSummaryModel.from(ViewExistingPatient, patientUuid)

  private val initSpec = InitSpec(PatientSummaryInit())

  @Test
  fun `when the screen is created, load the initial data`() {
    initSpec
        .whenInit(defaultModel)
        .then(
            assertThatFirst(
                hasModel(defaultModel),
                hasEffects(
                    LoadPatientSummaryProfile(patientUuid),
                    LoadCurrentFacility,
                    CheckForInvalidPhone(patientUuid),
                    LoadUserLoggedInStatus
                )
            )
        )
  }

  @Test
  fun `when the screen is restored, do not load the current facility`() {
    val addressUuid = UUID.fromString("27f25667-44de-4717-b235-f75f5456af1d")

    val profile = PatientSummaryProfile(
        patient = TestData.patient(uuid = patientUuid, addressUuid = addressUuid),
        address = TestData.patientAddress(uuid = addressUuid),
        phoneNumber = null,
        bpPassport = null,
        alternativeId = null
    )
    val facility = TestData.facility(uuid = UUID.fromString("fc5b49de-0e07-4d33-8b77-6611b47cb403"))

    val model = defaultModel
        .completedCheckForInvalidPhone()
        .patientSummaryProfileLoaded(profile)
        .currentFacilityLoaded(facility)

    initSpec
        .whenInit(model)
        .then(
            assertThatFirst(
                hasModel(model),
                hasEffects(LoadPatientSummaryProfile(patientUuid) as PatientSummaryEffect)
            )
        )
  }

  @Test
  fun `when the screen is restored and current facility is already loaded, then fetch teleconsultation info if it's not already fetched`() {
    val addressUuid = UUID.fromString("27f25667-44de-4717-b235-f75f5456af1d")

    val profile = PatientSummaryProfile(
        patient = TestData.patient(uuid = patientUuid, addressUuid = addressUuid),
        address = TestData.patientAddress(uuid = addressUuid),
        phoneNumber = null,
        bpPassport = null,
        alternativeId = null
    )
    val facility = TestData.facility(uuid = UUID.fromString("fc5b49de-0e07-4d33-8b77-6611b47cb403"))

    val model = defaultModel
        .completedCheckForInvalidPhone()
        .patientSummaryProfileLoaded(profile)
        .currentFacilityLoaded(facility)
        .fetchingTeleconsultationInfo()

    initSpec
        .whenInit(model)
        .then(
            assertThatFirst(
                hasModel(model),
                hasEffects(FetchTeleconsultationInfo(facility.uuid) as PatientSummaryEffect)
            )
        )
  }

  @Test
  fun `when the screen is restored and fetching the teleconsulation info had failed, the fetch failure error must be shown`() {
    val addressUuid = UUID.fromString("27f25667-44de-4717-b235-f75f5456af1d")

    val profile = PatientSummaryProfile(
        patient = TestData.patient(uuid = patientUuid, addressUuid = addressUuid),
        address = TestData.patientAddress(uuid = addressUuid),
        phoneNumber = null,
        bpPassport = null,
        alternativeId = null
    )
    val facility = TestData.facility(uuid = UUID.fromString("fc5b49de-0e07-4d33-8b77-6611b47cb403"))

    val model = defaultModel
        .completedCheckForInvalidPhone()
        .patientSummaryProfileLoaded(profile)
        .currentFacilityLoaded(facility)
        .failedToFetchTeleconsultationInfo()

    initSpec
        .whenInit(model)
        .then(
            assertThatFirst(
                hasModel(model),
                hasEffects(ShowTeleconsultInfoError as PatientSummaryEffect)
            )
        )
  }

  @Test
  fun `when the screen is restored and user logged in status is already loaded, then do not load user logged in status`() {
    val addressUuid = UUID.fromString("27f25667-44de-4717-b235-f75f5456af1d")

    val profile = PatientSummaryProfile(
        patient = TestData.patient(uuid = patientUuid, addressUuid = addressUuid),
        address = TestData.patientAddress(uuid = addressUuid),
        phoneNumber = null,
        bpPassport = null,
        alternativeId = null
    )
    val facility = TestData.facility(uuid = UUID.fromString("fc5b49de-0e07-4d33-8b77-6611b47cb403"))
    val phoneNumber = "+911111111111"

    val model = defaultModel
        .completedCheckForInvalidPhone()
        .patientSummaryProfileLoaded(profile)
        .currentFacilityLoaded(facility)
        .fetchedTeleconsultationInfo(TeleconsultInfo.Fetched(phoneNumber))
        .userLoggedInStatusLoaded(User.LoggedInStatus.LOGGED_IN)

    initSpec
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasEffects(LoadPatientSummaryProfile(patientUuid) as PatientSummaryEffect)
        ))
  }
}
