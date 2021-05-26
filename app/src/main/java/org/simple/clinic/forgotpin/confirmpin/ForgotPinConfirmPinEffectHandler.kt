package org.simple.clinic.forgotpin.confirmpin

import com.spotify.mobius.rx2.RxMobius
import dagger.Lazy
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import org.simple.clinic.facility.Facility
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.clearpatientdata.SyncAndClearPatientData
import org.simple.clinic.user.resetpin.ResetPinResult
import org.simple.clinic.user.resetpin.ResetUserPin
import org.simple.clinic.util.scheduler.SchedulersProvider

class ForgotPinConfirmPinEffectHandler @AssistedInject constructor(
    private val userSession: UserSession,
    private val currentUser: Lazy<User>,
    private val currentFacility: Lazy<Facility>,
    private val resetUserPin: ResetUserPin,
    private val syncAndClearPatientData: SyncAndClearPatientData,
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val uiActions: ForgotPinConfirmPinUiActions
) {

  @AssistedFactory
  interface Factory {
    fun create(uiActions: ForgotPinConfirmPinUiActions): ForgotPinConfirmPinEffectHandler
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
      .addAction(ShowUnexpectedError::class.java, uiActions::showUnexpectedError, schedulersProvider.ui())
      .addAction(GoToHomeScreen::class.java, uiActions::goToHomeScreen, schedulersProvider.ui())
      .addTransformer(SyncPatientDataAndResetPin::class.java, syncPatientDataAndResetPin())
      .build()

  private fun syncPatientDataAndResetPin(): ObservableTransformer<SyncPatientDataAndResetPin, ForgotPinConfirmPinEvent> {
    return ObservableTransformer { effects ->
      effects
          .flatMapSingle { (newPin) -> syncAndClearPatientData.run().toSingleDefault(newPin) }
          .flatMapSingle { newPin -> setUserLoggedInStatusToResettingPin().toSingleDefault(newPin) }
          .flatMapSingle(resetUserPin::resetPin)
          .onErrorReturn(ResetPinResult::UnexpectedError)
          .map(::PatientSyncAndResetPinCompleted)
    }
  }

  private fun setUserLoggedInStatusToResettingPin(): Completable {
    return Observable
        .fromCallable { currentUser.get() }
        .subscribeOn(schedulersProvider.io())
        .flatMapCompletable { user -> userSession.updateLoggedInStatusForUser(user.uuid, User.LoggedInStatus.RESETTING_PIN) }
  }

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
          .map { currentFacility.get() }
          .map(::CurrentFacilityLoaded)
    }
  }

  private fun loadLoggedInUser(): ObservableTransformer<LoadLoggedInUser, ForgotPinConfirmPinEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { currentUser.get() }
          .map(::LoggedInUserLoaded)
    }
  }
}
