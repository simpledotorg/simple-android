package org.simple.clinic.registration.register

import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.security.PasswordHasher
import org.simple.clinic.user.User
import org.simple.clinic.user.UserStatus
import org.simple.clinic.user.registeruser.RegisterUser
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.scheduler.SchedulersProvider
import java.time.Instant

class RegistrationLoadingEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    private val registerUser: RegisterUser,
    private val clock: UtcClock,
    private val passwordHasher: PasswordHasher,
    @Assisted private val uiActions: RegistrationLoadingUiActions
) {

  @AssistedFactory
  interface Factory {
    fun create(uiActions: RegistrationLoadingUiActions): RegistrationLoadingEffectHandler
  }

  fun build(): ObservableTransformer<RegistrationLoadingEffect, RegistrationLoadingEvent> {
    return RxMobius
        .subtypeEffectHandler<RegistrationLoadingEffect, RegistrationLoadingEvent>()
        .addTransformer(RegisterUserAtFacility::class.java, registerUserAtFacility())
        .addAction(GoToHomeScreen::class.java, uiActions::openHomeScreen, schedulers.ui())
        .addTransformer(ConvertRegistrationEntryToUserDetails::class.java, convertRegistrationEntryToUser())
        .build()
  }

  private fun registerUserAtFacility(): ObservableTransformer<RegisterUserAtFacility, RegistrationLoadingEvent> {
    return ObservableTransformer { effects ->
      effects
          .switchMapSingle { effect ->
            registerUser
                .registerUserAtFacility(effect.user)
                .subscribeOn(schedulers.io())
                .map { UserRegistrationCompleted(RegisterUserResult.from(it)) }
          }
    }
  }

  private fun convertRegistrationEntryToUser(): ObservableTransformer<ConvertRegistrationEntryToUserDetails, RegistrationLoadingEvent> {
    return ObservableTransformer { effects ->
      effects
          .map { it.registrationEntry }
          .map { entry ->
            val now = Instant.now(clock)

            User(
                uuid = entry.uuid!!,
                fullName = entry.fullName!!,
                phoneNumber = entry.phoneNumber!!,
                pinDigest = passwordHasher.hash(entry.pin!!),
                status = UserStatus.WaitingForApproval,
                createdAt = now,
                updatedAt = now,
                loggedInStatus = User.LoggedInStatus.LOGGED_IN,
                registrationFacilityUuid = entry.facilityId!!,
                currentFacilityUuid = entry.facilityId,
                teleconsultPhoneNumber = null,
                capabilities = null
            )
          }
          .map(::ConvertedRegistrationEntryToUserDetails)
    }
  }
}
