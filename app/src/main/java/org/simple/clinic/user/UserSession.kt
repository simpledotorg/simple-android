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
import io.reactivex.schedulers.Schedulers
import org.simple.clinic.AppDatabase
import org.simple.clinic.di.AppScope
import org.simple.clinic.facility.FacilityPullResult.NetworkError
import org.simple.clinic.facility.FacilityPullResult.Success
import org.simple.clinic.facility.FacilityPullResult.UnexpectedError
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.facility.FacilitySync
import org.simple.clinic.forgotpin.ForgotPinApiV1
import org.simple.clinic.forgotpin.ForgotPinResponse
import org.simple.clinic.forgotpin.ResetPinRequest
import org.simple.clinic.login.LoginApiV1
import org.simple.clinic.login.LoginErrorResponse
import org.simple.clinic.login.LoginRequest
import org.simple.clinic.login.LoginResponse
import org.simple.clinic.login.LoginResult
import org.simple.clinic.login.UserPayload
import org.simple.clinic.login.applock.PasswordHasher
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.registration.FindUserResult
import org.simple.clinic.registration.RegistrationApiV1
import org.simple.clinic.registration.RegistrationRequest
import org.simple.clinic.registration.RegistrationResponse
import org.simple.clinic.registration.RegistrationResult
import org.simple.clinic.registration.SaveUserLocallyResult
import org.simple.clinic.sync.SyncScheduler
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.threeten.bp.Instant
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named
import kotlin.reflect.KClass

@AppScope
class UserSession @Inject constructor(
    private val loginApi: LoginApiV1,
    private val registrationApi: RegistrationApiV1,
    private val forgotPinApiV1: ForgotPinApiV1,
    private val moshi: Moshi,
    private val facilitySync: FacilitySync,
    private val facilityRepository: FacilityRepository,
    private val sharedPreferences: SharedPreferences,
    private val appDatabase: AppDatabase,
    private val passwordHasher: PasswordHasher,
    @Named("preference_access_token") private val accessTokenPreference: Preference<Optional<String>>,
    private val syncScheduler: SyncScheduler,
    @Named("last_patient_pull_timestamp") private val patientSyncPullTimestamp: Preference<Optional<Instant>>,
    @Named("last_bp_pull_timestamp") private val bpSyncPullTimestamp: Preference<Optional<Instant>>,
    @Named("last_prescription_pull_timestamp") private val prescriptionSyncPullTimestamp: Preference<Optional<Instant>>,
    @Named("last_appointment_pull_timestamp") private val appointmentSyncPullTimestamp: Preference<Optional<Instant>>,
    @Named("last_communication_pull_timestamp") private val communicationSyncPullTimestamp: Preference<Optional<Instant>>,
    @Named("last_medicalhistory_pull_timestamp") private val medicalHistorySyncPullTimestamp: Preference<Optional<Instant>>
) {

  private var ongoingLoginEntry: OngoingLoginEntry? = null
  private var ongoingRegistrationEntry: OngoingRegistrationEntry? = null

  fun saveOngoingLoginEntry(entry: OngoingLoginEntry): Completable {
    return Completable.fromAction {
      this.ongoingLoginEntry = entry
    }
  }

  fun ongoingLoginEntry(): Single<OngoingLoginEntry> {
    return Single.fromCallable { ongoingLoginEntry }
  }

  fun clearOngoingLoginEntry(): Completable {
    return Completable.fromAction { ongoingLoginEntry = null }
  }

  fun loginWithOtp(otp: String): Single<LoginResult> {
    return ongoingLoginEntry()
        .map { LoginRequest(UserPayload(it.phoneNumber, it.pin, otp)) }
        .flatMap { loginApi.login(it) }
        .flatMap {
          // TODO: Review if this is necessary since Facilities are synced before making the request OTP call
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

  fun requestLoginOtp(): Single<LoginResult> {
    val ongoingEntry = ongoingLoginEntry().cache()
    return ongoingEntry
        .flatMap {
          loginApi.requestLoginOtp(it.userId)
              .andThen(Completable.fromAction {
                appDatabase.userDao()
                    .updateLoggedInStatusForUser(it.userId, User.LoggedInStatus.OTP_REQUESTED)
              })
              .toSingleDefault(LoginResult.Success() as LoginResult)
        }
        .onErrorReturn { error ->
          when (error) {
            is IOException -> LoginResult.NetworkError()
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
          val user = User(
              uuid = entry.uuid!!,
              fullName = entry.fullName!!,
              phoneNumber = entry.phoneNumber!!,
              pinDigest = passwordDigest,
              createdAt = entry.createdAt!!,
              updatedAt = entry.createdAt,
              status = UserStatus.WAITING_FOR_APPROVAL,
              loggedInStatus = User.LoggedInStatus.NOT_LOGGED_IN)
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

  fun syncFacilityAndSaveUser(loggedInUserPayload: LoggedInUserPayload): Single<SaveUserLocallyResult> {
    return facilitySync.pullWithResult()
        .flatMap { result ->
          when (result) {
            is Success -> {
              Single.just(userFromPayload(loggedInUserPayload, User.LoggedInStatus.NOT_LOGGED_IN))
                  .flatMap {
                    storeUser(it, loggedInUserPayload.facilityUuids)
                        .toSingleDefault(SaveUserLocallyResult.Success() as SaveUserLocallyResult)
                  }
                  .onErrorResumeNext(Single.just(SaveUserLocallyResult.UnexpectedError()))
            }
            is NetworkError -> Single.just(SaveUserLocallyResult.NetworkError())
            is UnexpectedError -> Single.just(SaveUserLocallyResult.UnexpectedError())
          }
        }
  }

  fun refreshLoggedInUser(): Completable {
    return requireLoggedInUser()
        .firstOrError()
        .flatMapCompletable { loggedInUser ->
          registrationApi.findUser(loggedInUser.phoneNumber)
              .flatMapCompletable { userPayload ->
                // FIXME: This is a hack to handle the case where the user logged in status will
                // not get set to RESET_PIN_REQUESTED when the PIN reset status is approved.
                val finalLoggedInStatus = if (userPayload.status == UserStatus.APPROVED_FOR_SYNCING) {
                  User.LoggedInStatus.LOGGED_IN

                } else {
                  loggedInUser.loggedInStatus
                }

                val user = userFromPayload(userPayload, finalLoggedInStatus)
                val userFacilities = userPayload.facilityUuids
                storeUser(user, userFacilities)
              }
        }
  }

  fun register(): Single<RegistrationResult> {
    val user: Single<User> = loggedInUser()
        .map { (user) -> user!! }
        .firstOrError()
        .cache()

    val currentFacility = user
        .flatMap { facilityRepository.facilityUuidsForUser(it).firstOrError() }

    return Singles.zip(user, currentFacility)
        .map { (user, facilityUuids) -> userToPayload(user, facilityUuids) }
        .map(::RegistrationRequest)
        .flatMap { registrationApi.createUser(it) }
        .flatMap {
          storeUserAndAccessToken(it)
              .toSingleDefault(RegistrationResult.Success() as RegistrationResult)
        }
        .onErrorReturn { e ->
          Timber.e(e)
          RegistrationResult.Error()
        }
  }

  private fun userToPayload(user: User, facilityUuids: List<UUID>): LoggedInUserPayload {
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

  private fun userFromPayload(payload: LoggedInUserPayload, status: User.LoggedInStatus): User {
    return payload.run {
      User(
          uuid = uuid,
          fullName = fullName,
          phoneNumber = phoneNumber,
          pinDigest = pinDigest,
          status = this.status,
          createdAt = createdAt,
          updatedAt = updatedAt,
          loggedInStatus = status)
    }
  }

  fun saveOngoingRegistrationEntry(entry: OngoingRegistrationEntry): Completable {
    return Completable.fromAction {
      this.ongoingRegistrationEntry = entry
    }
  }

  fun ongoingRegistrationEntry(): Single<OngoingRegistrationEntry> {
    return Single.fromCallable { ongoingRegistrationEntry }
  }

  fun clearOngoingRegistrationEntry(): Completable {
    return Completable.fromAction {
      ongoingRegistrationEntry = null
    }
  }

  fun isOngoingRegistrationEntryPresent(): Single<Boolean> =
      Single.fromCallable { ongoingRegistrationEntry != null }

  private fun storeUserAndAccessToken(response: LoginResponse): Completable {
    accessTokenPreference.set(Just(response.accessToken))
    return storeUser(
        userFromPayload(response.loggedInUser, User.LoggedInStatus.LOGGED_IN),
        response.loggedInUser.facilityUuids)
  }

  private fun storeUserAndAccessToken(response: ForgotPinResponse): Completable {
    accessTokenPreference.set(Just(response.accessToken))

    val user = userFromPayload(response.loggedInUser, User.LoggedInStatus.RESET_PIN_REQUESTED)
    return storeUser(user, response.loggedInUser.facilityUuids)
  }

  private fun storeUserAndAccessToken(response: RegistrationResponse): Completable {
    accessTokenPreference.set(Just(response.accessToken))

    val user = userFromPayload(response.userPayload, User.LoggedInStatus.LOGGED_IN)
    val userFacilityIds = response.userPayload.facilityUuids

    if (userFacilityIds.isEmpty()) {
      throw AssertionError("Server did not send back any facilities")
    }

    return storeUser(user, userFacilityIds)
  }

  private fun storeUser(user: User, facilityUuids: List<UUID>): Completable {
    return Completable
        .fromAction { appDatabase.userDao().createOrUpdate(user) }
        .andThen(facilityRepository.associateUserWithFacilities(user, facilityUuids, currentFacility = facilityUuids.first()))
  }

  private fun <T : Any> readErrorResponseJson(error: HttpException, clazz: KClass<T>): T {
    val jsonAdapter = moshi.adapter(clazz.java)
    return jsonAdapter.fromJson(error.response().errorBody()!!.source())!!
  }

  fun clearLoggedInUser(): Completable {
    return loggedInUser()
        .firstOrError()
        .filter { it is Just<User> }
        .map { (user) -> user!! }
        .flatMapCompletable {
          Completable.fromAction {
            appDatabase.userDao().deleteUserAndFacilityMappings(it, appDatabase.userFacilityMappingDao())
          }
        }
  }

  fun logout(): Completable {
    // FYI: RegistrationWorker doesn't get canceled when a user logs out.
    // It's possible that the wrong user will get sent to the server for
    // registration if another user logs in. This works for now because
    // there is no way to log out, but this is something to keep in mind.
    return Completable.fromAction {
      sharedPreferences.edit().clear().apply()
      appDatabase.clearAllTables()
    }
  }

  fun loggedInUser(): Observable<Optional<User>> {
    return appDatabase.userDao().user()
        .toObservable()
        .map { if (it.isEmpty()) None else Just(it.first()) }
  }

  fun requireLoggedInUser(): Observable<User> {
    return loggedInUser()
        .map { (user) ->
          if (user == null) {
            throw AssertionError("User isn't logged in yet")
          }
          user
        }
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

  fun syncAndClearData(patientRepository: PatientRepository, syncRetryCount: Int = 0, timeoutSeconds: Long = 15L): Completable {
    return syncScheduler.syncImmediately()
        .subscribeOn(Schedulers.io())
        .retry(syncRetryCount.toLong())
        .timeout(timeoutSeconds, TimeUnit.SECONDS)
        .onErrorComplete()
        .andThen(patientRepository.clearPatientData())
        .andThen(Completable.fromAction {
          patientSyncPullTimestamp.delete()
          bpSyncPullTimestamp.delete()
          prescriptionSyncPullTimestamp.delete()
          appointmentSyncPullTimestamp.delete()
          communicationSyncPullTimestamp.delete()
          medicalHistorySyncPullTimestamp.delete()
        })
        .andThen(requireLoggedInUser().firstOrError().flatMapCompletable { user ->
          // TODO: Move this to a separate method which can be called from wherever
          Completable.fromAction {
            appDatabase.userDao().updateLoggedInStatusForUser(user.uuid, User.LoggedInStatus.RESETTING_PIN)
          }
        })
  }

  fun resetPin(pin: String): Single<ForgotPinResult> {
    val resetPasswordCall = passwordHasher.hash(pin)
        .map { ResetPinRequest(it) }
        .flatMap { forgotPinApiV1.resetPin(it) }

    val updateUserOnSuccess = resetPasswordCall
        .flatMapCompletable { storeUserAndAccessToken(it) }
        .toSingleDefault(ForgotPinResult.Success as ForgotPinResult)

    return updateUserOnSuccess
        .onErrorReturn {
          when (it) {
            is IOException -> ForgotPinResult.NetworkError
            is HttpException -> if (it.code() == 401) {
              ForgotPinResult.UserNotFound
            } else {
              ForgotPinResult.UnexpectedError(it)
            }
            else -> ForgotPinResult.UnexpectedError(it)
          }
        }
  }
}
