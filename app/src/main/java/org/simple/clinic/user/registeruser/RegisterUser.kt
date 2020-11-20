package org.simple.clinic.user.registeruser

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import io.reactivex.Single
import org.simple.clinic.login.UsersApi
import org.simple.clinic.platform.analytics.Analytics
import org.simple.clinic.platform.analytics.AnalyticsUser
import org.simple.clinic.registration.RegistrationRequest
import org.simple.clinic.registration.RegistrationResponse
import org.simple.clinic.user.LoggedInUserPayload
import org.simple.clinic.user.User
import org.simple.clinic.user.User.LoggedInStatus.LOGGED_IN
import org.simple.clinic.user.registeruser.RegistrationResult.NetworkError
import org.simple.clinic.user.registeruser.RegistrationResult.Success
import org.simple.clinic.user.registeruser.RegistrationResult.UnexpectedError
import org.simple.clinic.util.Just
import org.simple.clinic.util.Optional
import timber.log.Timber
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

class RegisterUser @Inject constructor(
    private val usersApi: UsersApi,
    private val userDao: User.RoomDao,
    @Named("preference_access_token") private val accessTokenPreference: Preference<Optional<String>>
) {

  fun registerUserAtFacility(user: User): Single<RegistrationResult> {
    val registrationRequest = RegistrationRequest(userToPayload(user, user.registrationFacilityUuid))

    return usersApi
        .createUser(registrationRequest)
        .doOnSubscribe { Timber.i("Registering user") }
        .flatMap { storeUserAndAccessToken(it) }
        .map { AnalyticsUser(it.uuid, it.fullName) }
        .doOnSuccess(Analytics::setNewlyRegisteredUser)
        .map { Success as RegistrationResult }
        .doOnError { reportErrors(it) }
        .onErrorReturn { mapErrorToRegistrationResult(it) }
        .doOnSuccess { Timber.i("Registration result: $it") }

  }

  private fun reportErrors(error: Throwable) {
    if (error !is IOException) {
      Timber.e(error)
    }
  }

  private fun mapErrorToRegistrationResult(e: Throwable): RegistrationResult {
    return when (e) {
      is IOException -> NetworkError
      else -> UnexpectedError
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
          updatedAt = updatedAt,
          teleconsultPhoneNumber = user.teleconsultPhoneNumber,
          capabilities = user.capabilities
      )
    }
  }

  private fun storeUserAndAccessToken(response: RegistrationResponse): Single<User> {
    val user = userFromPayload(response.userPayload)

    return Completable
        .fromAction { userDao.createOrUpdate(user) }
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
          loggedInStatus = LOGGED_IN,
          registrationFacilityUuid = payload.registrationFacilityId,
          currentFacilityUuid = payload.registrationFacilityId,
          teleconsultPhoneNumber = payload.teleconsultPhoneNumber,
          capabilities = payload.capabilities
      )
    }
  }
}
