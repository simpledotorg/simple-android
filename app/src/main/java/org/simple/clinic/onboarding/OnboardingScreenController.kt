package org.simple.clinic.onboarding

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject
import javax.inject.Named

typealias Ui = OnboardingUi
typealias UiChange = (Ui) -> Unit

class OnboardingScreenController @Inject constructor(
    @Named("onboarding_complete") private val hasUserCompletedOnboarding: Preference<Boolean>
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .replay()

    return getStartedClicks(replayedEvents)
  }

  private fun getStartedClicks(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<OnboardingGetStartedClicked>()
        .flatMapSingle {
          Completable.fromAction { hasUserCompletedOnboarding.set(true) }
              .andThen(Single.just({ ui: Ui -> ui.moveToRegistrationScreen() }))
        }
  }
}
