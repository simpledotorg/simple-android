package org.simple.clinic.registration

import org.simple.clinic.user.LoggedInUserPayload

sealed class FindUserResult {

  data class Found(val user: LoggedInUserPayload) : FindUserResult()

  class NotFound : FindUserResult()

  class NetworkError : FindUserResult()

  class UnexpectedError : FindUserResult()
}
