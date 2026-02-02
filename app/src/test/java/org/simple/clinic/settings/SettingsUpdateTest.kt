package org.simple.clinic.settings

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.user.UserSession.LogoutResult.Failure
import org.simple.clinic.user.UserSession.LogoutResult.Success
import java.util.UUID

class SettingsUpdateTest {

  private val defaultModel = SettingsModel.default(
      isChangeLanguageFeatureEnabled = true,
  )

  private val spec = UpdateSpec(SettingsUpdate())

  @Test
  fun `when the user details are loaded, the ui must be updated`() {
    val userName = "Anish Acharya"
    val userPhoneNumber = "1234567890"

    spec
        .given(defaultModel)
        .whenEvent(UserDetailsLoaded(userName, userPhoneNumber))
        .then(assertThatNext(
            hasModel(defaultModel.userDetailsFetched(userName, userPhoneNumber)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when the current language is loaded, the ui must be updated`() {
    val language = ProvidedLanguage(displayName = "English", languageCode = "en-IN")

    spec
        .given(defaultModel)
        .whenEvent(CurrentLanguageLoaded(language))
        .then(assertThatNext(
            hasModel(defaultModel.currentLanguageFetched(language)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when the change language button is clicked, the language selection screen must be opened`() {
    val model = defaultModel
        .userDetailsFetched("Anish Acharya", "1234567890")
        .currentLanguageFetched(SystemDefaultLanguage)

    spec
        .given(model)
        .whenEvent(ChangeLanguage)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenLanguageSelectionScreenEffect as SettingsEffect)
        ))
  }

  @Test
  fun `when the app version is loaded, then ui must be updated`() {
    val appVersion = "1.0.0"

    spec
        .given(defaultModel)
        .whenEvent(AppVersionLoaded(appVersion))
        .then(assertThatNext(
            hasModel(defaultModel.appVersionLoaded(appVersion)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when there is app update, then ui must be updated`() {
    val isUpdateAvailable = true

    spec
        .given(defaultModel)
        .whenEvent(AppUpdateAvailabilityChecked(isUpdateAvailable))
        .then(assertThatNext(
            hasModel(defaultModel.checkedAppUpdate(isUpdateAvailable))
        ))
  }

  @Test
  fun `when logout button is clicked, then show confirm logout dialog`() {
    spec
        .given(defaultModel)
        .whenEvent(LogoutButtonClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowConfirmLogoutDialog)
        ))
  }

  @Test
  fun `when confirm logout button is clicked, then update model and logout user`() {
    spec
        .given(defaultModel)
        .whenEvent(ConfirmLogoutButtonClicked)
        .then(assertThatNext(
            hasModel(defaultModel.userLoggingOut()),
            hasEffects(LogoutUser)
        ))
  }

  @Test
  fun `when user is logged out successfully, then update the model and restart the app process`() {
    spec
        .given(defaultModel)
        .whenEvent(UserLogoutResult(Success))
        .then(assertThatNext(
            hasModel(defaultModel.userLoggedOut()),
            hasEffects(RestartApp)
        ))
  }

  @Test
  fun `when user is not logged out successfully, then update the model`() {
    spec
        .given(defaultModel)
        .whenEvent(UserLogoutResult(Failure(IllegalArgumentException())))
        .then(assertThatNext(
            hasModel(defaultModel.userLogoutFailed()),
            hasNoEffects()
        ))
  }

  @Test
  fun `when back is clicked and user is not in the process of logging out, then go back`() {
    spec
        .given(defaultModel.userLoggedOut())
        .whenEvent(BackClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(GoBack)
        ))
  }

  @Test
  fun `when back is clicked and user is in the process of logging out, then do nothing`() {
    spec
        .given(defaultModel.userLoggingOut())
        .whenEvent(BackClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasNoEffects()
        ))
  }

  @Test
  fun `when sync medical records button is clicked, then update model and fetch complete medical records`() {
    spec
        .given(defaultModel)
        .whenEvent(PushAllMedicalRecordsClicked)
        .then(assertThatNext(
            hasModel(defaultModel.medicalRecordsPushStarted()),
            hasEffects(FetchCompleteMedicalRecords)
        ))
  }

  @Test
  fun `when complete medical records are fetch, then push them online`() {
    // given
    val identifier = Identifier("4f1cea37-70ff-498e-bd09-ad0ca75628ff", BpPassport)
    val commonIdentifier = TestData.businessId(identifier = identifier)
    val patientUuid1 = TestData.patientProfile(patientUuid = UUID.fromString("0b78c024-f527-4306-9e20-6ae6d7251e9b"), businessId = commonIdentifier)
    val patientUuid2 = TestData.patientProfile(patientUuid = UUID.fromString("47fdb968-9512-4e50-b95f-cc83c6de4b0a"), businessId = commonIdentifier)

    val completeMedicalRecord = TestData.completeMedicalRecord(patient = patientUuid1)
    val completeMedicalRecord2 = TestData.completeMedicalRecord(patient = patientUuid2)

    val medicalRecords = listOf(completeMedicalRecord, completeMedicalRecord2)

    spec
        .given(defaultModel)
        .whenEvent(MedicalRecordsFetched(medicalRecords))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(PushCompleteMedicalRecordsOnline(medicalRecords))
        ))
  }
}
