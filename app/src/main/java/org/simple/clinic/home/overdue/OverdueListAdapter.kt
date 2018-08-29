package org.simple.clinic.home.overdue

import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotterknife.bindView
import org.simple.clinic.R

class OverdueListAdapter : ListAdapter<OverdueListItem, OverdueListViewHolder>(OverdueListDiffer()) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OverdueListViewHolder {
    val layout = LayoutInflater.from(parent.context).inflate(R.layout.item_overdue_list, parent, false)
    return OverdueListViewHolder(layout)
  }

  override fun onBindViewHolder(holder: OverdueListViewHolder, position: Int) {
    holder.render(getItem(position))
  }
}

data class OverdueListItem(
    val name: String,
    val gender: String,
    val age: Int,
    val bpSystolic: Int,
    val bpDiastolic: Int,
    val bpDate: String,
    val overdueDays: Int
)

class OverdueListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

  private val patientName by bindView<TextView>(R.id.overdue_patient_name)
  private val patientBP by bindView<TextView>(R.id.overdue_patient_bp)
  private val overdueDays by bindView<TextView>(R.id.overdue_days)
  private val patientGenderAge by bindView<TextView>(R.id.overdue_gender_age)

  fun render(item: OverdueListItem) {
    patientName.text = item.name
    patientBP.text = "${item.bpDate} days ago: ${item.bpSystolic} / ${item.bpDiastolic}"
    overdueDays.text = "${item.overdueDays} days overdue"
    patientGenderAge.text = "(${item.gender}, ${item.age})"
  }
}

class OverdueListDiffer : DiffUtil.ItemCallback<OverdueListItem>() {

  override fun areItemsTheSame(oldItem: OverdueListItem?, newItem: OverdueListItem?): Boolean {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun areContentsTheSame(oldItem: OverdueListItem?, newItem: OverdueListItem?): Boolean {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }
}
