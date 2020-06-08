package org.simple.clinic.onboarding

import com.f2prateek.rx.preferences2.Preference
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import org.simple.mobius.migration.MobiusTestFixture

class OnboardingScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val onboardingUi = mock<OnboardingUi>()
  private val hasUserCompletedOnboarding = mock<Preference<Boolean>>()

  private val uiEvents = PublishSubject.create<OnboardingEvent>()

  private val effectHandler = OnboardingEffectHandler(
      hasUserCompletedOnboarding,
      TrampolineSchedulersProvider(),
      onboardingUi
  ).build()

  @Before
  fun setUp() {
    MobiusTestFixture<OnboardingModel, OnboardingEvent, OnboardingEffect>(
        uiEvents,
        OnboardingModel,
        null,
        OnboardingUpdate(),
        effectHandler,
        { /* No-op */ }
    ).start()
  }

  @Test
  fun `when the onboarding action is done, it should set the preference and open the registration screen`() {
    uiEvents.onNext(GetStartedClicked)

    verify(hasUserCompletedOnboarding).set(eq(true))
    verify(onboardingUi).moveToRegistrationScreen()
  }
}
