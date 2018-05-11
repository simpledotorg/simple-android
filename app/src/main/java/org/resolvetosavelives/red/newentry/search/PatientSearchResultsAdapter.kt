package org.resolvetosavelives.red.newentry.search

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.reactivex.functions.Consumer
import kotterknife.bindView
import org.resolvetosavelives.red.R

class PatientSearchResultsAdapter : RecyclerView.Adapter<PatientSearchResultsAdapter.ViewHolder>(), Consumer<List<Patient>> {

  private var patients: List<Patient>? = null

  override fun accept(patients: List<Patient>) {
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
    private val titleTextView: TextView by bindView(R.id.patientsearch_item_title)
    private val bylineTextView: TextView by bindView(R.id.patientsearch_item_byline)

    fun render(patient: Patient) {
      titleTextView.text = patient.fullName
      bylineTextView.text = patient.mobileNumber
    }
  }
}
