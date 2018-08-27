package org.simple.clinic.onboarding

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.registration.RegistrationConfig
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject
import javax.inject.Named

typealias Ui = OnboardingScreen
typealias UiChange = (Ui) -> Unit

class OnboardingScreenController @Inject constructor(
    private val registrationConfig: Single<RegistrationConfig>,
    @Named("onboarding_complete") private val hasUserCompletedOnboarding: Preference<Boolean>
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.compose(ReportAnalyticsEvents()).replay(1).refCount()
    return getStartedClicks(replayedEvents)
  }

  private fun getStartedClicks(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<OnboardingGetStartedClicked>()
        .withLatestFrom(registrationConfig.toObservable())
        .flatMapSingle { (_, config) ->
          Completable.fromAction { hasUserCompletedOnboarding.set(true) }
              .andThen(Single.just({ ui: Ui ->
                when (config.isRegistrationEnabled) {
                  true -> ui.moveToRegistrationScreen()
                  else -> ui.moveToLoginScreen()
                }
              }))
        }
  }
}
