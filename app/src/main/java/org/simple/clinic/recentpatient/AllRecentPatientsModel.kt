package org.simple.clinic.recentpatient

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.patient.RecentPatient

@Parcelize
data class AllRecentPatientsModel(
    private val recentPatients: List<RecentPatient>?
): Parcelable {

  companion object {
    fun create(): AllRecentPatientsModel {
      return AllRecentPatientsModel(
          recentPatients = null
      )
    }
  }

  fun recentPatientsLoaded(recentPatients: List<RecentPatient>): AllRecentPatientsModel {
    return copy(recentPatients = recentPatients)
  }
}
