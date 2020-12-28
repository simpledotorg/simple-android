package org.simple.clinic.allpatientsinfacility

import android.annotation.SuppressLint
import androidx.annotation.VisibleForTesting
import androidx.recyclerview.widget.DiffUtil
import io.reactivex.subjects.Subject
import org.simple.clinic.R
import org.simple.clinic.databinding.ListAllpatientsinfacilityFacilityHeaderBinding
import org.simple.clinic.databinding.ListPatientSearchOldBinding
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.PatientSearchResultItemView_Old.PatientSearchResultViewModel
import org.simple.clinic.widgets.recyclerview.BindingViewHolder
import java.util.Locale
import java.util.UUID

sealed class AllPatientsInFacilityListItem : ItemAdapter.Item<AllPatientsInFacilityListItem.Event> {

  companion object {
    fun mapSearchResultsToListItems(
        facilityUiState: FacilityUiState,
        patientSearchResults: List<PatientSearchResultUiState>
    ): List<AllPatientsInFacilityListItem> {
      return patientSearchResults
          .map(this::mapSearchResultUiStateToViewModel)
          .map { patientSearchResult -> SearchResult(facilityUiState.uuid, patientSearchResult) }
          .let { searchResultItems -> listOf(FacilityHeader(facilityUiState.name)) + searchResultItems }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun mapSearchResultUiStateToViewModel(searchResultUiState: PatientSearchResultUiState): PatientSearchResultViewModel {
      return PatientSearchResultViewModel(
          uuid = searchResultUiState.uuid,
          fullName = searchResultUiState.fullName,
          gender = searchResultUiState.gender,
          age = searchResultUiState.age,
          dateOfBirth = searchResultUiState.dateOfBirth,
          address = searchResultUiState.address,
          phoneNumber = searchResultUiState.phoneNumber,
          lastSeen = searchResultUiState.lastSeen
      )
    }
  }

  abstract fun sectionTitle(locale: Locale): SectionTitle

  data class FacilityHeader(val facilityName: String) : AllPatientsInFacilityListItem() {

    override fun layoutResId(): Int {
      return R.layout.list_allpatientsinfacility_facility_header
    }

    override fun render(
        holder: BindingViewHolder,
        subject: Subject<Event>
    ) {
      val resources = holder.itemView.resources
      val binding = holder.binding as ListAllpatientsinfacilityFacilityHeaderBinding

      binding.facilityLabel.text = resources.getString(R.string.allpatientsinfacility_foundpatients_header, facilityName)
    }

    override fun sectionTitle(locale: Locale): SectionTitle = SectionTitle.None
  }

  data class SearchResult(
      val facilityUuid: UUID,
      val searchResultViewModel: PatientSearchResultViewModel
  ) : AllPatientsInFacilityListItem() {

    override fun layoutResId(): Int {
      return R.layout.list_patient_search_old
    }

    override fun render(
        holder: BindingViewHolder,
        subject: Subject<Event>
    ) {
      val binding = holder.binding as ListPatientSearchOldBinding

      binding.patientSearchResultView.render(searchResultViewModel, facilityUuid)
      holder.itemView.setOnClickListener { subject.onNext(Event.SearchResultClicked(searchResultViewModel.uuid)) }
    }

    override fun sectionTitle(locale: Locale): SectionTitle {
      return SectionTitle.Text(searchResultViewModel.fullName.take(1).toUpperCase(locale))
    }
  }

  sealed class Event {
    data class SearchResultClicked(val patientUuid: UUID) : AllPatientsInFacilityListItem.Event()
  }

  sealed class SectionTitle {
    object None : SectionTitle()
    data class Text(val title: String) : SectionTitle()
  }

  class AllPatientsInFacilityListItemCallback : DiffUtil.ItemCallback<AllPatientsInFacilityListItem>() {

    override fun areItemsTheSame(oldItem: AllPatientsInFacilityListItem, newItem: AllPatientsInFacilityListItem): Boolean {
      return when {
        oldItem is FacilityHeader && newItem is FacilityHeader -> true
        oldItem is SearchResult && newItem is SearchResult -> oldItem.searchResultViewModel.uuid == newItem.searchResultViewModel.uuid
        else -> false
      }
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: AllPatientsInFacilityListItem, newItem: AllPatientsInFacilityListItem): Boolean {
      return oldItem == newItem
    }
  }
}
