package org.simple.clinic.user.finduser

import org.simple.clinic.user.UserStatus
import java.util.UUID

sealed class FindUserResult {

  data class Found(val uuid: UUID, val status: UserStatus) : FindUserResult()

  data object NotFound : FindUserResult()

  data object NetworkError : FindUserResult()

  data object UnexpectedError : FindUserResult()
}
