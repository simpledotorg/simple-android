package org.simple.clinic.instantsearch

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import io.reactivex.subjects.Subject
import org.simple.clinic.R
import org.simple.clinic.databinding.ListPatientSearchBinding
import org.simple.clinic.databinding.ListPatientSearchHeaderBinding
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.PatientSearchResultItemView.PatientSearchResultViewModel
import org.simple.clinic.widgets.recyclerview.BindingViewHolder
import java.util.UUID

sealed class InstantSearchResultsItemType : ItemAdapter.Item<InstantSearchResultsItemType.Event> {

  companion object {

    fun from(
        patientSearchResults: List<PatientSearchResult>,
        currentFacility: Facility
    ): List<InstantSearchResultsItemType> {
      val (assignedFacilityPatients, nearbyFacilitiesPatients) = patientSearchResults
          .partition { it.assignedFacilityId == currentFacility.uuid }

      val assignedFacilityPatientsGroup = generateAssignedFacilityPatientsGroup(assignedFacilityPatients, currentFacility)
      val nearbyFacilitiesPatientsGroup = generateNearbyFacilitiesPatientsGroup(nearbyFacilitiesPatients, currentFacility)

      return assignedFacilityPatientsGroup + nearbyFacilitiesPatientsGroup
    }

    private fun generateAssignedFacilityPatientsGroup(
        assignedFacilityPatients: List<PatientSearchResult>,
        currentFacility: Facility
    ) = if (assignedFacilityPatients.isNotEmpty()) {
      listOf(AssignedFacilityHeader(currentFacility.name)) + SearchResult.forSearchResults(assignedFacilityPatients, currentFacility.uuid)
    } else {
      emptyList()
    }

    private fun generateNearbyFacilitiesPatientsGroup(nearbyFacilitiesPatients: List<PatientSearchResult>, currentFacility: Facility) =
        if (nearbyFacilitiesPatients.isNotEmpty()) {
          listOf(NearbyFacilitiesHeader) + SearchResult.forSearchResults(nearbyFacilitiesPatients, currentFacility.uuid)
        } else {
          emptyList()
        }
  }

  sealed class Event {
    data class ResultClicked(val patientUuid: UUID) : Event()
  }

  data class AssignedFacilityHeader(val facilityName: String) : InstantSearchResultsItemType() {

    override fun layoutResId(): Int = R.layout.list_patient_search_header

    override fun render(holder: BindingViewHolder, subject: Subject<Event>) {
      val binding = holder.binding as ListPatientSearchHeaderBinding
      val context = holder.itemView.context
      val headerColor = ContextCompat.getColor(context, R.color.color_on_surface_67)

      binding.patientsearchHeader.text = context.getString(R.string.patientinstantsearch_assigned_facility_header, facilityName)
      binding.patientsearchHeader.setTextColor(headerColor)
    }
  }

  object NearbyFacilitiesHeader : InstantSearchResultsItemType() {

    override fun layoutResId(): Int = R.layout.list_patient_search_header

    override fun render(holder: BindingViewHolder, subject: Subject<Event>) {
      val binding = holder.binding as ListPatientSearchHeaderBinding
      val context = holder.itemView.context
      val headerColor = ContextCompat.getColor(context, R.color.simple_red_500)

      binding.patientsearchHeader.text = context.getString(R.string.patientinstantsearch_nearby_facilities_header)
      binding.patientsearchHeader.setTextColor(headerColor)
    }
  }

  data class SearchResult(
      val searchResultViewModel: PatientSearchResultViewModel,
      val currentFacilityId: UUID
  ) : InstantSearchResultsItemType() {

    companion object {
      fun forSearchResults(
          searchResults: List<PatientSearchResult>,
          currentFacilityId: UUID
      ): List<SearchResult> {
        return searchResults
            .map(::mapPatientSearchResultToViewModel)
            .map { searchResultViewModel -> SearchResult(searchResultViewModel, currentFacilityId) }
      }

      private fun mapPatientSearchResultToViewModel(searchResult: PatientSearchResult): PatientSearchResultViewModel {
        return PatientSearchResultViewModel(
            uuid = searchResult.uuid,
            fullName = searchResult.fullName,
            gender = searchResult.gender,
            age = searchResult.age,
            dateOfBirth = searchResult.dateOfBirth,
            address = searchResult.address,
            phoneNumber = searchResult.phoneNumber,
            lastSeen = searchResult.lastSeen
        )
      }
    }


    override fun layoutResId(): Int = R.layout.list_patient_search

    override fun render(holder: BindingViewHolder, subject: Subject<Event>) {
      val binding = holder.binding as ListPatientSearchBinding

      binding.patientSearchResultView.setOnClickListener {
        subject.onNext(Event.ResultClicked(searchResultViewModel.uuid))
      }
      binding.patientSearchResultView.render(searchResultViewModel, currentFacilityId)
    }
  }

  class DiffCallback : DiffUtil.ItemCallback<InstantSearchResultsItemType>() {

    override fun areItemsTheSame(oldItem: InstantSearchResultsItemType, newItem: InstantSearchResultsItemType): Boolean {
      return when {
        oldItem is AssignedFacilityHeader && newItem is AssignedFacilityHeader -> oldItem.facilityName == newItem.facilityName
        oldItem is NearbyFacilitiesHeader && newItem is NearbyFacilitiesHeader -> true
        oldItem is SearchResult && newItem is SearchResult -> oldItem.searchResultViewModel.uuid == newItem.searchResultViewModel.uuid
        else -> false
      }
    }

    override fun areContentsTheSame(oldItem: InstantSearchResultsItemType, newItem: InstantSearchResultsItemType): Boolean {
      return oldItem == newItem
    }
  }
}
