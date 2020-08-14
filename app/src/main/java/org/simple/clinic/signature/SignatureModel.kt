package org.simple.clinic.signature

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.File

@Parcelize
data class SignatureModel(
    val internalStoragePath: File
) : Parcelable {

  companion object {
    fun create(path: File) = SignatureModel(
        internalStoragePath = path
    )
  }
}
