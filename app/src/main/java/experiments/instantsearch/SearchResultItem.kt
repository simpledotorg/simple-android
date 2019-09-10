package experiments.instantsearch

import androidx.recyclerview.widget.DiffUtil
import experiments.instantsearch.SearchResultItem.Event.SearchResultClicked
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.list_patient_search.*
import kotlinx.android.synthetic.main.list_patient_search_header.*
import org.simple.clinic.R
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.PatientSearchResultItemView.PatientSearchResultViewModel
import org.simple.clinic.widgets.recyclerview.ViewHolderX
import java.util.UUID

sealed class SearchResultItem : ItemAdapter.Item<SearchResultItem.Event> {

  companion object {

    fun from(results: PatientSearchResults): List<SearchResultItem> {
      return when {
        results.hasNoResults -> emptyList()
        else -> generateSearchResultListItems(results)
      }
    }

    private fun generateSearchResultListItems(
        results: PatientSearchResults
    ): List<SearchResultItem> {
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
    ): List<SearchResultItem> {

      val currentFacilityHeader = InCurrentFacility(currentFacility)

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
    ): List<SearchResultItem> {

      return if (searchResultRows.isNotEmpty()) {
        listOf(
            NotInCurrentFacility,
            *searchResultRows.toTypedArray()
        )
      } else {
        emptyList()
      }
    }
  }

  data class InCurrentFacility(val facility: Facility) : SearchResultItem() {
    override fun layoutResId(): Int {
      return R.layout.list_patient_search_header
    }

    override fun render(holder: ViewHolderX, subject: Subject<Event>) {
      val header = holder.patientsearch_header

      header.text = holder.itemView.context.getString(R.string.patientsearchresults_current_facility_header, facility.name)
    }
  }

  object NotInCurrentFacility : SearchResultItem() {
    override fun layoutResId(): Int {
      return R.layout.list_patient_search_header
    }

    override fun render(holder: ViewHolderX, subject: Subject<Event>) {
      val header = holder.patientsearch_header

      header.text = holder.itemView.context.getString(R.string.patientsearchresults_other_results_header)
    }
  }

  object NoPatientsInCurrentFacility : SearchResultItem() {
    override fun layoutResId(): Int {
      return R.layout.list_patient_search_no_patients
    }

    override fun render(holder: ViewHolderX, subject: Subject<Event>) {
    }
  }

  data class SearchResultRow(
      val searchResultViewModel: PatientSearchResultViewModel,
      val facility: Facility
  ) : SearchResultItem() {

    companion object {
      fun forSearchResults(
          searchResults: List<PatientSearchResult>,
          currentFacility: Facility
      ): List<SearchResultRow> {
        return searchResults
            .map(::mapPatientSearchResultToViewModel)
            .map { searchResultViewModel -> SearchResultRow(searchResultViewModel, currentFacility) }
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
            lastBp = searchResult.lastBp
        )
      }
    }

    override fun layoutResId(): Int {
      return R.layout.list_patient_search
    }

    override fun render(holder: ViewHolderX, subject: Subject<Event>) {
      val patientSearchResultView = holder.patientSearchResultView

      patientSearchResultView.setOnClickListener {
        subject.onNext(SearchResultClicked(searchResultViewModel.uuid))
      }

      patientSearchResultView.render(searchResultViewModel, facility.uuid)
    }
  }

  sealed class Event {
    data class SearchResultClicked(val patientUuid: UUID) : Event()
  }

  class DiffCallback : DiffUtil.ItemCallback<SearchResultItem>() {

    override fun areItemsTheSame(oldItem: SearchResultItem, newItem: SearchResultItem): Boolean {
      return when {
        oldItem is InCurrentFacility && newItem is InCurrentFacility -> true
        oldItem is NotInCurrentFacility && newItem is NotInCurrentFacility -> true
        oldItem is NoPatientsInCurrentFacility && newItem is NoPatientsInCurrentFacility -> true
        oldItem is SearchResultRow && newItem is SearchResultRow -> oldItem.searchResultViewModel.uuid == newItem.searchResultViewModel.uuid
        else -> false
      }
    }

    override fun areContentsTheSame(oldItem: SearchResultItem, newItem: SearchResultItem): Boolean {
      return when {
        oldItem is InCurrentFacility && newItem is InCurrentFacility -> oldItem.facility == newItem.facility
        oldItem is NotInCurrentFacility && newItem is NotInCurrentFacility -> true
        oldItem is NoPatientsInCurrentFacility && newItem is NoPatientsInCurrentFacility -> true
        oldItem is SearchResultRow && newItem is SearchResultRow -> {
          (oldItem.searchResultViewModel == newItem.searchResultViewModel) && (oldItem.facility == newItem.facility)
        }
        else -> false
      }
    }
  }
}
