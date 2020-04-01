package org.simple.clinic.user.refreshuser

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Single
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.login.LoginApi
import org.simple.clinic.user.User
import org.simple.clinic.user.User.LoggedInStatus.LOGGED_IN
import org.simple.clinic.user.User.LoggedInStatus.RESET_PIN_REQUESTED
import org.simple.clinic.user.UserStatus.ApprovedForSyncing
import org.simple.clinic.user.finduser.FindUserResult
import org.simple.clinic.user.finduser.FindUserResult.Found_Old
import org.simple.clinic.user.finduser.UserLookup
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.toUser
import java.util.UUID

class RefreshCurrentUserTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val userUuid = UUID.fromString("2a90955a-d21b-4fa9-918f-d1c4c44b7aae")
  private val phone = "1234567890"

  private val userDao = mock<User.RoomDao>()
  private val findUserWithPhoneNumber = mock<UserLookup>()
  private val loginApi = mock<LoginApi>()

  private val refreshCurrentUser = RefreshCurrentUser(userDao, findUserWithPhoneNumber, loginApi)

  @Test
  fun `when refreshing the user succeeds, the updated user details must be saved in the database`() {
    // given
    val userPayload = TestData.loggedInUserPayload(uuid = userUuid, phone = phone, status = ApprovedForSyncing)
    whenever(findUserWithPhoneNumber.find_old(phone)) doReturn Single.just<FindUserResult>(Found_Old(userPayload))

    val savedUser = userPayload.toUser(LOGGED_IN)
    whenever(userDao.userImmediate()) doReturn savedUser

    // when
    refreshCurrentUser.refresh().blockingAwait()

    // then
    verify(userDao).createOrUpdate(userPayload.toUser(LOGGED_IN))
  }

  @Test
  fun `when the locally saved user has requested a PIN reset and the refreshed user has been approved for syncing, the user must be updated as logged in`() {
    val userPayload = TestData.loggedInUserPayload(uuid = userUuid, phone = phone, status = ApprovedForSyncing)
    whenever(findUserWithPhoneNumber.find_old(phone)) doReturn Single.just<FindUserResult>(Found_Old(userPayload))

    val savedUser = userPayload.toUser(RESET_PIN_REQUESTED)
    whenever(userDao.userImmediate()) doReturn savedUser

    // when
    refreshCurrentUser.refresh().blockingAwait()

    // then
    verify(userDao).createOrUpdate(userPayload.toUser(LOGGED_IN))
  }
}
