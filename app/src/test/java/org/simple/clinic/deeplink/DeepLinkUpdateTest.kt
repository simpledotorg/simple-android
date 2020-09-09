package org.simple.clinic.deeplink

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.user.User
import java.util.UUID

class DeepLinkUpdateTest {

  private val defaultModel = DeepLinkModel.default(null, null, false)
  private val updateSpec = UpdateSpec(DeepLinkUpdate())
  private val patientUuid = UUID.fromString("f88cc05b-620a-490a-92f3-1c0c43fb76ab")

  @Test
  fun `if there is no user logged in, then open setup activity`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(UserFetched(null))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(NavigateToSetupActivity as DeepLinkEffect)
        ))
  }

  @Test
  fun `if there is a logged in user and patient uuid is null, then show no patient error`() {
    val user = TestData.loggedInUser(
        uuid = UUID.fromString("dc0a9d11-aee4-4792-820f-c5cb66ae5e47"),
        loggedInStatus = User.LoggedInStatus.LOGGED_IN,
        capabilities = User.Capabilities(User.CapabilityStatus.Yes)
    )

    updateSpec
        .given(defaultModel)
        .whenEvent(UserFetched(user))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowNoPatientUuidError as DeepLinkEffect)
        ))
  }

  @Test
  fun `if user didn't complete the login, then navigate to setup activity`() {
    val user = TestData.loggedInUser(
        uuid = UUID.fromString("dc0a9d11-aee4-4792-820f-c5cb66ae5e47"),
        loggedInStatus = User.LoggedInStatus.OTP_REQUESTED
    )

    updateSpec
        .given(defaultModel)
        .whenEvent(UserFetched(user))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(NavigateToSetupActivity as DeepLinkEffect)
        ))
  }

  @Test
  fun `if user is logged in and patient uuid is not null, then fetch patient`() {
    val user = TestData.loggedInUser(
        uuid = UUID.fromString("fa0dfb7b-a0ea-425a-987d-2056f1a9e93b"),
        loggedInStatus = User.LoggedInStatus.LOGGED_IN,
        capabilities = User.Capabilities(User.CapabilityStatus.Yes)
    )
    val model = DeepLinkModel.default(patientUuid, null, false)

    updateSpec
        .given(model)
        .whenEvent(UserFetched(user))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(FetchPatient(patientUuid) as DeepLinkEffect)
        ))
  }

  @Test
  fun `if user is logged in and user does not have capabilities, then show user is not authorised to teleconsult log`(){
    val user = TestData.loggedInUser(
        uuid = UUID.fromString("ce7af001-5b38-4f4b-9d7c-62af2a47643e"),
        loggedInStatus = User.LoggedInStatus.LOGGED_IN,
        capabilities = User.Capabilities(User.CapabilityStatus.No)
    )
    val model = DeepLinkModel.default(patientUuid, null, false)

    updateSpec
        .given(model)
        .whenEvent(UserFetched(user))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowTeleconsultLogNotAllowed as DeepLinkEffect)
        ))
  }

  @Test
  fun `if patient exists, then navigate to patient summary`() {
    val patient = TestData.patient(
        uuid = patientUuid
    )
    val model = DeepLinkModel.default(
        patientUuid = patientUuid,
        teleconsultRecordId = null,
        isLogTeleconsultDeepLink = false
    )

    updateSpec
        .given(model)
        .whenEvent(PatientFetched(patient))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(NavigateToPatientSummary(patientUuid) as DeepLinkEffect)
        ))
  }

  @Test
  fun `if patient exists and deep link is teleconsult log deep link, then navigate to patient summary with teleconsult log`() {
    val patient = TestData.patient(
        uuid = patientUuid
    )
    val teleconsultRecordId = UUID.fromString("cec5c691-6623-465d-8d86-27cce2e5acbc")
    val model = DeepLinkModel.default(
        patientUuid = patientUuid,
        teleconsultRecordId = teleconsultRecordId,
        isLogTeleconsultDeepLink = true
    )

    updateSpec
        .given(model)
        .whenEvent(PatientFetched(patient))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(NavigateToPatientSummaryWithTeleconsultLog(patientUuid, teleconsultRecordId) as DeepLinkEffect)
        ))
  }

  @Test
  fun `if patient does not exist, then show patient does not exist`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(PatientFetched(null))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowPatientDoesNotExist as DeepLinkEffect)
        ))
  }
}
