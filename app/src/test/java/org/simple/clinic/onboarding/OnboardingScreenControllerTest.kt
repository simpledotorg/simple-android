package org.simple.clinic.onboarding

import com.f2prateek.rx.preferences2.Preference
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.widgets.UiEvent

class OnboardingScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val screen = mock<OnboardingUi>()
  private val hasUserCompletedOnboarding = mock<Preference<Boolean>>()

  private val uiEvents = PublishSubject.create<UiEvent>()
  lateinit var controller: OnboardingScreenController

  @Before
  fun setUp() {
    controller = OnboardingScreenController(hasUserCompletedOnboarding)

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when the onboarding action is done, it should set the preference and open the registration screen`() {
    uiEvents.onNext(OnboardingGetStartedClicked())

    verify(hasUserCompletedOnboarding).set(eq(true))
    verify(screen).moveToRegistrationScreen()
  }
}
