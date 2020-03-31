package org.simple.clinic.user.finduser

import org.simple.clinic.user.LoggedInUserPayload
import org.simple.clinic.user.UserStatus
import java.util.UUID

sealed class FindUserResult {

  // TODO: Remove this once we move to the new user refresh call
  data class Found_Old(val user: LoggedInUserPayload) : FindUserResult()

  data class Found(val uuid: UUID, val status: UserStatus): FindUserResult()

  object NotFound : FindUserResult()

  object NetworkError : FindUserResult()

  object UnexpectedError : FindUserResult()
}
