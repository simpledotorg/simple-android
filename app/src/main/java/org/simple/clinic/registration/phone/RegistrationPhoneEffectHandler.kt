package org.simple.clinic.registration.phone

import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import org.simple.clinic.facility.FacilitySync
import org.simple.clinic.user.OngoingLoginEntry
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.finduser.UserLookup
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.uuid.UuidGenerator

class RegistrationPhoneEffectHandler @AssistedInject constructor(
    @Assisted private val uiActions: RegistrationPhoneUiActions,
    private val schedulers: SchedulersProvider,
    private val userSession: UserSession,
    private val uuidGenerator: UuidGenerator,
    private val numberValidator: PhoneNumberValidator,
    private val facilitySync: FacilitySync,
    private val userLookup: UserLookup
) {

  @AssistedFactory
  interface Factory {
    fun create(uiActions: RegistrationPhoneUiActions): RegistrationPhoneEffectHandler
  }

  fun build(): ObservableTransformer<RegistrationPhoneEffect, RegistrationPhoneEvent> {
    return RxMobius
        .subtypeEffectHandler<RegistrationPhoneEffect, RegistrationPhoneEvent>()
        .addConsumer(PrefillFields::class.java, { uiActions.preFillUserDetails(it.entry) }, schedulers.ui())
        .addTransformer(ValidateEnteredNumber::class.java, validateEnteredPhoneNumber())
        .addTransformer(SyncFacilities::class.java, syncFacilities())
        .addTransformer(SearchForExistingUser::class.java, findUserByPhoneNumber())
        .addConsumer(ShowAccessDeniedScreen::class.java, { uiActions.showAccessDeniedScreen(it.number) }, schedulers.ui())
        .addTransformer(CreateUserLocally::class.java, createUserLocally())
        .addAction(ProceedToLogin::class.java, uiActions::openLoginPinEntryScreen, schedulers.ui())
        .addTransformer(LoadCurrentUserUnauthorizedStatus::class.java, loadCurrentUserUnauthorizedStatus())
        .addAction(ShowUserLoggedOutAlert::class.java, uiActions::showLoggedOutOfDeviceDialog, schedulers.ui())
        .addConsumer(ContinueRegistration::class.java, { uiActions.openRegistrationNameEntryScreen(it.entry) }, schedulers.ui())
        .build()
  }

  private fun validateEnteredPhoneNumber(): ObservableTransformer<ValidateEnteredNumber, RegistrationPhoneEvent> {
    return ObservableTransformer { effects ->
      effects
          .map { numberValidator.validate(it.number, PhoneNumberValidator.Type.MOBILE) }
          .map { EnteredNumberValidated.fromValidateNumberResult(it) }
    }
  }

  private fun syncFacilities(): ObservableTransformer<SyncFacilities, RegistrationPhoneEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulers.io())
          .map { facilitySync.pullWithResult() }
          .map { FacilitiesSynced.fromFacilityPullResult(it) }
    }
  }

  private fun findUserByPhoneNumber(): ObservableTransformer<SearchForExistingUser, RegistrationPhoneEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulers.io())
          .map { userLookup.find(it.number) }
          .map { SearchForExistingUserCompleted.fromFindUserResult(it) }
    }
  }

  private fun createUserLocally(): ObservableTransformer<CreateUserLocally, RegistrationPhoneEvent> {
    return ObservableTransformer { effects ->
      effects
          .map {
            OngoingLoginEntry(
                uuid = it.userUuid,
                phoneNumber = it.number,
                status = it.status,
                capabilities = null
            )
          }
          .flatMapSingle {
            userSession
                .saveOngoingLoginEntry(it)
                .andThen(Single.just(UserCreatedLocally as RegistrationPhoneEvent))
          }
    }
  }

  private fun loadCurrentUserUnauthorizedStatus(): ObservableTransformer<LoadCurrentUserUnauthorizedStatus, RegistrationPhoneEvent> {
    return ObservableTransformer { effects ->
      effects
          .flatMapSingle {
            userSession
                .isUserUnauthorized()
                .subscribeOn(schedulers.io())
                .firstOrError()
          }
          .map(::CurrentUserUnauthorizedStatusLoaded)
    }
  }
}
