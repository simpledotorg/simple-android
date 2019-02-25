package org.simple.clinic.patient.recent

import android.annotation.SuppressLint
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.recent_patient_item_view.view.*
import org.simple.clinic.R
import org.simple.clinic.patient.Gender

class RecentPatientItem(
    val name: String,
    val age: Int,
    val lastBp: String,
    val gender: Gender
) : Item<ViewHolder>() {

  override fun getLayout(): Int = R.layout.recent_patient_item_view

  @SuppressLint("SetTextI18n")
  override fun bind(viewHolder: ViewHolder, position: Int) {
    viewHolder.itemView.apply {
      recentpatient_item_title.text = "$name, $age"
      recentpatient_item_last_bp.text = lastBp
      recentpatient_item_gender.setImageResource(gender.displayIconRes)
    }
  }
}
