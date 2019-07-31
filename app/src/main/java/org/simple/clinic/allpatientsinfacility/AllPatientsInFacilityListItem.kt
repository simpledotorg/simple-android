package org.simple.clinic.allpatientsinfacility

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.list_allpatientsinfacility_facility_header.*
import kotlinx.android.synthetic.main.list_patient_search.*
import org.simple.clinic.R
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.recyclerview.ViewHolderX
import java.util.Locale
import java.util.UUID

sealed class AllPatientsInFacilityListItem : ItemAdapter.Item<AllPatientsInFacilityListItem.Event> {

  companion object {
    fun mapSearchResultsToListItems(
        facilityUiState: FacilityUiState,
        patientSearchResults: List<PatientSearchResultUiState>
    ): List<AllPatientsInFacilityListItem> {
      return patientSearchResults
          .map { patientSearchResult -> SearchResult(facilityUiState.uuid, patientSearchResult) }
          .let { searchResultItems -> listOf(FacilityHeader(facilityUiState.name)) + searchResultItems }
    }
  }

  abstract fun sectionTitle(locale: Locale): SectionTitle

  data class FacilityHeader(val facilityName: String) : AllPatientsInFacilityListItem() {

    override fun layoutResId(): Int {
      return R.layout.list_allpatientsinfacility_facility_header
    }

    override fun render(
        holder: ViewHolderX,
        subject: Subject<Event>
    ) {
      val resources = holder.itemView.resources
      holder.facilityLabel.text = resources.getString(R.string.allpatientsinfacility_foundpatients_header, facilityName)
    }

    override fun sectionTitle(locale: Locale): SectionTitle = SectionTitle.None
  }

  data class SearchResult(
      val facilityUuid: UUID,
      val patientSearchResultUiState: PatientSearchResultUiState
  ) : AllPatientsInFacilityListItem() {

    override fun layoutResId(): Int {
      return R.layout.list_patient_search
    }

    override fun render(
        holder: ViewHolderX,
        subject: Subject<Event>
    ) {
      holder.patientSearchResultView.render(patientSearchResultUiState, facilityUuid)
      holder.itemView.setOnClickListener { subject.onNext(Event.SearchResultClicked(patientSearchResultUiState.uuid)) }
    }

    override fun sectionTitle(locale: Locale): SectionTitle {
      return SectionTitle.Text(patientSearchResultUiState.fullName.take(1).toUpperCase(locale))
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
        oldItem is SearchResult && newItem is SearchResult -> oldItem.patientSearchResultUiState.uuid == newItem.patientSearchResultUiState.uuid
        else -> false
      }
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: AllPatientsInFacilityListItem, newItem: AllPatientsInFacilityListItem): Boolean {
      return oldItem == newItem
    }
  }
}
