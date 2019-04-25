package org.simple.clinic.searchresultsview

import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.xwray.groupie.ViewHolder
import io.reactivex.subjects.Subject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.searchresultsview.SearchResultsItemType.SearchResultRow.SearchResultRowViewHolder
import org.simple.clinic.summary.GroupieItemWithUiEvents
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.estimateCurrentAge
import org.simple.clinic.util.toLocalDateAtZone
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Clock
import org.threeten.bp.format.DateTimeFormatter

sealed class SearchResultsItemType<T : ViewHolder>(adapterId: Long) : GroupieItemWithUiEvents<T>(adapterId) {

  companion object {
    private const val HEADER_NOT_IN_CURRENT_FACILITY = 0L
    private const val HEADER_NO_PATIENTS_IN_CURRENT_FACILITY = 1L
  }

  override lateinit var uiEvents: Subject<UiEvent>

  data class InCurrentFacilityHeader(
      private val facilityName: String
  ) : SearchResultsItemType<ViewHolder>(facilityName.hashCode().toLong()) {

    override fun getLayout(): Int = R.layout.list_patient_search_header

    override fun bind(viewHolder: ViewHolder, position: Int) {
      val header = viewHolder.itemView.findViewById<TextView>(R.id.patientsearch_header)

      header.text = viewHolder.itemView.context.getString(R.string.patientsearchresults_current_facility_header, facilityName)
    }
  }

  object NotInCurrentFacilityHeader : SearchResultsItemType<ViewHolder>(HEADER_NOT_IN_CURRENT_FACILITY) {

    override fun getLayout(): Int = R.layout.list_patient_search_header

    override fun bind(viewHolder: ViewHolder, position: Int) {
      val header = viewHolder.itemView.findViewById<TextView>(R.id.patientsearch_header)

      header.text = viewHolder.itemView.context.getString(R.string.patientsearchresults_other_results_header)
    }
  }

  object NoPatientsInCurrentFacility : SearchResultsItemType<ViewHolder>(HEADER_NO_PATIENTS_IN_CURRENT_FACILITY) {
    override fun getLayout(): Int = R.layout.list_patient_search_no_patients

    override fun bind(viewHolder: ViewHolder, position: Int) {
    }
  }

  data class SearchResultRow(
      private val searchResult: PatientSearchResult,
      private val currentFacility: Facility,
      private val phoneObfuscator: PhoneNumberObfuscator,
      private val dateOfBirthFormat: DateTimeFormatter,
      private val clock: Clock,
      private val userClock: UserClock
  ) : SearchResultsItemType<SearchResultRowViewHolder>(searchResult.hashCode().toLong()) {

    override fun getLayout(): Int = R.layout.list_patient_search

    override fun createViewHolder(itemView: View) = SearchResultRowViewHolder(itemView)

    override fun bind(viewHolder: SearchResultRowViewHolder, position: Int) {
      viewHolder.itemView.setOnClickListener {
        uiEvents.onNext(SearchResultClicked(searchResult))
      }

      val resources = viewHolder.itemView.resources
      renderPatientNameAgeAndGender(viewHolder, resources)
      renderPatientAddress(viewHolder, resources)
      renderPatientDateOfBirth(viewHolder)
      renderPatientPhoneNumber(viewHolder)
      renderLastRecordedBloodPressure(viewHolder, resources)
    }

    private fun renderLastRecordedBloodPressure(
        holder: SearchResultRowViewHolder,
        resources: Resources
    ) {
      val lastBp = searchResult.lastBp
      if (lastBp == null) {
        holder.lastBpDateFrame.visibility = View.GONE
      } else {
        holder.lastBpDateFrame.visibility = View.VISIBLE

        val lastBpDate = lastBp.takenOn.toLocalDateAtZone(userClock.zone)
        val formattedLastBpDate = dateOfBirthFormat.format(lastBpDate)

        val isCurrentFacility = lastBp.takenAtFacilityUuid == currentFacility.uuid
        if (isCurrentFacility) {
          holder.lastBpDateAndFacilityTextView.text = formattedLastBpDate
        } else {
          holder.lastBpDateAndFacilityTextView.text = resources.getString(
              R.string.patientsearchresults_item_last_bp_date_with_facility,
              formattedLastBpDate,
              lastBp.takenAtFacilityName)
        }
      }
    }

    private fun renderPatientPhoneNumber(holder: SearchResultRowViewHolder) {
      val phoneNumber = searchResult.phoneNumber
      if (phoneNumber.isNullOrBlank()) {
        holder.phoneNumberTextView.visibility = View.GONE
      } else {
        holder.phoneNumberTextView.visibility = View.VISIBLE
        holder.phoneNumberTextView.text = phoneObfuscator.obfuscate(phoneNumber)
      }
    }

    private fun renderPatientDateOfBirth(holder: SearchResultRowViewHolder) {
      val dateOfBirth = searchResult.dateOfBirth
      if (dateOfBirth == null) {
        holder.dateOfBirthTextView.visibility = View.GONE
      } else {
        holder.dateOfBirthTextView.visibility = View.VISIBLE
        holder.dateOfBirthTextView.text = dateOfBirthFormat.format(dateOfBirth)
      }
    }

    private fun renderPatientAddress(holder: SearchResultRowViewHolder, resources: Resources) {
      val address = searchResult.address
      if (address.colonyOrVillage.isNullOrEmpty()) {
        holder.addressTextView.text = searchResult.address.district
      } else {
        holder.addressTextView.text = resources.getString(
            R.string.patientsearchresults_item_address_with_colony_and_district,
            searchResult.address.colonyOrVillage,
            searchResult.address.district)
      }
    }

    private fun renderPatientNameAgeAndGender(holder: SearchResultRowViewHolder, resources: Resources) {
      holder.genderImageView.setImageResource(searchResult.gender.displayIconRes)

      val age = when (searchResult.age) {
        null -> {
          estimateCurrentAge(searchResult.dateOfBirth!!, clock)
        }
        else -> {
          val (recordedAge, ageRecordedAtTimestamp, _) = searchResult.age
          estimateCurrentAge(recordedAge, ageRecordedAtTimestamp, clock)
        }
      }

      holder.titleTextView.text = resources.getString(
          R.string.patientsearchresults_item_name_with_gender_and_age,
          searchResult.fullName,
          resources.getString(searchResult.gender.displayLetterRes),
          age)
    }

    class SearchResultRowViewHolder(rootView: View) : ViewHolder(rootView) {
      val genderImageView by bindView<ImageView>(R.id.patientsearchresult_item_gender)
      val titleTextView by bindView<TextView>(R.id.patientsearchresult_item_title)
      val addressTextView by bindView<TextView>(R.id.patientsearchresult_item_address)
      val dateOfBirthTextView by bindView<TextView>(R.id.patientsearchresult_item_dateofbirth)
      val phoneNumberTextView by bindView<TextView>(R.id.patientsearchresult_item_phone)
      val lastBpDateAndFacilityTextView by bindView<TextView>(R.id.patientsearchresults_item_last_bp)
      val lastBpDateFrame by bindView<ViewGroup>(R.id.patientsearchresults_item_last_bp_container)
    }
  }
}
