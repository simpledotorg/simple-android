package org.simple.clinic.user

import android.content.SharedPreferences
import com.f2prateek.rx.preferences2.Preference
import com.squareup.moshi.Moshi
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.simple.clinic.AppDatabase
import org.simple.clinic.di.AppScope
import org.simple.clinic.facility.FacilitySync
import org.simple.clinic.login.LoginApiV1
import org.simple.clinic.login.LoginErrorResponse
import org.simple.clinic.login.LoginRequest
import org.simple.clinic.login.LoginResponse
import org.simple.clinic.login.LoginResult
import org.simple.clinic.login.UserPayload
import org.simple.clinic.registration.RegisterUserPayload
import org.simple.clinic.registration.RegistrationApiV1
import org.simple.clinic.registration.RegistrationRequest
import org.simple.clinic.registration.RegistrationResult
import org.simple.clinic.util.Just
import org.simple.clinic.util.Optional
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named
import kotlin.reflect.KClass

@AppScope
class UserSession @Inject constructor(
    private val loginApi: LoginApiV1,
    private val registrationApi: RegistrationApiV1,
    private val loggedInUserPreference: Preference<Optional<LoggedInUser>>,
    private val moshi: Moshi,
    private val facilitySync: FacilitySync,
    private val sharedPreferences: SharedPreferences,
    private val appDatabase: AppDatabase,
    @Named("preference_access_token") private val accessTokenPreference: Preference<Optional<String>>
) {

  private lateinit var ongoingLoginEntry: OngoingLoginEntry
  private lateinit var ongoingRegistrationEntry: OngoingRegistrationEntry

  fun saveOngoingLoginEntry(entry: OngoingLoginEntry): Completable {
    return Completable.fromAction {
      this.ongoingLoginEntry = entry
    }
  }

  fun ongoingLoginEntry(): Single<OngoingLoginEntry> {
    return Single.fromCallable { ongoingLoginEntry }
  }

  fun login(): Single<LoginResult> {
    return ongoingLoginEntry()
        .map { LoginRequest(UserPayload(it.phoneNumber!!, it.pin!!, it.otp)) }
        .flatMap { loginApi.login(it) }
        .doOnSuccess { storeUserAndAccessToken(it) }
        .flatMap {
          facilitySync
              .sync()
              .andThen(Single.just(it))
        }
        .map<LoginResult> { LoginResult.Success() }
        .onErrorReturn { error ->
          when {
            error is IOException -> LoginResult.NetworkError()
            error is HttpException && error.code() == 401 -> {
              val errorResponse = readErrorResponseJson(error, LoginErrorResponse::class)
              LoginResult.ServerError(errorResponse.firstError())
            }
            else -> {
              Timber.e(error)
              LoginResult.UnexpectedError()
            }
          }
        }
  }

  fun register(): Single<RegistrationResult> {
    return ongoingRegistrationEntry()
        .map { entry ->
          RegisterUserPayload(
              fullName = entry.fullName!!,
              phoneNumber = entry.phoneNumber!!,
              pin = entry.pin!!,
              pinConfirmation = entry.pinConfirmation!!,
              facilityId = "1bb26c0b-e0cb-4d5e-8582-47095a3e18bc",
              createdAt = entry.createdAt!!,
              updatedAt = entry.createdAt
          )
        }
        .flatMap { payload -> registrationApi.createUser(RegistrationRequest(user = payload)) }
        .map<RegistrationResult> { response ->
          storeUser(response.loggedInUser)
          RegistrationResult.Success()
        }
        .onErrorReturn { RegistrationResult.Error() }
  }

  fun saveOngoingRegistrationEntry(entry: OngoingRegistrationEntry): Completable {
    return Completable.fromAction {
      this.ongoingRegistrationEntry = entry
    }
  }

  fun ongoingRegistrationEntry(): Single<OngoingRegistrationEntry> {
    return Single.fromCallable { ongoingRegistrationEntry }
  }

  private fun storeUserAndAccessToken(response: LoginResponse) {
    accessTokenPreference.set(Just(response.accessToken))
    storeUser(response.loggedInUser)
  }

  private fun storeUser(loggedInUser: LoggedInUser) {
    loggedInUserPreference.set(Just(loggedInUser))
  }

  private fun <T : Any> readErrorResponseJson(error: HttpException, clazz: KClass<T>): T {
    val jsonAdapter = moshi.adapter(clazz.java)
    return jsonAdapter.fromJson(error.response().errorBody()!!.source())!!
  }

  fun logout(): Completable {
    return Completable.fromAction {
      sharedPreferences.edit().clear().apply()
      appDatabase.clearAllTables()
    }
  }

  fun loggedInUser(): Observable<Optional<LoggedInUser>> {
    return loggedInUserPreference.asObservable()
  }

  fun isUserLoggedIn(): Boolean {
    return loggedInUserPreference.get() is Just
  }

  fun accessToken(): Optional<String> {
    return accessTokenPreference.get()
  }
}
