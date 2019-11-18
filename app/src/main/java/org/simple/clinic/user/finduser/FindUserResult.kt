package org.simple.clinic.user.finduser

import org.simple.clinic.user.LoggedInUserPayload

sealed class FindUserResult {

  data class Found(val user: LoggedInUserPayload) : FindUserResult()

  object NotFound : FindUserResult()

  object NetworkError : FindUserResult()

  object UnexpectedError : FindUserResult()
}
