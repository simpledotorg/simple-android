package org.simple.clinic.user.resetpin

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import io.reactivex.Single
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.forgotpin.ForgotPinResponse
import org.simple.clinic.forgotpin.ResetPinRequest
import org.simple.clinic.login.UsersApi
import org.simple.clinic.security.PasswordHasher
import org.simple.clinic.user.LoggedInUserPayload
import org.simple.clinic.user.User
import org.simple.clinic.user.User.LoggedInStatus.RESET_PIN_REQUESTED
import org.simple.clinic.user.resetpin.ResetPinResult.NetworkError
import org.simple.clinic.user.resetpin.ResetPinResult.Success
import org.simple.clinic.user.resetpin.ResetPinResult.UnexpectedError
import org.simple.clinic.user.resetpin.ResetPinResult.UserNotFound
import org.simple.clinic.util.Just
import org.simple.clinic.util.Optional
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

class ResetUserPin @Inject constructor(
    private val passwordHasher: PasswordHasher,
    // TODO(vs): 2019-11-21 Unify the LoginApi and RegistrationApi into a single UserApi
    private val usersApi: UsersApi,
    private val userDao: User.RoomDao,
    private val facilityRepository: FacilityRepository,
    @Named("preference_access_token") private val accessTokenPreference: Preference<Optional<String>>
) {

  fun resetPin(pin: String): Single<ResetPinResult> {
    return Single.fromCallable { passwordHasher.hash(pin) }
        .doOnSubscribe { Timber.i("Resetting PIN") }
        .map(::ResetPinRequest)
        .flatMap(usersApi::resetPin)
        .flatMapCompletable(::storeUserAndAccessToken)
        .toSingleDefault(Success as ResetPinResult)
        .onErrorReturn(::mapErrorToResetPinResult)
  }

  private fun mapErrorToResetPinResult(error: Throwable): ResetPinResult {
    return when (error) {
      is IOException -> NetworkError
      is HttpException -> if (error.code() == 401) UserNotFound else UnexpectedError(error)
      else -> UnexpectedError(error)
    }
  }

  private fun storeUserAndAccessToken(response: ForgotPinResponse): Completable {
    Timber.i("Storing user and access token. Is token blank? ${response.accessToken.isBlank()}")
    accessTokenPreference.set(Just(response.accessToken))

    val user = userFromPayload(response.loggedInUser)
    return storeUser(user, response.loggedInUser.registrationFacilityId)
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
          loggedInStatus = RESET_PIN_REQUESTED,
          registrationFacilityUuid = registrationFacilityId,
          currentFacilityUuid = registrationFacilityId,
          teleconsultPhoneNumber = teleconsultPhoneNumber
      )
    }
  }

  private fun storeUser(user: User, facilityUuid: UUID): Completable {
    return Completable
        .fromAction { userDao.createOrUpdate(user) }
        .andThen(facilityRepository.setCurrentFacility(user, facilityUuid = facilityUuid))
        .doOnError(Timber::e)
  }

}
