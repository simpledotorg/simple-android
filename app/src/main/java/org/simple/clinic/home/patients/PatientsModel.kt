package org.simple.clinic.home.patients

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class PatientsModel: Parcelable {

  companion object {
    fun create(): PatientsModel = PatientsModel()
  }
}
