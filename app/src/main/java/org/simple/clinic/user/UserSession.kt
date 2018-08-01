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
import org.simple.clinic.login.applock.PasswordHasher
import org.simple.clinic.registration.RegistrationApiV1
import org.simple.clinic.registration.RegistrationRequest
import org.simple.clinic.registration.RegistrationResult
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named
import kotlin.reflect.KClass

@AppScope
class UserSession @Inject constructor(
    private val loginApi: LoginApiV1,
    private val registrationApi: RegistrationApiV1,
    private val moshi: Moshi,
    private val facilitySync: FacilitySync,
    private val sharedPreferences: SharedPreferences,
    private val appDatabase: AppDatabase,
    private val passwordHasher: PasswordHasher,
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

  // TODO: rename to loginFromOngoingLoginEntry()
  fun login(): Single<LoginResult> {
    return ongoingLoginEntry()
        .map { LoginRequest(UserPayload(it.phoneNumber!!, it.pin!!, it.otp)) }
        .flatMap { loginApi.login(it) }
        .flatMap {
          storeUserAndAccessToken(it)
              .toSingleDefault(it)
        }
        .flatMap {
          facilitySync.sync()
              .toSingleDefault(it)
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

  fun loginFromOngoingRegistrationEntry(): Completable {
    return ongoingRegistrationEntry()
        .flatMap { entry ->
          passwordHasher.hash(entry.pin!!)
              .map { passwordDigest ->
                LoggedInUser(
                    uuid = entry.uuid!!,
                    fullName = entry.fullName!!,
                    phoneNumber = entry.phoneNumber!!,
                    pinDigest = passwordDigest,
                    facilityUuid = UUID.fromString("38b16571-4105-41ae-b8dd-8e3312cdb96c"),
                    createdAt = entry.createdAt!!,
                    updatedAt = entry.createdAt,
                    status = LoggedInUser.Status.WAITING_FOR_APPROVAL
                )
              }
        }
        .flatMapCompletable { storeUser(it) }
  }

  fun register(): Single<RegistrationResult> {
    return loggedInUser()
        .map { (user) -> user }
        .firstOrError()
        .map(::RegistrationRequest)
        .flatMap { registrationApi.createUser(it) }
        .flatMap<RegistrationResult> {
          storeUser(it.loggedInUser)
              .andThen(Single.just(RegistrationResult.Success()))
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

  private fun storeUserAndAccessToken(response: LoginResponse): Completable {
    accessTokenPreference.set(Just(response.accessToken))
    return storeUser(response.loggedInUser)
  }

  private fun storeUser(loggedInUser: LoggedInUser): Completable {
    return Completable.fromAction {
      appDatabase.userDao().create(loggedInUser)
    }
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
    return appDatabase.userDao().user()
        .toObservable()
        .map { if (it.isEmpty()) None else Just(it.first()) }
  }

  fun isUserLoggedIn(): Boolean {
    // TODO: This is bad. Make this function return Single<Boolean> instead.
    val user = loggedInUser().blockingFirst()
    return user is Just
  }

  fun accessToken(): Optional<String> {
    return accessTokenPreference.get()
  }
}
