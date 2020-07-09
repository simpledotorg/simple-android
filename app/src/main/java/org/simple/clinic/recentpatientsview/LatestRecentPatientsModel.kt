package org.simple.clinic.recentpatientsview

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class LatestRecentPatientsModel : Parcelable {

  companion object {
    fun create(): LatestRecentPatientsModel = LatestRecentPatientsModel()
  }
}
