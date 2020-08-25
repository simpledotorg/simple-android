package org.simple.clinic.login

import com.f2prateek.rx.preferences2.Preference
import com.squareup.moshi.Moshi
import io.reactivex.Completable
import io.reactivex.Single
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.platform.analytics.Analytics
import org.simple.clinic.platform.analytics.AnalyticsUser
import org.simple.clinic.user.LoggedInUserPayload
import org.simple.clinic.user.User
import org.simple.clinic.util.Just
import org.simple.clinic.util.Optional
import org.simple.clinic.util.readErrorResponseJson
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named

class LoginUserWithOtp @Inject constructor(
    private val usersApi: UsersApi,
    private val userDao: User.RoomDao,
    private val facilityRepository: FacilityRepository,
    private val moshi: Moshi,
    @Named("preference_access_token") private val accessTokenPreference: Preference<Optional<String>>
) {

  fun loginWithOtp(phoneNumber: String, pin: String, otp: String): Single<LoginResult> {
    return Single.just(UserPayload(phoneNumber, pin, otp))
        .map(::LoginRequest)
        .flatMap(usersApi::login)
        .flatMap(::storeUserAndAccessToken)
        .doOnSuccess(::reportUserLoggedInToAnalytics)
        .map { LoginResult.Success as LoginResult }
        .onErrorReturn(::mapErrorToLoginResult)
        .doOnSuccess { Timber.i("Login result: $it") }
  }

  private fun storeUserAndAccessToken(response: LoginResponse): Single<User> {
    val user = mapPayloadToUser(response.loggedInUser)
    val facilityUuid = response.loggedInUser.registrationFacilityId

    return Completable
        .fromAction { userDao.createOrUpdate(user) }
        .andThen(facilityRepository.setCurrentFacility(facilityUuid))
        .andThen(Completable.fromAction { accessTokenPreference.set(Just(response.accessToken)) })
        .andThen(Single.just(user))
  }

  private fun mapPayloadToUser(payload: LoggedInUserPayload): User {
    return with(payload) {
      User(
          uuid = uuid,
          fullName = fullName,
          phoneNumber = phoneNumber,
          pinDigest = pinDigest,
          status = status,
          createdAt = createdAt,
          updatedAt = updatedAt,
          loggedInStatus = User.LoggedInStatus.LOGGED_IN,
          registrationFacilityUuid = payload.registrationFacilityId,
          currentFacilityUuid = payload.registrationFacilityId,
          teleconsultPhoneNumber = payload.teleconsultPhoneNumber
      )
    }
  }

  private fun mapErrorToLoginResult(error: Throwable): LoginResult {
    return when {
      error is IOException -> LoginResult.NetworkError
      error is HttpException && error.code() == 401 -> {
        val errorResponse = readErrorResponseJson<LoginErrorResponse>(error, moshi)
        LoginResult.ServerError(errorResponse.firstError())
      }
      else -> {
        Timber.e(error)
        LoginResult.UnexpectedError
      }
    }
  }

  private fun reportUserLoggedInToAnalytics(user: User) {
    Analytics.setLoggedInUser(AnalyticsUser(user.uuid, user.fullName))
  }
}
