package org.simple.clinic.user.refreshuser

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.FakeCall
import org.simple.clinic.TestData
import org.simple.clinic.login.CurrentUserResponse
import org.simple.clinic.login.UsersApi
import org.simple.clinic.user.User
import org.simple.clinic.user.User.LoggedInStatus.LOGGED_IN
import org.simple.clinic.user.User.LoggedInStatus.RESET_PIN_REQUESTED
import org.simple.clinic.user.UserStatus.ApprovedForSyncing
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.toUser
import java.util.UUID

class RefreshCurrentUserTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val userUuid = UUID.fromString("2a90955a-d21b-4fa9-918f-d1c4c44b7aae")
  private val phone = "1234567890"

  private val userDao = mock<User.RoomDao>()
  private val loginApi = mock<UsersApi>()

  private val refreshCurrentUser = RefreshCurrentUser(userDao, loginApi)

  @Test
  fun `when refreshing the user succeeds, the updated user details must be saved in the database`() {
    // given
    val userPayload = TestData.loggedInUserPayload(uuid = userUuid, phone = phone, status = ApprovedForSyncing)
    whenever(loginApi.self()) doReturn FakeCall.success(CurrentUserResponse(userPayload))

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
    whenever(loginApi.self()) doReturn FakeCall.success(CurrentUserResponse(userPayload))

    val savedUser = userPayload.toUser(RESET_PIN_REQUESTED)
    whenever(userDao.userImmediate()) doReturn savedUser

    // when
    refreshCurrentUser.refresh().blockingAwait()

    // then
    verify(userDao).createOrUpdate(userPayload.toUser(LOGGED_IN))
  }
}
