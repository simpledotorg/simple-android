package org.simple.clinic.security.pin

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import org.simple.clinic.security.ComparisonResult.DIFFERENT
import org.simple.clinic.security.ComparisonResult.SAME
import org.simple.clinic.security.PasswordHasher
import org.simple.clinic.util.scheduler.SchedulersProvider

class PinEntryEffectHandler @AssistedInject constructor(
    private val passwordHasher: PasswordHasher,
    private val bruteForceProtection: BruteForceProtection,
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val uiActions: UiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: UiActions): PinEntryEffectHandler
  }

  fun build(): ObservableTransformer<PinEntryEffect, PinEntryEvent> {
    return RxMobius
        .subtypeEffectHandler<PinEntryEffect, PinEntryEvent>()
        .addTransformer(ValidateEnteredPin::class.java, validateEnteredPin(schedulersProvider.computation()))
        .addTransformer(LoadPinEntryProtectedStates::class.java, loadPinEntryProtectedStates(schedulersProvider.io()))
        .addAction(HideError::class.java, { uiActions.hideError() }, schedulersProvider.ui())
        .addConsumer(ShowIncorrectPinError::class.java, { showIncorrectPinError(it.attemptsMade, it.attemptsRemaining) }, schedulersProvider.ui())
        .addConsumer(ShowIncorrectPinLimitReachedError::class.java, { uiActions.showIncorrectAttemptsLimitReachedError(it.attemptsMade) }, schedulersProvider.ui())
        .build()
  }

  private fun validateEnteredPin(
      scheduler: Scheduler
  ): ObservableTransformer<ValidateEnteredPin, PinEntryEvent> {
    return ObservableTransformer { effects ->
      effects
          .flatMapSingle {
            passwordHasher
                .compare(hashed = it.pinDigest, password = it.enteredPin)
                .subscribeOn(scheduler)
          }
          .map { result ->
            when (result) {
              SAME -> CorrectPinEntered
              DIFFERENT -> WrongPinEntered
            }
          }
    }
  }

  private fun loadPinEntryProtectedStates(
      scheduler: Scheduler
  ): ObservableTransformer<LoadPinEntryProtectedStates, PinEntryEvent> {
    return ObservableTransformer { effects ->
      effects
          .switchMap { bruteForceProtection.protectedStateChanges().subscribeOn(scheduler) }
          .map(::PinEntryStateChanged)
    }
  }

  private fun showIncorrectPinError(
      attemptsMade: Int,
      attemptsRemaining: Int
  ) {
    if (attemptsMade == 1) {
      uiActions.showIncorrectPinErrorForFirstAttempt()
    } else {
      uiActions.showIncorrectPinErrorOnSubsequentAttempts(remaining = attemptsRemaining)
    }
  }
}
