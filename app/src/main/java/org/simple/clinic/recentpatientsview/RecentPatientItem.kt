package org.simple.clinic.recentpatientsview

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import com.xwray.groupie.ViewHolder
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.recent_patient_item_view.view.*
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.patient.Gender
import org.simple.clinic.recentpatientsview.SeeAllItem.SeeAllItemViewHolder
import org.simple.clinic.summary.GroupieItemWithUiEvents
import org.simple.clinic.summary.RelativeTimestamp
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.visibleOrGone
import java.util.UUID

sealed class RecentPatientItemType<VH : ViewHolder>(adapterId: Long) : GroupieItemWithUiEvents<VH>(adapterId) {
  override lateinit var uiEvents: Subject<UiEvent>
}

data class RecentPatientItem(
    val uuid: UUID,
    val name: String,
    val age: Int,
    val lastBp: LastBp?,
    val gender: Gender
) : RecentPatientItemType<ViewHolder>(uuid.hashCode().toLong()) {

  override fun getLayout(): Int = R.layout.recent_patient_item_view

  @SuppressLint("SetTextI18n")
  override fun bind(viewHolder: ViewHolder, position: Int) {
    viewHolder.itemView.apply {
      recentpatient_item_title.text = "$name, $age"

      val lastBpText = lastBpText(context)
      recentpatient_item_last_bp.text = lastBpText
      recentpatient_item_last_bp_label.visibleOrGone(lastBpText.isNotBlank())
      recentpatient_item_gender.setImageResource(gender.displayIconRes)

      setOnClickListener {
        uiEvents.onNext(RecentPatientItemClicked(patientUuid = uuid))
      }
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

object SeeAllItem : RecentPatientItemType<SeeAllItemViewHolder>(0) {
  override fun getLayout() = R.layout.see_all_item_view

  override fun createViewHolder(itemView: View): SeeAllItemViewHolder {
    return SeeAllItemViewHolder(itemView)
  }

  override fun bind(viewHolder: SeeAllItemViewHolder, position: Int) {
    viewHolder.seeAllButton.setOnClickListener {
      uiEvents.onNext(SeeAllItemClicked)
    }
  }

  class SeeAllItemViewHolder(rootView: View) : ViewHolder(rootView) {
    val seeAllButton by bindView<View>(R.id.seeall_button)
  }
}
