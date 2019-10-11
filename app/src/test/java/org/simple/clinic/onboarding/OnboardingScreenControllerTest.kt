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
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import org.simple.mobius.migration.MobiusTestFixture

class OnboardingScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val onboardingUi = mock<OnboardingUi>()
  private val hasUserCompletedOnboarding = mock<Preference<Boolean>>()

  private val uiEvents = PublishSubject.create<OnboardingEvent>()

  @Before
  fun setUp() {
    val onboardingEffectHandler = OnboardingEffectHandler
        .createEffectHandler(hasUserCompletedOnboarding, onboardingUi, TrampolineSchedulersProvider())

    MobiusTestFixture<OnboardingModel, OnboardingEvent, OnboardingEffect>(
        uiEvents,
        OnboardingModel,
        null,
        OnboardingUpdate(),
        onboardingEffectHandler,
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
