package org.simple.clinic.user

import com.google.common.truth.Truth
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.TestData
import org.simple.clinic.util.Optional
import org.simple.clinic.util.RxErrorsRule

@RunWith(JUnitParamsRunner::class)
class NewlyVerifiedUserTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private lateinit var newlyVerifiedUser: NewlyVerifiedUser
  private lateinit var receivedUsers: MutableList<User>

  private val userEmitter = PublishSubject.create<Optional<User>>()!!

  @Before
  fun setUp() {
    newlyVerifiedUser = NewlyVerifiedUser()

    receivedUsers = mutableListOf()

    userEmitter.compose(newlyVerifiedUser)
        .subscribe { receivedUsers.add(it) }
  }

  @Test
  @Parameters(value = [
    "OTP_REQUESTED|OTP_REQUESTED|LOGGED_IN|true",
    "LOGGED_IN|LOGGED_IN|LOGGED_IN|false"
  ])
  fun `when the user status changes to verified, it should emit the user`(
      previousLoggedInStatus2: User.LoggedInStatus,
      previousLoggedInStatus1: User.LoggedInStatus,
      currentLoggedInStatus: User.LoggedInStatus,
      shouldEmitUser: Boolean
  ) {
    val user = TestData.loggedInUser(loggedInStatus = previousLoggedInStatus2)

    userEmitter.onNext(Optional.of(user))
    userEmitter.onNext(Optional.of(user.copy(loggedInStatus = previousLoggedInStatus1)))

    val expectedUser = user.copy(loggedInStatus = currentLoggedInStatus)
    userEmitter.onNext(Optional.of(expectedUser))

    if (shouldEmitUser) {
      Truth.assertThat(receivedUsers).isEqualTo(listOf(expectedUser))
    }
  }
}
