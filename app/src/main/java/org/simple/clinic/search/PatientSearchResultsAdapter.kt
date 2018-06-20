package org.simple.clinic.search

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.reactivex.subjects.PublishSubject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.Period
import org.threeten.bp.ZoneOffset

class PatientSearchResultsAdapter : RecyclerView.Adapter<PatientSearchResultsAdapter.ViewHolder>() {

  private var patients: List<PatientSearchResult> = listOf()

  val itemClicks: PublishSubject<UiEvent> = PublishSubject.create<UiEvent>()

  fun updateAndNotifyChanges(patients: List<PatientSearchResult>) {
    this.patients = patients
    notifyDataSetChanged()
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    val holder = ViewHolder(inflater.inflate(R.layout.list_patient_search, parent, false))
    holder.setClickListener(itemClicks)
    return holder
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.searchResult = patients[position]
    holder.render()
  }

  override fun getItemCount(): Int {
    return patients.size
  }

  class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val titleTextView by bindView<TextView>(R.id.patientsearch_item_title)
    private val ageTextView by bindView<TextView>(R.id.patientsearch_item_age)
    private val bylineTextView by bindView<TextView>(R.id.patientsearch_item_byline)

    lateinit var searchResult: PatientSearchResult

    fun setClickListener(itemClicks: PublishSubject<UiEvent>) {
      itemView.setOnClickListener {
        itemClicks.onNext(SearchResultClicked(searchResult))
      }
    }

    fun render() {
      titleTextView.text = String.format("%s • %s", searchResult.fullName, searchResult.gender.toString().substring(0, 1))

      val storedAddress = searchResult.address
      if (storedAddress.colonyOrVillage.isNullOrEmpty()) {
        bylineTextView.text = String.format("%s", searchResult.address.district)
      } else {
        bylineTextView.text = String.format("%s, %s", searchResult.address.colonyOrVillage, searchResult.address.district)
      }

      if (searchResult.age == null) {
        val years = Period.between(searchResult.dateOfBirth, LocalDate.now()).years.toString()
        ageTextView.text = itemView.context.getString(R.string.patientsearch_age, years)

      } else {
        val ageUpdatedAt = LocalDateTime.ofInstant(searchResult.age!!.updatedAt, ZoneOffset.UTC)
        val updatedAtLocalDate = LocalDate.of(ageUpdatedAt.year, ageUpdatedAt.month, ageUpdatedAt.dayOfMonth)
        val yearsSinceThen = Period.between(updatedAtLocalDate, LocalDate.now()).years

        val oldAge = searchResult.age!!.value
        val currentAge = oldAge + yearsSinceThen
        ageTextView.text = itemView.context.getString(R.string.patientsearch_age, currentAge.toString())
      }

      if (searchResult.phoneNumber?.isNotEmpty() == true) {
        val current = bylineTextView.text
        bylineTextView.text = String.format("%s | $current", searchResult.phoneNumber)
      }
    }
  }
}
