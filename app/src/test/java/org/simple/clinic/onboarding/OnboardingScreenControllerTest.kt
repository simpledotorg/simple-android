package org.simple.clinic.onboarding

import com.f2prateek.rx.preferences2.Preference
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.spotify.mobius.Next.noChange
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.util.RxErrorsRule
import org.simple.mobius.migration.MobiusTestFixture

class OnboardingScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val screen = mock<OnboardingUi>()
  private val hasUserCompletedOnboarding = mock<Preference<Boolean>>()

  private val uiEvents = PublishSubject.create<OnboardingEvent>()
  lateinit var controller: OnboardingScreenController

  @Before
  fun setUp() {
    controller = OnboardingScreenController(hasUserCompletedOnboarding)

    val sharedOnboardingEvents = uiEvents.hide().share()

    sharedOnboardingEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }

    MobiusTestFixture<OnboardingModel, OnboardingEvent, OnboardingEffect>(
        sharedOnboardingEvents,
        OnboardingModel,
        null,
        { model: OnboardingModel, event: OnboardingEvent -> noChange<OnboardingModel, OnboardingEffect>() },
        OnboardingEffectHandler.createEffectHandler(),
        { /* No-op */ }
    ).start()
  }

  @Test
  fun `when the onboarding action is done, it should set the preference and open the registration screen`() {
    uiEvents.onNext(GetStartedClicked)

    verify(hasUserCompletedOnboarding).set(eq(true))
    verify(screen).moveToRegistrationScreen()
  }
}
