package org.simple.clinic.patient.recent

import android.annotation.SuppressLint
import android.content.Context
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.recent_patient_item_view.view.*
import org.simple.clinic.R
import org.simple.clinic.patient.Gender
import org.simple.clinic.summary.RelativeTimestamp
import org.simple.clinic.widgets.visibleOrGone
import java.util.UUID

data class RecentPatientItem(
    val uuid: UUID,
    val name: String,
    val age: Int,
    val lastBp: LastBp?,
    val gender: Gender
) : Item<ViewHolder>() {

  override fun getLayout(): Int = R.layout.recent_patient_item_view

  @SuppressLint("SetTextI18n")
  override fun bind(viewHolder: ViewHolder, position: Int) {
    viewHolder.itemView.apply {
      recentpatient_item_title.text = "$name, $age"

      val lastBpText = lastBpText(context)
      recentpatient_item_last_bp.text = lastBpText
      recentpatient_item_last_bp_label.visibleOrGone(lastBpText.isNotBlank())

      recentpatient_item_gender.setImageResource(gender.displayIconRes)
    }
  }

  private fun lastBpText(context: Context): String =
      lastBp?.run { "$systolic/$diastolic, ${updatedAtRelativeTimestamp.displayText(context)}" } ?: ""

  data class LastBp(
      val systolic: Int,
      val diastolic: Int,
      val updatedAtRelativeTimestamp: RelativeTimestamp
  )
}
