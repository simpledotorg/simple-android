package org.simple.clinic.login.pin

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import org.simple.clinic.user.OngoingLoginEntry
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.scheduler.SchedulersProvider

class LoginPinEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    private val userSession: UserSession,
    @Assisted private val uiActions: UiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: UiActions): LoginPinEffectHandler
  }

  fun build(): ObservableTransformer<LoginPinEffect, LoginPinEvent> = RxMobius
      .subtypeEffectHandler<LoginPinEffect, LoginPinEvent>()
      .addTransformer(LoadOngoingLoginEntry::class.java, loadOngoingLoginEntry())
      .addTransformer(SaveOngoingLoginEntry::class.java, saveOngoingLoginEntry())
      .addTransformer(LoginUser::class.java, loginUser())
      .build()

  private fun loginUser(): ObservableTransformer<LoginUser, LoginPinEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .flatMap { (entry) ->
            userSession.storeUser(
                user = createUserFromLoginEntry(entry),
                facilityUuid = entry.registrationFacilityUuid!!
            ).andThen(Observable.just(UserLoggedIn))
          }
    }
  }

  private fun saveOngoingLoginEntry(): ObservableTransformer<SaveOngoingLoginEntry, LoginPinEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .flatMapSingle { (ongoingLoginEntry) ->
            userSession
                .saveOngoingLoginEntry(ongoingLoginEntry)
                .andThen(Single.just(LoginPinScreenUpdatedLoginEntry(ongoingLoginEntry)))
          }
    }
  }

  private fun loadOngoingLoginEntry(): ObservableTransformer<LoadOngoingLoginEntry, LoginPinEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .flatMapSingle { userSession.ongoingLoginEntry() }
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
        currentFacilityUuid = entry.registrationFacilityUuid
    )
  }
}
