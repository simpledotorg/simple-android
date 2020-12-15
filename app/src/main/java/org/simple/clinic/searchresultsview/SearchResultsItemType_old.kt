package org.simple.clinic.searchresultsview

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

sealed class SearchResultsItemType_old : ItemAdapter.Item<SearchResultsItemType_old.Event> {

  sealed class Event {
    data class ResultClicked(val patientUuid: UUID) : Event()
  }

  companion object {
    fun from(
        results: PatientSearchResults
    ): List<SearchResultsItemType_old> {
      return when {
        results.hasNoResults -> emptyList()
        else -> generateSearchResultListItems(results)
      }
    }

    private fun generateSearchResultListItems(
        results: PatientSearchResults
    ): List<SearchResultsItemType_old> {
      val currentFacility = results.currentFacility!!
      val itemsInCurrentFacility = SearchResultRow
          .forSearchResults(results.visitedCurrentFacility, currentFacility)
          .let { searchResultRowsInCurrentFacility ->
            listItemsForCurrentFacility(
                currentFacility = currentFacility,
                searchResultRows = searchResultRowsInCurrentFacility
            )
          }

      val itemsInOtherFacility = SearchResultRow
          .forSearchResults(results.notVisitedCurrentFacility, currentFacility)
          .let(::listItemsForOtherFacilities)

      return itemsInCurrentFacility + itemsInOtherFacility
    }

    private fun listItemsForCurrentFacility(
        currentFacility: Facility,
        searchResultRows: List<SearchResultRow>
    ): List<SearchResultsItemType_old> {

      val currentFacilityHeader = InCurrentFacilityHeader(currentFacility.name)

      return if (searchResultRows.isNotEmpty()) {
        listOf(
            currentFacilityHeader,
            *searchResultRows.toTypedArray()
        )
      } else {
        listOf(
            currentFacilityHeader,
            NoPatientsInCurrentFacility
        )
      }
    }

    private fun listItemsForOtherFacilities(
        searchResultRows: List<SearchResultRow>
    ): List<SearchResultsItemType_old> {

      return if (searchResultRows.isNotEmpty()) {
        listOf(
            NotInCurrentFacilityHeader,
            *searchResultRows.toTypedArray()
        )
      } else {
        emptyList()
      }
    }
  }

  data class InCurrentFacilityHeader(
      val facilityName: String
  ) : SearchResultsItemType_old() {

    override fun layoutResId(): Int = R.layout.list_patient_search_header

    override fun render(holder: BindingViewHolder, subject: Subject<Event>) {
      val binding = holder.binding as ListPatientSearchHeaderBinding

      binding.patientsearchHeader.text = holder.itemView.context.getString(R.string.patientsearchresults_current_facility_header, facilityName)
    }
  }

  object NotInCurrentFacilityHeader : SearchResultsItemType_old() {

    override fun layoutResId(): Int = R.layout.list_patient_search_header

    override fun render(holder: BindingViewHolder, subject: Subject<Event>) {
      val binding = holder.binding as ListPatientSearchHeaderBinding

      binding.patientsearchHeader.text = holder.itemView.context.getString(R.string.patientsearchresults_other_results_header)
    }
  }

  object NoPatientsInCurrentFacility : SearchResultsItemType_old() {

    override fun layoutResId(): Int = R.layout.list_patient_search_no_patients

    override fun render(holder: BindingViewHolder, subject: Subject<Event>) {
    }
  }

  data class SearchResultRow(
      val searchResultViewModel: PatientSearchResultViewModel,
      val currentFacilityUuid: UUID
  ) : SearchResultsItemType_old() {

    companion object {
      fun forSearchResults(
          searchResults: List<PatientSearchResult>,
          currentFacility: Facility
      ): List<SearchResultRow> {
        return searchResults
            .map(::mapPatientSearchResultToViewModel)
            .map { searchResultViewModel -> SearchResultRow(searchResultViewModel, currentFacility.uuid) }
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
      binding.patientSearchResultView.render(searchResultViewModel, currentFacilityUuid)
    }
  }

  class DiffCallback : DiffUtil.ItemCallback<SearchResultsItemType_old>() {

    override fun areItemsTheSame(oldItem: SearchResultsItemType_old, newItem: SearchResultsItemType_old): Boolean {
      return when {
        oldItem is InCurrentFacilityHeader && newItem is InCurrentFacilityHeader -> oldItem.facilityName == newItem.facilityName
        oldItem is NotInCurrentFacilityHeader && newItem is NotInCurrentFacilityHeader -> true
        oldItem is NoPatientsInCurrentFacility && newItem is NoPatientsInCurrentFacility -> true
        oldItem is SearchResultRow && newItem is SearchResultRow -> oldItem.searchResultViewModel.uuid == newItem.searchResultViewModel.uuid
        else -> false
      }
    }

    override fun areContentsTheSame(oldItem: SearchResultsItemType_old, newItem: SearchResultsItemType_old): Boolean {
      return oldItem == newItem
    }
  }
}
