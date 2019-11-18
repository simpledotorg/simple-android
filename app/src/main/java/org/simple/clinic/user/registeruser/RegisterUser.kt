package org.simple.clinic.user.registeruser

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import io.reactivex.Single
import org.simple.clinic.analytics.Analytics
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.registration.RegistrationApi
import org.simple.clinic.registration.RegistrationRequest
import org.simple.clinic.registration.RegistrationResponse
import org.simple.clinic.user.LoggedInUserPayload
import org.simple.clinic.user.User
import org.simple.clinic.user.User.LoggedInStatus.LOGGED_IN
import org.simple.clinic.util.Just
import org.simple.clinic.util.Optional
import timber.log.Timber
import java.io.IOException
import java.util.UUID
import javax.inject.Named

class RegisterUser(
    private val registrationApi: RegistrationApi,
    private val userDao: User.RoomDao,
    private val facilityRepository: FacilityRepository,
    @Named("preference_access_token") private val accessTokenPreference: Preference<Optional<String>>
) {

  fun registerUserAtFacility(user: User, facility: Facility): Single<RegistrationResult> {
    return Single.fromCallable { userToPayload(user, facility.uuid) }
        .doOnSubscribe { Timber.i("Registering user") }
        .map(::RegistrationRequest)
        .flatMap(registrationApi::createUser)
        .flatMap(::storeUserAndAccessToken)
        .doOnSuccess(Analytics::setNewlyRegisteredUser)
        .map { RegistrationResult.Success as RegistrationResult }
        .onErrorReturn(::mapErrorToRegistrationResult)
        .doOnSuccess { Timber.i("Registration result: $it") }

  }

  private fun mapErrorToRegistrationResult(e: Throwable): RegistrationResult {
    return when (e) {
      is IOException -> RegistrationResult.NetworkError
      else -> {
        Timber.e(e)
        RegistrationResult.UnexpectedError
      }
    }
  }

  private fun userToPayload(user: User, registrationFacilityUuid: UUID): LoggedInUserPayload {
    return with(user) {
      LoggedInUserPayload(
          uuid = uuid,
          fullName = fullName,
          phoneNumber = phoneNumber,
          pinDigest = pinDigest,
          registrationFacilityId = registrationFacilityUuid,
          status = status,
          createdAt = createdAt,
          updatedAt = updatedAt
      )
    }
  }

  private fun storeUserAndAccessToken(response: RegistrationResponse): Single<User> {
    val user = userFromPayload(response.userPayload)
    val facilityUuid = response.userPayload.registrationFacilityId

    return Completable
        .fromAction { userDao.createOrUpdate(user) }
        .andThen(facilityRepository.associateUserWithFacilities(user, listOf(facilityUuid), currentFacility = facilityUuid))
        .andThen(Completable.fromAction { accessTokenPreference.set(Just(response.accessToken)) })
        .andThen(Single.just(user))
  }

  private fun userFromPayload(payload: LoggedInUserPayload): User {
    return with(payload) {
      User(
          uuid = uuid,
          fullName = fullName,
          phoneNumber = phoneNumber,
          pinDigest = pinDigest,
          status = status,
          createdAt = createdAt,
          updatedAt = updatedAt,
          loggedInStatus = LOGGED_IN
      )
    }
  }
}
