package org.simple.clinic.instantsearch

import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat
import androidx.paging.PagingData
import androidx.paging.TerminalSeparatorType.SOURCE_COMPLETE
import androidx.paging.insertSeparators
import androidx.paging.map
import androidx.recyclerview.widget.DiffUtil
import io.reactivex.subjects.Subject
import org.simple.clinic.R
import org.simple.clinic.databinding.ListPatientSearchBinding
import org.simple.clinic.databinding.ListPatientSearchHeaderBinding
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.widgets.PagingItemAdapter
import org.simple.clinic.widgets.PatientSearchResultItemView.PatientSearchResultViewModel
import org.simple.clinic.widgets.recyclerview.BindingViewHolder
import java.util.UUID

sealed class InstantSearchResultsItemType : PagingItemAdapter.Item<InstantSearchResultsItemType.Event> {

  companion object {

    fun from(
        patientSearchResults: PagingData<PatientSearchResult>,
        currentFacility: Facility,
        searchQuery: String?
    ): PagingData<InstantSearchResultsItemType> {
      return patientSearchResults
          .map { SearchResult.forSearchResult(it, currentFacility.uuid, searchQuery) }
          .insertSeparators(SOURCE_COMPLETE) { before, after -> insertHeaders(before, after, currentFacility) }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun insertHeaders(
        beforeSearchResult: SearchResult?,
        afterSearchResult: SearchResult?,
        currentFacility: Facility
    ): InstantSearchResultsItemType? {
      return when {
        shouldAddAssignedFacility(beforeSearchResult, afterSearchResult) -> AssignedFacilityHeader(facilityName = currentFacility.name)
        shouldAddNearbyFacilityHeader(beforeSearchResult, afterSearchResult) -> NearbyFacilitiesHeader
        else -> null
      }
    }

    private fun shouldAddAssignedFacility(beforeSearchResult: SearchResult?, afterSearchResult: SearchResult?) =
        beforeSearchResult == null && afterSearchResult != null && afterSearchResult.isAtCurrentFacility

    private fun shouldAddNearbyFacilityHeader(beforeSearchResult: SearchResult?, afterSearchResult: SearchResult?) =
        isFirstSearchResultNotAtCurrentFacility(beforeSearchResult, afterSearchResult) || shouldAddNearbyFacilityHeaderInBetweenSearchResults(beforeSearchResult, afterSearchResult)

    private fun isFirstSearchResultNotAtCurrentFacility(beforeSearchResult: SearchResult?, afterSearchResult: SearchResult?) =
        beforeSearchResult == null && afterSearchResult != null && !afterSearchResult.isAtCurrentFacility

    private fun shouldAddNearbyFacilityHeaderInBetweenSearchResults(beforeSearchResult: SearchResult?, afterSearchResult: SearchResult?) =
        beforeSearchResult != null && afterSearchResult != null && beforeSearchResult.isAtCurrentFacility && !afterSearchResult.isAtCurrentFacility
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
      val currentFacilityId: UUID,
      val searchQuery: String?
  ) : InstantSearchResultsItemType() {

    companion object {
      fun forSearchResult(
          searchResult: PatientSearchResult,
          currentFacilityId: UUID,
          searchQuery: String?
      ): SearchResult {
        return SearchResult(
            searchResultViewModel = mapPatientSearchResultToViewModel(searchResult),
            currentFacilityId = currentFacilityId,
            searchQuery = searchQuery
        )
      }

      private fun mapPatientSearchResultToViewModel(searchResult: PatientSearchResult): PatientSearchResultViewModel {
        return PatientSearchResultViewModel(
            uuid = searchResult.uuid,
            fullName = searchResult.fullName,
            gender = searchResult.gender,
            ageDetails = searchResult.ageDetails,
            address = searchResult.address,
            phoneNumber = searchResult.phoneNumber,
            lastSeen = searchResult.lastSeen,
            identifier = searchResult.identifier,
            assignedFacilityId = searchResult.assignedFacilityId,
            assignedFacilityName = searchResult.assignedFacilityName
        )
      }
    }

    val isAtCurrentFacility
      get() = searchResultViewModel.assignedFacilityId == currentFacilityId

    override fun layoutResId(): Int = R.layout.list_patient_search

    override fun render(holder: BindingViewHolder, subject: Subject<Event>) {
      val binding = holder.binding as ListPatientSearchBinding

      binding.patientSearchResultView.setOnClickListener {
        subject.onNext(Event.ResultClicked(searchResultViewModel.uuid))
      }
      binding.patientSearchResultView.render(searchResultViewModel, currentFacilityId, searchQuery)
    }
  }

  class DiffCallback : DiffUtil.ItemCallback<InstantSearchResultsItemType>() {

    override fun areItemsTheSame(
        oldItem: InstantSearchResultsItemType,
        newItem: InstantSearchResultsItemType
    ): Boolean {
      return when {
        oldItem is AssignedFacilityHeader && newItem is AssignedFacilityHeader -> oldItem.facilityName == newItem.facilityName
        oldItem is NearbyFacilitiesHeader && newItem is NearbyFacilitiesHeader -> true
        oldItem is SearchResult && newItem is SearchResult -> oldItem.searchResultViewModel.uuid == newItem.searchResultViewModel.uuid
        else -> false
      }
    }

    override fun areContentsTheSame(
        oldItem: InstantSearchResultsItemType,
        newItem: InstantSearchResultsItemType
    ): Boolean {
      return oldItem == newItem
    }
  }
}
