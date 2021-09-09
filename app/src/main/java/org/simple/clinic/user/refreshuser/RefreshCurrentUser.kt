package org.simple.clinic.user.refreshuser

import io.reactivex.Completable
import org.simple.clinic.login.UsersApi
import org.simple.clinic.user.LoggedInUserPayload
import org.simple.clinic.user.User
import org.simple.clinic.user.User.LoggedInStatus
import org.simple.clinic.user.User.LoggedInStatus.LOGGED_IN
import org.simple.clinic.user.User.LoggedInStatus.RESET_PIN_REQUESTED
import org.simple.clinic.user.User.RoomDao
import org.simple.clinic.user.UserStatus
import timber.log.Timber
import java.util.Optional
import javax.inject.Inject

class RefreshCurrentUser @Inject constructor(
    private val userDao: RoomDao,
    private val usersApi: UsersApi
) {

  fun refresh(): Completable {
    return Completable
        .fromAction {
          val fetchedUserInfo = fetchUserDetails()

          fetchedUserInfo.ifPresent { userPayload ->
            val storedUser = userDao.userImmediate()!!
            val updatedUserDetails = mapPayloadToUser(storedUser, userPayload, newLoggedInStatus(storedUser, userPayload))

            userDao.createOrUpdate(updatedUserDetails)
          }
        }
        .doOnSubscribe { Timber.i("Refreshing logged-in user") }
        .doOnError {
          // We don't care about handling errors here and we'll just
          // report it.
          Timber.e(it)
        }
        .onErrorComplete()
  }

  private fun fetchUserDetails(): Optional<LoggedInUserPayload> {
    val response = usersApi.self().execute()

    return if (response.code() == 200) Optional.of(response.body()!!.user) else Optional.empty()
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
          currentFacilityUuid = storedUser.currentFacilityUuid,
          teleconsultPhoneNumber = payload.teleconsultPhoneNumber,
          capabilities = payload.capabilities
      )
    }
  }
}
