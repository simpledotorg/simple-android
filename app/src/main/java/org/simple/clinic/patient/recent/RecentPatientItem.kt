package org.simple.clinic.patient.recent

import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import org.simple.clinic.R

class RecentPatientItem : Item<ViewHolder>() {
  override fun getLayout(): Int = R.layout.recent_patient_item_view

  override fun bind(viewHolder: ViewHolder, position: Int) {
  }
}
