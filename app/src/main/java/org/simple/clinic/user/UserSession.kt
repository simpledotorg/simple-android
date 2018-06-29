package org.simple.clinic.user

import com.f2prateek.rx.preferences2.Preference
import com.squareup.moshi.Moshi
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.simple.clinic.di.AppScope
import org.simple.clinic.facility.FacilitySync
import org.simple.clinic.login.LoginApiV1
import org.simple.clinic.login.LoginErrorResponse
import org.simple.clinic.login.LoginRequest
import org.simple.clinic.login.LoginResponse
import org.simple.clinic.login.LoginResult
import org.simple.clinic.login.UserPayload
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named
import kotlin.reflect.KClass

@AppScope
class UserSession @Inject constructor(
    private val api: LoginApiV1,
    private val loggedInUserPreference: Preference<Optional<LoggedInUser>>,
    private val moshi: Moshi,
    private val facilitySync: FacilitySync,
    @Named("preference_access_token") private val accessTokenPreference: Preference<Optional<String>>
) {

  private lateinit var ongoingLoginEntry: OngoingLoginEntry

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
        .flatMap { api.login(it) }
        .doOnSuccess { storeUser(it) }
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

  private fun storeUser(response: LoginResponse) {
    accessTokenPreference.set(Just(response.accessToken))
    loggedInUserPreference.set(Just(response.loggedInUser))
  }

  private fun <T : Any> readErrorResponseJson(error: HttpException, clazz: KClass<T>): T {
    val jsonAdapter = moshi.adapter(clazz.java)
    return jsonAdapter.fromJson(error.response().errorBody()!!.source())!!
  }

  fun logout() {
    loggedInUserPreference.set(None)
    accessTokenPreference.set(None)
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
