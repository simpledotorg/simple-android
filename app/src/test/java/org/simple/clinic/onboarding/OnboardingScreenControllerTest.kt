package org.simple.clinic.onboarding

import com.f2prateek.rx.preferences2.Preference
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.simple.clinic.registration.RegistrationConfig
import org.simple.clinic.widgets.UiEvent

class OnboardingScreenControllerTest {

  private val screen = mock<OnboardingScreen>()
  private val hasUserCompletedOnboarding = mock<Preference<Boolean>>()
  private val registrationConfig = PublishSubject.create<RegistrationConfig>()

  private val uiEvents = PublishSubject.create<UiEvent>()
  lateinit var controller: OnboardingScreenController

  @Before
  fun setUp() {
    controller = OnboardingScreenController(registrationConfig.firstOrError(), hasUserCompletedOnboarding)

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when the onboarding action is done, it should set the preference and open the registration screen`() {
    registrationConfig.onNext(RegistrationConfig(isRegistrationEnabled = true, retryBackOffDelayInMinutes = 0))
    uiEvents.onNext(OnboardingGetStartedClicked())

    verify(hasUserCompletedOnboarding).set(eq(true))
    verify(screen).moveToRegistrationScreen()
  }

  @Test
  fun `when registration feature is not enabled and get started is clicked then login screen should be opened`() {
    registrationConfig.onNext(RegistrationConfig(isRegistrationEnabled = false, retryBackOffDelayInMinutes = 0))

    uiEvents.onNext(OnboardingGetStartedClicked())
    verify(screen).moveToLoginScreen()
  }
}
