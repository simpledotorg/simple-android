package org.simple.clinic.user

import android.content.SharedPreferences
import android.support.annotation.WorkerThread
import com.f2prateek.rx.preferences2.Preference
import com.squareup.moshi.Moshi
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
import io.reactivex.rxkotlin.zipWith
import org.simple.clinic.AppDatabase
import org.simple.clinic.di.AppScope
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.facility.FacilitySync
import org.simple.clinic.login.LoginApiV1
import org.simple.clinic.login.LoginErrorResponse
import org.simple.clinic.login.LoginRequest
import org.simple.clinic.login.LoginResponse
import org.simple.clinic.login.LoginResult
import org.simple.clinic.login.UserPayload
import org.simple.clinic.login.applock.PasswordHasher
import org.simple.clinic.registration.FindUserResult
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
    private val facilityRepository: FacilityRepository,
    private val sharedPreferences: SharedPreferences,
    private val appDatabase: AppDatabase,
    private val passwordHasher: PasswordHasher,
    @Named("preference_access_token") private val accessTokenPreference: Preference<Optional<String>>
) {

  private lateinit var ongoingLoginEntry: OngoingLoginEntry
  private var ongoingRegistrationEntry: OngoingRegistrationEntry? = null

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
          facilitySync.sync()
              .toSingleDefault(it)
        }
        .flatMap {
          storeUserAndAccessToken(it)
              .toSingleDefault(it)
        }
        .map { LoginResult.Success() as LoginResult }
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
    val ongoingEntry = ongoingRegistrationEntry().cache()

    return ongoingEntry
        .zipWith(ongoingEntry.flatMap { entry -> passwordHasher.hash(entry.pin!!) })
        .flatMapCompletable { (entry, passwordDigest) ->
          val user = LoggedInUser(
              uuid = entry.uuid!!,
              fullName = entry.fullName!!,
              phoneNumber = entry.phoneNumber!!,
              pinDigest = passwordDigest,
              createdAt = entry.createdAt!!,
              updatedAt = entry.createdAt,
              status = UserStatus.WAITING_FOR_APPROVAL)
          storeUser(user, entry.facilityIds!!)
        }
        .andThen(clearOngoingRegistrationEntry())
  }

  fun findExistingUser(phoneNumber: String): Single<FindUserResult> {
    return registrationApi.findUser(phoneNumber)
        .map { user -> FindUserResult.Found(user) as FindUserResult }
        .onErrorReturn { e ->
          when {
            e is IOException -> FindUserResult.NetworkError()
            e is HttpException && e.code() == 404 -> FindUserResult.NotFound()
            else -> {
              Timber.e(e)
              FindUserResult.UnexpectedError()
            }
          }
        }
  }

  fun register(): Single<RegistrationResult> {
    val loggedInUser: Single<LoggedInUser> = loggedInUser()
        .map { (user) -> user!! }
        .firstOrError()
        .cache()

    val currentFacility = loggedInUser
        .flatMap { facilityRepository.facilityUuidsForUser(it).firstOrError() }

    return Singles.zip(loggedInUser, currentFacility)
        .map { (user, facilityUuids) -> userToPayload(user, facilityUuids) }
        .map(::RegistrationRequest)
        .flatMap { registrationApi.createUser(it) }
        .flatMap {
          val user = userFromPayload(it.userPayload)
          val userFacilities = it.userPayload.facilityUuids

          if (userFacilities.isEmpty()) {
            throw AssertionError("Server did not send back any facilities")
          }

          storeUser(user, userFacilities)
              .toSingleDefault(RegistrationResult.Success() as RegistrationResult)
        }
        .onErrorReturn { e ->
          Timber.e(e)
          RegistrationResult.Error()
        }
  }

  private fun userToPayload(user: LoggedInUser, facilityUuids: List<UUID>): LoggedInUserPayload {
    return user.run {
      LoggedInUserPayload(
          uuid = uuid,
          fullName = fullName,
          phoneNumber = phoneNumber,
          pinDigest = pinDigest,
          facilityUuids = facilityUuids,
          status = status,
          createdAt = createdAt,
          updatedAt = updatedAt)
    }
  }

  private fun userFromPayload(payload: LoggedInUserPayload): LoggedInUser {
    return payload.run {
      LoggedInUser(
          uuid = uuid,
          fullName = fullName,
          phoneNumber = phoneNumber,
          pinDigest = pinDigest,
          status = status,
          createdAt = createdAt,
          updatedAt = updatedAt)
    }
  }

  fun saveOngoingRegistrationEntry(entry: OngoingRegistrationEntry): Completable {
    return Completable.fromAction {
      this.ongoingRegistrationEntry = entry
    }
  }

  fun clearOngoingRegistrationEntry(): Completable {
    return saveOngoingRegistrationEntry(OngoingRegistrationEntry())
  }

  fun ongoingRegistrationEntry(): Single<OngoingRegistrationEntry> {
    return Single.fromCallable { ongoingRegistrationEntry }
  }

  fun isOngoingRegistrationEntryPresent(): Single<Boolean> =
      Single.fromCallable { ongoingRegistrationEntry != null }

  private fun storeUserAndAccessToken(response: LoginResponse): Completable {
    accessTokenPreference.set(Just(response.accessToken))
    return storeUser(
        userFromPayload(response.loggedInUser),
        response.loggedInUser.facilityUuids)
  }

  private fun storeUser(loggedInUser: LoggedInUser, facilityUuids: List<UUID>): Completable {
    return Completable
        .fromAction { appDatabase.userDao().createOrUpdate(loggedInUser) }
        .andThen(facilityRepository.associateUserWithFacilities(loggedInUser, facilityUuids, currentFacility = facilityUuids.first()))
  }

  private fun <T : Any> readErrorResponseJson(error: HttpException, clazz: KClass<T>): T {
    val jsonAdapter = moshi.adapter(clazz.java)
    return jsonAdapter.fromJson(error.response().errorBody()!!.source())!!
  }

  fun logout(): Completable {
    // FYI: RegistrationWorker doesn't get canceled when a user logs out.
    // It's possible that the wrong user will get sent to the server for
    // registration if another user logs in. This works for now because
    // there is no way to log out, but this something to keep in mind.
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

  // FIXME: Figure out a better way to add access tokens to network calls in a reactive fashion
  // FIXME: Maybe add a separate RxCallAdapterFactory that lets us transform requests without interceptors?
  // This was added because we needed to call it from the OkHttp Interceptor
  // in a synchronous fashion because the Rx - blocking() call used earlier
  // was causing a deadlock in the Room threads when data sync happened
  @WorkerThread
  fun loggedInUserImmediate() = appDatabase.userDao().userImmediate()

  fun isUserLoggedIn(): Boolean {
    // TODO: This is bad. Make this function return Single<Boolean> instead.
    val user = loggedInUser().blockingFirst()
    return user is Just
  }

  fun accessToken(): Optional<String> {
    return accessTokenPreference.get()
  }
}
