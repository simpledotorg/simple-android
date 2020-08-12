package org.simple.clinic.summary.linkId

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class LinkIdWithPatientModel : Parcelable {

  companion object {

    fun create() = LinkIdWithPatientModel()
  }
}
