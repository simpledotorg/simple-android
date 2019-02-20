package org.simple.clinic.patient.recent

import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.recent_patient_item_view.view.*
import org.simple.clinic.R
import org.simple.clinic.patient.Gender

class RecentPatientItem(private val data: Data) : Item<ViewHolder>() {

  override fun getLayout(): Int = R.layout.recent_patient_item_view

  override fun bind(viewHolder: ViewHolder, position: Int) {
    viewHolder.itemView.apply {
      recentpatient_item_title.text = data.title
      recentpatient_item_last_bp.text = data.lastBp
      recentpatient_item_gender.setImageResource(data.gender.displayIconRes)
    }
  }

  data class Data(
      val title: String,
      val lastBp: String,
      val gender: Gender
  )
}
