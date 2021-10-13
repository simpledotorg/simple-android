package org.simple.clinic.enterotp

import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.login.LoginUserWithOtp
import org.simple.clinic.login.activateuser.ActivateUser
import org.simple.clinic.sync.DataSync
import org.simple.clinic.user.NewlyVerifiedUser
import org.simple.clinic.user.OngoingLoginEntryRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.scheduler.SchedulersProvider

class EnterOtpEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    private val userSession: UserSession,
    private val dataSync: DataSync,
    private val ongoingLoginEntryRepository: OngoingLoginEntryRepository,
    private val loginUserWithOtp: LoginUserWithOtp,
    private val activateUser: ActivateUser,
    private val bruteForceProtection: BruteForceOtpEntryProtection,
    @Assisted private val viewEffectsConsumer: Consumer<EnterOtpViewEffect>
) {

  @AssistedFactory
  interface Factory {
    fun create(
        viewEffectsConsumer: Consumer<EnterOtpViewEffect>
    ): EnterOtpEffectHandler
  }

  fun build(): ObservableTransformer<EnterOtpEffect, EnterOtpEvent> {
    return RxMobius
        .subtypeEffectHandler<EnterOtpEffect, EnterOtpEvent>()
        .addTransformer(LoadUser::class.java, loadUser())
        .addAction(TriggerSync::class.java, dataSync::fireAndForgetSync)
        .addAction(ClearLoginEntry::class.java, ongoingLoginEntryRepository::clearLoginEntry)
        .addTransformer(LoginUser::class.java, loginUser())
        .addTransformer(ListenForUserBackgroundVerification::class.java, waitForUserBackgroundVerifications())
        .addTransformer(RequestLoginOtp::class.java, activateUser())
        .addConsumer(FailedLoginOtpAttempt::class.java, { bruteForceProtection.incrementFailedOtpAttempt() }, schedulers.io())
        .addTransformer(LoadOtpEntryProtectedStates::class.java, loadOtpEntryStates())
        .addAction(ResetOtpAttemptLimit::class.java, { bruteForceProtection.resetFailedAttempts() }, schedulers.io())
        .addConsumer(EnterOtpViewEffect::class.java, viewEffectsConsumer::accept)
        .build()
  }

  private fun loadOtpEntryStates(): ObservableTransformer<LoadOtpEntryProtectedStates, EnterOtpEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulers.io())
          .switchMap { bruteForceProtection.protectedStateChanges() }
          .map(::OtpEntryProtectedStateChanged)
    }
  }

  private fun loadUser(): ObservableTransformer<LoadUser, EnterOtpEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulers.io())
          .map { userSession.loggedInUserImmediate()!! }
          .map(::UserLoaded)
    }
  }

  private fun loginUser(): ObservableTransformer<LoginUser, EnterOtpEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulers.io())
          .switchMapSingle { effect ->
            val otp = effect.otp
            val entry = ongoingLoginEntryRepository.entryImmediate()

            loginUserWithOtp.loginWithOtp(
                phoneNumber = entry.phoneNumber!!,
                pin = entry.pin!!,
                otp = otp
            )
          }
          .map(::LoginUserCompleted)
    }
  }

  private fun waitForUserBackgroundVerifications(): ObservableTransformer<ListenForUserBackgroundVerification, EnterOtpEvent> {
    return ObservableTransformer { effects ->
      effects
          .switchMap {
            userSession
                .loggedInUser()
                .subscribeOn(schedulers.io())
                .compose(NewlyVerifiedUser())
                .map { UserVerifiedInBackground }
          }
    }
  }

  private fun activateUser(): ObservableTransformer<RequestLoginOtp, EnterOtpEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulers.io())
          .map {
            val entry = ongoingLoginEntryRepository.entryImmediate()

            val result = activateUser.activate(entry.uuid, entry.pin!!)

            RequestLoginOtpCompleted(result)
          }
    }
  }
}
