package org.simple.clinic.user.finduser

import org.simple.clinic.user.UserStatus
import java.util.UUID

sealed class FindUserResult {

  data class Found(val uuid: UUID, val status: UserStatus) : FindUserResult()

  object NotFound : FindUserResult()

  object NetworkError : FindUserResult()

  object UnexpectedError : FindUserResult()
}
