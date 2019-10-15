package org.simple.clinic.settings

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.After
import org.junit.Test
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import org.simple.clinic.util.toOptional
import java.util.UUID

class SettingsEffectHandlerTest {

  private val userSession = mock<UserSession>()
  private val settingsRepository = mock<SettingsRepository>()

  private val testCase = EffectHandlerTestCase(SettingsEffectHandler.create(
      userSession = userSession,
      settingsRepository = settingsRepository,
      schedulersProvider = TrampolineSchedulersProvider()
  ))

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `user details must be fetched when the load user details event is received`() {
    // given
    val savedUser = PatientMocker.loggedInUser(
        uuid = UUID.fromString("eadfd8ef-6c88-4bc9-ba96-e05bec2d6d8b"),
        name = "Mahalakshmi Puri",
        phone = "1234567890"
    )
    val updatedUser = savedUser.copy(fullName = "Mahalakshmi T Sharma", phoneNumber = "0987654321")
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
    whenever(userSession.loggedInUser()).doReturn(Observable.just<Optional<User>>(None))

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
}
