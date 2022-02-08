package org.simple.clinic.login.pin

import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import org.simple.clinic.user.OngoingLoginEntry
import org.simple.clinic.user.OngoingLoginEntryRepository
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.scheduler.SchedulersProvider

class LoginPinEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    private val userSession: UserSession,
    private val ongoingLoginEntryRepository: OngoingLoginEntryRepository,
    @Assisted private val uiActions: UiActions,
    @Assisted private val viewEffectsConsumer: Consumer<LoginPinViewEffect>
) {

  @AssistedFactory
  interface Factory {
    fun create(
        uiActions: UiActions,
        viewEffectsConsumer: Consumer<LoginPinViewEffect>
    ): LoginPinEffectHandler
  }

  fun build(): ObservableTransformer<LoginPinEffect, LoginPinEvent> = RxMobius
      .subtypeEffectHandler<LoginPinEffect, LoginPinEvent>()
      .addTransformer(LoadOngoingLoginEntry::class.java, loadOngoingLoginEntry())
      .addTransformer(SaveOngoingLoginEntry::class.java, saveOngoingLoginEntry())
      .addTransformer(LoginUser::class.java, loginUser())
      .addTransformer(ClearOngoingLoginEntry::class.java, clearOngoingLoginEntry())
      .addConsumer(LoginPinViewEffect::class.java, viewEffectsConsumer::accept)
      .build()

  private fun clearOngoingLoginEntry(): ObservableTransformer<ClearOngoingLoginEntry, LoginPinEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .doOnNext { ongoingLoginEntryRepository.clearLoginEntry() }
          .map { OngoingLoginEntryCleared }
    }
  }

  private fun loginUser(): ObservableTransformer<LoginUser, LoginPinEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .flatMap { (entry) ->
            userSession.storeUser(
                user = createUserFromLoginEntry(entry)
            ).andThen(Observable.just(UserLoggedIn))
          }
    }
  }

  private fun saveOngoingLoginEntry(): ObservableTransformer<SaveOngoingLoginEntry, LoginPinEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .flatMapSingle { (ongoingLoginEntry) ->
            ongoingLoginEntryRepository
                .saveLoginEntry(ongoingLoginEntry)
                .andThen(Single.just(LoginPinScreenUpdatedLoginEntry(ongoingLoginEntry)))
          }
    }
  }

  private fun loadOngoingLoginEntry(): ObservableTransformer<LoadOngoingLoginEntry, LoginPinEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { ongoingLoginEntryRepository.entryImmediate() }
          .map(::OngoingLoginEntryLoaded)
    }
  }

  private fun createUserFromLoginEntry(entry: OngoingLoginEntry): User {
    return User(
        uuid = entry.uuid,
        fullName = entry.fullName!!,
        phoneNumber = entry.phoneNumber!!,
        pinDigest = entry.pinDigest!!,
        status = entry.status!!,
        createdAt = entry.createdAt!!,
        updatedAt = entry.updatedAt!!,
        loggedInStatus = User.LoggedInStatus.OTP_REQUESTED,
        registrationFacilityUuid = entry.registrationFacilityUuid!!,
        currentFacilityUuid = entry.registrationFacilityUuid,
        teleconsultPhoneNumber = entry.teleconsultPhoneNumber,
        capabilities = entry.capabilities
    )
  }
}
