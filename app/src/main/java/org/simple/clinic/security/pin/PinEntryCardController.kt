package org.simple.clinic.security.pin

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.security.ComparisonResult.DIFFERENT
import org.simple.clinic.security.ComparisonResult.SAME
import org.simple.clinic.security.PasswordHasher
import org.simple.clinic.security.pin.BruteForceProtection.ProtectedState
import org.simple.clinic.security.pin.PinEntryCardView.State
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import java.util.concurrent.TimeUnit
import javax.inject.Inject

typealias Ui = PinEntryCardView
typealias UiChange = (Ui) -> Unit

class PinEntryCardController @Inject constructor(
    private val passwordHasher: PasswordHasher,
    private val utcClock: UtcClock,
    private val bruteForceProtection: BruteForceProtection
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(autoSubmitPin())
        .compose(ReportAnalyticsEvents())
        .replay()

    return Observable.merge(
        validatePin(replayedEvents),
        blockWhenAuthenticationLimitIsReached(replayedEvents),
        updateIncorrectPinError(replayedEvents))
  }

  private fun autoSubmitPin(): ObservableTransformer<UiEvent, UiEvent> {
    return ObservableTransformer { events ->
      val pinSubmitClicks = events
          .ofType<PinTextChanged>()
          .filter { it.pin.length == 4 }
          .map { PinSubmitClicked(it.pin) }

      events.mergeWith(pinSubmitClicks)
    }
  }

  private fun validatePin(events: Observable<UiEvent>): Observable<UiChange> {
    val pinDigestToVerifyStream = events
        .ofType<PinDigestToVerify>()
        .map { it.pinDigest }

    val submittedPinStream = events
        .ofType<PinSubmitClicked>()
        .map { it.pin }

    return Observables.combineLatest(pinDigestToVerifyStream, submittedPinStream)
        .flatMap { (pinDigestToVerify, submittedPin) ->
          val cachedPinValidation = passwordHasher.compare(pinDigestToVerify, submittedPin)
              .toObservable()
              .replay()
              .refCount()

          val progressUiChanges = cachedPinValidation
              .filter { it == DIFFERENT }
              .map { { ui: Ui -> ui.moveToState(State.PinEntry) } }
              .startWith { ui: Ui ->
                ui.hideError()
                ui.moveToState(State.Progress)
              }

          val recordAttempts = cachedPinValidation
              .switchMap {
                when (it) {
                  SAME -> bruteForceProtection.recordSuccessfulAuthentication()
                  DIFFERENT -> bruteForceProtection.incrementFailedAttempt()
                }.andThen(Observable.empty<UiChange>())
              }

          val validationResultUiChange = cachedPinValidation
              .map {
                when (it) {
                  SAME -> { ui: Ui -> ui.dispatchAuthenticatedCallback(submittedPin) }
                  DIFFERENT -> { ui: Ui -> ui.clearPin() }
                }
              }

          Observable.mergeArray(progressUiChanges, recordAttempts, validationResultUiChange)
        }
  }

  private fun blockWhenAuthenticationLimitIsReached(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<PinEntryViewCreated>()
        .flatMap { bruteForceProtection.protectedStateChanges() }
        .switchMap { state ->
          when (state) {
            is ProtectedState.Allowed -> {
              Observable.just({ ui: Ui -> ui.moveToState(State.PinEntry) })
            }

            is ProtectedState.Blocked -> {
              Observable.interval(1L, TimeUnit.SECONDS)
                  .startWith(0L)  // initial item.
                  .map {
                    val formattedTimeRemaining = formatTimeRemainingTill(state.blockedTill)
                    return@map { ui: Ui ->
                      ui.moveToState(State.BruteForceLocked(formattedTimeRemaining))
                    }
                  }
            }
          }
        }
  }

  private fun formatTimeRemainingTill(futureTime: Instant): TimerDuration {
    val secondsRemaining = futureTime.epochSecond - Instant.now(utcClock).epochSecond
    val secondsPerHour = Duration.ofHours(1).seconds
    val secondsPerMinute = Duration.ofMinutes(1).seconds

    val minutes = (secondsRemaining % secondsPerHour / secondsPerMinute).toString()
    val seconds = (secondsRemaining % secondsPerMinute).toString()

    val minutesWithPadding = minutes.padStart(2, padChar = '0')
    val secondsWithPadding = seconds.padStart(2, padChar = '0')

    return TimerDuration(minutesWithPadding, secondsWithPadding)
  }

  private fun updateIncorrectPinError(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<PinEntryViewCreated>()
        .flatMap { bruteForceProtection.protectedStateChanges() }
        .map { state ->
          when (state) {
            is ProtectedState.Allowed -> {
              when (state.attemptsMade) {
                0 -> { ui: Ui -> ui.hideError() }
                1 -> { ui: Ui -> ui.showIncorrectPinErrorForFirstAttempt() }
                else -> { ui: Ui -> ui.showIncorrectPinErrorOnSubsequentAttempts(remaining = state.attemptsRemaining) }
              }
            }

            is ProtectedState.Blocked -> {
              { ui: Ui -> ui.showIncorrectAttemptsLimitReachedError(state.attemptsMade) }
            }
          }
        }
  }
}
