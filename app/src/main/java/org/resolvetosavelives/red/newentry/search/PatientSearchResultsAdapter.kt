package org.resolvetosavelives.red.newentry.search

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotterknife.bindView
import org.resolvetosavelives.red.R

class PatientSearchResultsAdapter : RecyclerView.Adapter<PatientSearchResultsAdapter.ViewHolder>() {

  private var patients: List<Patient>? = null

  fun updateAndNotifyChanges(patients: List<Patient>) {
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
    return patients?.size
        ?: 0
  }

  class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val titleTextView by bindView<TextView>(R.id.patientsearch_item_title)
    private val bylineTextView by bindView<TextView>(R.id.patientsearch_item_byline)

    init {
      itemView.setOnClickListener({
        // Registering a click listener just to show touch feedback.
      })
    }

    fun render(patient: Patient) {
      if (patient.fullName.isNotBlank()) {
        titleTextView.text = patient.fullName
      } else {
        titleTextView.text = "(no name)"
      }

      if (patient.mobileNumber.isNotBlank()) {
        bylineTextView.text = patient.mobileNumber
      } else {
        bylineTextView.text = "(no number)"
      }
    }
  }
}
