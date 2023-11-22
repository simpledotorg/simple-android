package org.simple.clinic.facility.alertchange

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AlertFacilityChangeModel(
    val isFacilityChanged: Boolean
) : Parcelable {

  companion object {

    fun default() = AlertFacilityChangeModel(
        isFacilityChanged = false
    )
  }

  fun updateIsFacilityChanged(isFacilityChanged: Boolean) = copy(
      isFacilityChanged = isFacilityChanged
  )
}
