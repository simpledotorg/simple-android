package org.simple.clinic.settings

import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.After
import org.junit.Test
import org.simple.sharedTestCode.TestData
import org.simple.clinic.appupdate.AppUpdateState
import org.simple.clinic.appupdate.AppUpdateState.ShowAppUpdate
import org.simple.clinic.appupdate.CheckAppUpdateAvailability
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.UserSession.LogoutResult
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import org.simple.clinic.util.toOptional
import java.util.Optional
import java.util.UUID

class SettingsEffectHandlerTest {

  private val userSession = mock<UserSession>()
  private val settingsRepository = mock<SettingsRepository>()
  private val uiActions = mock<UiActions>()
  private val appVersionFetcher = mock<AppVersionFetcher>()
  private val checkAppUpdateAvailability = mock<CheckAppUpdateAvailability>()

  private val effectHandler = SettingsEffectHandler(
      userSession = userSession,
      settingsRepository = settingsRepository,
      schedulersProvider = TrampolineSchedulersProvider(),
      appVersionFetcher = appVersionFetcher,
      appUpdateAvailability = checkAppUpdateAvailability,
      viewEffectsConsumer = SettingsViewEffectHandler(uiActions)::handle
  ).build()
  private val testCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `user details must be fetched when the load user details event is received`() {
    // given
    val savedUser = TestData.loggedInUser(
        uuid = UUID.fromString("eadfd8ef-6c88-4bc9-ba96-e05bec2d6d8b"),
        name = "Mahalakshmi Puri",
        phone = "1234567890"
    )
    val updatedUser = savedUser
        .withFullName("Mahalakshmi T Sharma")
        .withPhoneNumber("0987654321")

    whenever(userSession.loggedInUser()).doReturn(Observable.just(
        savedUser.toOptional(),
        updatedUser.toOptional()
    ))

    // when
    testCase.dispatch(LoadUserDetailsEffect)

    // then
    testCase.assertOutgoingEvents(
        UserDetailsLoaded(name = savedUser.fullName, phoneNumber = savedUser.phoneNumber),
        UserDetailsLoaded(name = updatedUser.fullName, phoneNumber = updatedUser.phoneNumber)
    )
  }

  @Test
  fun `when the user gets logged out, there must not be any error`() {
    // given
    whenever(userSession.loggedInUser()).doReturn(Observable.just<Optional<User>>(Optional.empty()))

    // when
    testCase.dispatch(LoadUserDetailsEffect)

    // then
    testCase.assertNoOutgoingEvents()
  }

  @Test
  fun `when the load current language effect is received, the current language must be fetched`() {
    // given
    val language = SystemDefaultLanguage
    whenever(settingsRepository.getCurrentLanguage()).doReturn(Single.just<Language>(language))

    // when
    testCase.dispatch(LoadCurrentLanguageEffect)

    // then
    testCase.assertOutgoingEvents(CurrentLanguageLoaded(language))
  }

  @Test
  fun `when the open language selection screen effect is received, the open language screen ui action must be invoked`() {
    // when
    testCase.dispatch(OpenLanguageSelectionScreenEffect)

    // then
    verify(uiActions).openLanguageSelectionScreen()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when load app version effect is received, the app version must be loaded`() {
    // given
    val versionName = "1.0.0"
    whenever(appVersionFetcher.appVersion()) doReturn versionName

    // when
    testCase.dispatch(LoadAppVersionEffect)

    // then
    verifyNoInteractions(uiActions)
    testCase.assertOutgoingEvents(AppVersionLoaded(versionName))
  }

  @Test
  fun `when check app update availability effect is received, then app needs to check if an update is available`() {
    // given
    val appUpdateState = ShowAppUpdate(appUpdateNudgePriority = null, appStaleness = null)
    whenever(checkAppUpdateAvailability.listenAllUpdates()) doReturn Observable.just<AppUpdateState>(appUpdateState)

    // when
    testCase.dispatch(CheckAppUpdateAvailable)

    // then
    verifyNoInteractions(uiActions)
    testCase.assertOutgoingEvents(AppUpdateAvailabilityChecked(true))
  }

  @Test
  fun `when logout effect is received, then logout the user`() {
    // given
    whenever(userSession.logout()) doReturn Single.just(LogoutResult.Success)

    // when
    testCase.dispatch(LogoutUser)

    // then
    verify(userSession).logout()
    verifyNoMoreInteractions(userSession)

    verifyNoInteractions(uiActions)

    testCase.assertOutgoingEvents(UserLogoutResult(LogoutResult.Success))
  }

  @Test
  fun `when show logout confirmation dialog effect is received, then show logout confirmation dialog`() {
    // when
    testCase.dispatch(ShowConfirmLogoutDialog)

    // then
    verifyNoInteractions(userSession)

    verify(uiActions).showConfirmLogoutDialog()
    verifyNoMoreInteractions(uiActions)

    testCase.assertNoOutgoingEvents()
  }

  @Test
  fun `when restart app effect is received, then restart the app`() {
    // when
    testCase.dispatch(RestartApp)

    // then
    verifyNoInteractions(userSession)

    verify(uiActions).restartApp()
    verifyNoMoreInteractions(uiActions)

    testCase.assertNoOutgoingEvents()
  }
}
