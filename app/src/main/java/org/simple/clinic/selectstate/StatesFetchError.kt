package org.simple.clinic.selectstate

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.util.ResolvedError

sealed class StatesFetchError : Parcelable {

  companion object {
    fun fromResolvedError(error: ResolvedError): StatesFetchError {
      return when (error) {
        is ResolvedError.Unexpected, is ResolvedError.Unauthenticated -> UnexpectedError
        is ResolvedError.NetworkRelated -> NetworkError
        is ResolvedError.ServerError -> ServerError
      }
    }
  }

  @Parcelize
  data object NetworkError : StatesFetchError()

  @Parcelize
  data object ServerError : StatesFetchError()

  @Parcelize
  data object UnexpectedError : StatesFetchError()
}
