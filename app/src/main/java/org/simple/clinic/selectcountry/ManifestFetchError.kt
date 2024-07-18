package org.simple.clinic.selectcountry

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.util.ResolvedError
import org.simple.clinic.util.ResolvedError.NetworkRelated
import org.simple.clinic.util.ResolvedError.Unauthenticated
import org.simple.clinic.util.ResolvedError.Unexpected

sealed class ManifestFetchError : Parcelable {

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

@Parcelize
data object NetworkError : ManifestFetchError()

@Parcelize
data object ServerError : ManifestFetchError()

@Parcelize
data object UnexpectedError : ManifestFetchError()
