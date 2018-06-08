package org.resolvetosavelives.red.search

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotterknife.bindView
import org.resolvetosavelives.red.R
import org.resolvetosavelives.red.patient.PatientSearchResult
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.Period
import org.threeten.bp.ZoneOffset

class PatientSearchResultsAdapter : RecyclerView.Adapter<PatientSearchResultsAdapter.ViewHolder>() {

  private var patients: List<PatientSearchResult>? = null

  fun updateAndNotifyChanges(patients: List<PatientSearchResult>) {
    this.patients = patients
    notifyDataSetChanged()
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

    val inflater = LayoutInflater.from(parent.context)
    return ViewHolder(inflater.inflate(R.layout.list_patient_search, parent, false))
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.render(patients!![position])
  }

  override fun getItemCount(): Int {
    return patients?.size ?: 0
  }

  class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val titleTextView by bindView<TextView>(R.id.patientsearch_item_title)
    private val ageTextView by bindView<TextView>(R.id.patientsearch_item_age)
    private val bylineTextView by bindView<TextView>(R.id.patientsearch_item_byline)

    init {
      itemView.setOnClickListener({
        // Registering a click listener just to show touch feedback.
      })
    }

    fun render(patient: PatientSearchResult) {
      titleTextView.text = String.format("%s • %s", patient.fullName, patient.gender.toString().substring(0, 1))
      bylineTextView.text = String.format("%s, %s", patient.address.colonyOrVillage, patient.address.district)

      if (patient.age == null) {
        val years = Period.between(patient.dateOfBirth, LocalDate.now()).years.toString()
        ageTextView.text = itemView.context.getString(R.string.patientsearch_age, years)

      } else {
        val ageUpdatedAt = LocalDateTime.ofInstant(patient.age.updatedAt, ZoneOffset.UTC)
        val updatedAtLocalDate = LocalDate.of(ageUpdatedAt.year, ageUpdatedAt.month, ageUpdatedAt.dayOfMonth)
        val yearsSinceThen = Period.between(updatedAtLocalDate, LocalDate.now()).years

        val oldAge = patient.age.value
        val currentAge = oldAge + yearsSinceThen
        ageTextView.text = itemView.context.getString(R.string.patientsearch_age, currentAge.toString())
      }

      if (patient.phoneNumber?.isNotEmpty() == true) {
        val current = bylineTextView.text
        bylineTextView.text = String.format("%s | $current", patient.phoneNumber)
      }
    }
  }
}
