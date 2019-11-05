package org.simple.clinic.selectcountry

import org.simple.clinic.util.ResolvedError
import org.simple.clinic.util.ResolvedError.*

sealed class ManifestFetchError {

  companion object {
    fun fromResolvedError(error: ResolvedError): ManifestFetchError {
      return when (error) {
        is Unexpected, is Unauthenticated -> UnexpectedError
        is NetworkRelated -> NetworkError
        is ResolvedError.ServerError -> ServerError
      }
    }
  }
}

object NetworkError : ManifestFetchError()

object ServerError : ManifestFetchError()

object UnexpectedError : ManifestFetchError()
