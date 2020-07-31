package org.simple.clinic.forgotpin.confirmpin

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.extractIfPresent
import org.simple.clinic.util.scheduler.SchedulersProvider

class ForgotPinConfirmPinEffectHandler @AssistedInject constructor(
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository,
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val uiActions: UiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: UiActions): ForgotPinConfirmPinEffectHandler
  }

  fun build(): ObservableTransformer<ForgotPinConfirmPinEffect, ForgotPinConfirmPinEvent> = RxMobius
      .subtypeEffectHandler<ForgotPinConfirmPinEffect, ForgotPinConfirmPinEvent>()
      .addTransformer(LoadLoggedInUser::class.java, loadLoggedInUser())
      .addTransformer(LoadCurrentFacility::class.java, loadCurrentFacility())
      .addAction(HideError::class.java, uiActions::hideError, schedulersProvider.ui())
      .addTransformer(ValidatePinConfirmation::class.java, validatePinConfirmation())
      .addAction(ShowMismatchedError::class.java, uiActions::showPinMismatchedError, schedulersProvider.ui())
      .addAction(ShowProgress::class.java, uiActions::showProgress, schedulersProvider.ui())
      .addAction(ShowNetworkError::class.java, uiActions::showNetworkError, schedulersProvider.ui())
      .build()

  private fun validatePinConfirmation(): ObservableTransformer<ValidatePinConfirmation, ForgotPinConfirmPinEvent> {
    return ObservableTransformer { effects ->
      effects
          .map { (previousPin, enteredPin) ->
            val isValid = previousPin == enteredPin
            PinConfirmationValidated(isValid, enteredPin)
          }
    }
  }

  private fun loadCurrentFacility(): ObservableTransformer<LoadCurrentFacility, ForgotPinConfirmPinEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .flatMap { userSession.loggedInUser() }
          .extractIfPresent()
          .flatMap { facilityRepository.currentFacility(it) }
          .map(::CurrentFacilityLoaded)
    }
  }

  private fun loadLoggedInUser(): ObservableTransformer<LoadLoggedInUser, ForgotPinConfirmPinEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .flatMap { userSession.loggedInUser() }
          .extractIfPresent()
          .map(::LoggedInUserLoaded)
    }
  }
}
