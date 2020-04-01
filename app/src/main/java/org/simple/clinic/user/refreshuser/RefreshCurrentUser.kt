package org.simple.clinic.user.refreshuser

import io.reactivex.Completable
import io.reactivex.Single
import org.simple.clinic.login.LoginApi
import org.simple.clinic.user.LoggedInUserPayload
import org.simple.clinic.user.User
import org.simple.clinic.user.User.LoggedInStatus
import org.simple.clinic.user.User.LoggedInStatus.LOGGED_IN
import org.simple.clinic.user.User.LoggedInStatus.RESET_PIN_REQUESTED
import org.simple.clinic.user.User.RoomDao
import org.simple.clinic.user.UserStatus
import org.simple.clinic.user.finduser.FindUserResult.Found_Old
import org.simple.clinic.user.finduser.UserLookup
import org.simple.clinic.util.mapType
import timber.log.Timber
import javax.inject.Inject

class RefreshCurrentUser @Inject constructor(
    private val userDao: RoomDao,
    private val userLookup: UserLookup,
    private val loginApi: LoginApi
) {

  fun refresh(): Completable {
    return Single.fromCallable { userDao.userImmediate() }
        .doOnSuccess { Timber.i("Refreshing logged-in user") }
        .flatMapCompletable { user ->
          userLookup.find_old(user.phoneNumber)
              .mapType<Found_Old, LoggedInUserPayload> { it.user }
              .map { payload -> mapPayloadToUser(user, payload, newLoggedInStatus(user, payload)) }
              .flatMapCompletable { updatedUserDetails ->
                Completable.fromAction { userDao.createOrUpdate(updatedUserDetails) }
              }
        }
  }

  private fun newLoggedInStatus(
      user: User,
      userPayload: LoggedInUserPayload
  ): LoggedInStatus {
    // TODO: This was added to handle the case where the user logged in status will
    // not get set to LOGGED_IN when the PIN reset request is approved. See if it can
    // be done in a better way since there are many places where this sort of logic is
    // littered all over the app currently.
    return if (user.loggedInStatus == RESET_PIN_REQUESTED && userPayload.status == UserStatus.ApprovedForSyncing) {
      LOGGED_IN

    } else {
      user.loggedInStatus
    }
  }

  private fun mapPayloadToUser(
      storedUser: User,
      payload: LoggedInUserPayload,
      loggedInStatus: LoggedInStatus
  ): User {
    return with(payload) {
      User(
          uuid = uuid,
          fullName = fullName,
          phoneNumber = phoneNumber,
          pinDigest = pinDigest,
          status = status,
          createdAt = createdAt,
          updatedAt = updatedAt,
          loggedInStatus = loggedInStatus,
          registrationFacilityUuid = payload.registrationFacilityId,
          currentFacilityUuid = storedUser.currentFacilityUuid
      )
    }
  }
}
