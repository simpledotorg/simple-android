package org.simple.clinic.home.overdue.search

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class OverdueSearchModel(
    val overdueSearchProgressState: OverdueSearchProgressState?,
    @IgnoredOnParcel
    val selectedOverdueAppointments: Set<UUID> = emptySet(),
    val villageAndPatientNames: List<String>?,
    val searchInputs: List<String>
) : Parcelable {

  val hasSearchInputs: Boolean
    get() = searchInputs.isNotEmpty()

  val hasVillageAndPatientNames: Boolean
    get() = !villageAndPatientNames.isNullOrEmpty()

  companion object {

    fun create(): OverdueSearchModel {
      return OverdueSearchModel(
          overdueSearchProgressState = null,
          selectedOverdueAppointments = emptySet(),
          villageAndPatientNames = null,
          searchInputs = emptyList()
      )
    }
  }

  fun loadStateChanged(overdueSearchProgressState: OverdueSearchProgressState): OverdueSearchModel {
    return copy(overdueSearchProgressState = overdueSearchProgressState)
  }

  fun selectedOverdueAppointmentsChanged(selectedOverdueAppointments: Set<UUID>): OverdueSearchModel {
    return copy(selectedOverdueAppointments = selectedOverdueAppointments)
  }

  fun villagesAndPatientNamesLoaded(villageAndPatientNames: List<String>): OverdueSearchModel {
    return copy(villageAndPatientNames = villageAndPatientNames)
  }

  fun overdueSearchInputsChanged(searchInputs: List<String>): OverdueSearchModel {
    return copy(searchInputs = searchInputs)
  }
}
