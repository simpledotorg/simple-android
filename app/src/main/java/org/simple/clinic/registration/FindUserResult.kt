package org.simple.clinic.registration

import org.simple.clinic.user.LoggedInUser

sealed class FindUserResult {

  data class Found(val user: LoggedInUser) : FindUserResult()

  class NotFound : FindUserResult()

  class NetworkError : FindUserResult()

  class UnexpectedError : FindUserResult()
}
