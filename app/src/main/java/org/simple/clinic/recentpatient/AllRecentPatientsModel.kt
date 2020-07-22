package org.simple.clinic.recentpatient

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class AllRecentPatientsModel: Parcelable {

  companion object {
    fun create(): AllRecentPatientsModel = AllRecentPatientsModel()
  }
}
