package org.simple.clinic.user.finduser

import org.simple.clinic.user.LoggedInUserPayload

sealed class FindUserResult {

  // TODO: Remove this once we move to the new user refresh call
  data class Found_Old(val user: LoggedInUserPayload) : FindUserResult()

  object NotFound : FindUserResult()

  object NetworkError : FindUserResult()

  object UnexpectedError : FindUserResult()
}
