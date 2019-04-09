package org.simple.clinic.search.results

import android.content.res.Resources
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.xwray.groupie.ViewHolder
import io.reactivex.subjects.Subject
import org.simple.clinic.R
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.summary.GroupieItemWithUiEvents
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.estimateCurrentAge
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Clock
import org.threeten.bp.ZoneOffset
import org.threeten.bp.format.DateTimeFormatter

sealed class PatientSearchResultsItemType(adapterId: Long) : GroupieItemWithUiEvents<ViewHolder>(adapterId) {

  override lateinit var uiEvents: Subject<UiEvent>

  data class InCurrentFacilityHeader(
      private val facilityName: String
  ) : PatientSearchResultsItemType(facilityName.hashCode().toLong()) {

    override fun getLayout(): Int = R.layout.list_patient_search_header

    override fun bind(viewHolder: ViewHolder, position: Int) {
      val header = viewHolder.itemView.findViewById<TextView>(R.id.patientsearch_header)

      header.text = viewHolder.itemView.context.getString(R.string.patientsearchresults_current_facility_header, facilityName)
    }
  }

  object NotInCurrentFacilityHeader : PatientSearchResultsItemType(0) {

    override fun getLayout(): Int = R.layout.list_patient_search_header

    override fun bind(viewHolder: ViewHolder, position: Int) {
      val header = viewHolder.itemView.findViewById<TextView>(R.id.patientsearch_header)

      header.text = viewHolder.itemView.context.getString(R.string.patientsearchresults_other_results_header)
    }
  }

  object NoPatientsInCurrentFacility : PatientSearchResultsItemType(1) {
    override fun getLayout(): Int = R.layout.list_patient_search_no_patients

    override fun bind(viewHolder: ViewHolder, position: Int) {
    }
  }

  data class PatientSearchResultRow(
      private val searchResult: PatientSearchResult,
      private val currentFacility: Facility,
      private val phoneObfuscator: PhoneNumberObfuscator,
      private val dateOfBirthFormat: DateTimeFormatter,
      private val clock: Clock,
      private val userClock: UserClock
  ) : PatientSearchResultsItemType(searchResult.hashCode().toLong()) {

    private lateinit var viewHolder: ViewHolder

    private val genderImageView by unsafeLazy { viewHolder.itemView.findViewById<ImageView>(R.id.patientsearchresult_item_gender) }
    private val titleTextView by unsafeLazy { viewHolder.itemView.findViewById<TextView>(R.id.patientsearchresult_item_title) }
    private val addressTextView by unsafeLazy { viewHolder.itemView.findViewById<TextView>(R.id.patientsearchresult_item_address) }
    private val dateOfBirthTextView by unsafeLazy { viewHolder.itemView.findViewById<TextView>(R.id.patientsearchresult_item_dateofbirth) }
    private val phoneNumberTextView by unsafeLazy { viewHolder.itemView.findViewById<TextView>(R.id.patientsearchresult_item_phone) }
    private val lastBpDateAndFacilityTextView by unsafeLazy { viewHolder.itemView.findViewById<TextView>(R.id.patientsearchresults_item_last_bp) }
    private val lastBpDateFrame by unsafeLazy { viewHolder.itemView.findViewById<View>(R.id.patientsearchresults_item_last_bp_container) }

    override fun getLayout(): Int = R.layout.list_patient_search

    override fun bind(viewHolder: ViewHolder, position: Int) {
      this.viewHolder = viewHolder

      viewHolder.itemView.setOnClickListener {
        uiEvents.onNext(PatientSearchResultClicked(searchResult))
      }

      val resources = viewHolder.itemView.resources
      renderPatientNameAgeAndGender(resources, clock)
      renderPatientAddress(resources)
      renderPatientDateOfBirth(dateOfBirthFormat)
      renderPatientPhoneNumber(phoneObfuscator)
      renderLastRecordedBloodPressure(dateOfBirthFormat, currentFacility, resources)
    }

    private fun renderLastRecordedBloodPressure(
        dateOfBirthFormat: DateTimeFormatter,
        currentFacility: Facility,
        resources: Resources
    ) {
      val lastBp = searchResult.lastBp
      if (lastBp == null) {
        lastBpDateFrame.visibility = View.GONE
      } else {
        lastBpDateFrame.visibility = View.VISIBLE

        val lastBpDate = lastBp
            .takenOn
            .atZone(ZoneOffset.UTC)
            .withZoneSameInstant(userClock.zone)
            .toLocalDate()
        val formattedLastBpDate = dateOfBirthFormat.format(lastBpDate)

        val isCurrentFacility = lastBp.takenAtFacilityUuid == currentFacility.uuid
        if (isCurrentFacility) {
          lastBpDateAndFacilityTextView.text = formattedLastBpDate
        } else {
          lastBpDateAndFacilityTextView.text = resources.getString(
              R.string.patientsearchresults_item_last_bp_date_with_facility,
              formattedLastBpDate,
              lastBp.takenAtFacilityName)
        }
      }
    }

    private fun renderPatientPhoneNumber(phoneObfuscator: PhoneNumberObfuscator) {
      val phoneNumber = searchResult.phoneNumber
      if (phoneNumber.isNullOrBlank()) {
        phoneNumberTextView.visibility = View.GONE
      } else {
        phoneNumberTextView.visibility = View.VISIBLE
        phoneNumberTextView.text = phoneObfuscator.obfuscate(phoneNumber)
      }
    }

    private fun renderPatientDateOfBirth(dateOfBirthFormat: DateTimeFormatter) {
      val dateOfBirth = searchResult.dateOfBirth
      if (dateOfBirth == null) {
        dateOfBirthTextView.visibility = View.GONE
      } else {
        dateOfBirthTextView.visibility = View.VISIBLE
        dateOfBirthTextView.text = dateOfBirthFormat.format(dateOfBirth)
      }
    }

    private fun renderPatientAddress(resources: Resources) {
      val address = searchResult.address
      if (address.colonyOrVillage.isNullOrEmpty()) {
        addressTextView.text = searchResult.address.district
      } else {
        addressTextView.text = resources.getString(
            R.string.patientsearchresults_item_address_with_colony_and_district,
            searchResult.address.colonyOrVillage,
            searchResult.address.district)
      }
    }

    private fun renderPatientNameAgeAndGender(resources: Resources, clock: Clock) {
      genderImageView.setImageResource(searchResult.gender.displayIconRes)

      val age = when (searchResult.age) {
        null -> {
          estimateCurrentAge(searchResult.dateOfBirth!!, clock)
        }
        else -> {
          val (recordedAge, ageRecordedAtTimestamp, _) = searchResult.age
          estimateCurrentAge(recordedAge, ageRecordedAtTimestamp, clock)
        }
      }

      titleTextView.text = resources.getString(
          R.string.patientsearchresults_item_name_with_gender_and_age,
          searchResult.fullName,
          resources.getString(searchResult.gender.displayLetterRes),
          age)
    }
  }
}
