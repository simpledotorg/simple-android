package org.simple.clinic.allpatientsinfacility

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil
import kotlinx.android.synthetic.main.list_allpatientsinfacility_facility_header.*
import kotlinx.android.synthetic.main.list_patient_search.*
import org.simple.clinic.R
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.widgets.recyclerview.ViewHolderX

sealed class AllPatientsInFacilityListItem {

  companion object {
    fun mapSearchResultsToListItems(
        facility: Facility,
        patientSearchResults: List<PatientSearchResult>
    ): List<AllPatientsInFacilityListItem> {
      return patientSearchResults
          .map { patientSearchResult -> SearchResult(facility, patientSearchResult) }
          .let { searchResultItems -> listOf(FacilityHeader(facility.name)) + searchResultItems }
    }
  }

  abstract val layoutResId: Int

  abstract fun render(holder: ViewHolderX)

  data class FacilityHeader(val facilityName: String) : AllPatientsInFacilityListItem() {

    override val layoutResId = R.layout.list_allpatientsinfacility_facility_header

    override fun render(holder: ViewHolderX) {
      val resources = holder.itemView.resources
      holder.facilityLabel.text = resources.getString(R.string.allpatientsinfacility_nopatients_title, facilityName)
    }
  }

  data class SearchResult(
      val facility: Facility,
      val patientSearchResult: PatientSearchResult
  ) : AllPatientsInFacilityListItem() {

    override val layoutResId = R.layout.list_patient_search

    override fun render(holder: ViewHolderX) {
      holder.patientSearchResultView.render(patientSearchResult, facility)
    }
  }

  class AllPatientsInFacilityListItemCallback : DiffUtil.ItemCallback<AllPatientsInFacilityListItem>() {

    override fun areItemsTheSame(oldItem: AllPatientsInFacilityListItem, newItem: AllPatientsInFacilityListItem): Boolean {
      return when {
        oldItem is FacilityHeader && newItem is FacilityHeader -> true
        oldItem is SearchResult && newItem is SearchResult -> oldItem.patientSearchResult.uuid == newItem.patientSearchResult.uuid
        else -> false
      }
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: AllPatientsInFacilityListItem, newItem: AllPatientsInFacilityListItem): Boolean {
      return oldItem == newItem
    }
  }
}
