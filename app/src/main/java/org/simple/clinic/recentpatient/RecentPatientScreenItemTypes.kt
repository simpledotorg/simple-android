package org.simple.clinic.recentpatient

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.xwray.groupie.ViewHolder
import io.reactivex.subjects.Subject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.patient.Gender
import org.simple.clinic.recentpatient.DateHeader.DateHeaderViewHolder
import org.simple.clinic.recentpatient.RecentPatientItem.RecentPatientItemViewHolder
import org.simple.clinic.recentpatient.RelativeTimestamp.OlderThanTwoDays
import org.simple.clinic.recentpatient.RelativeTimestamp.Today
import org.simple.clinic.recentpatient.RelativeTimestamp.Yesterday
import org.simple.clinic.summary.GroupieItemWithUiEvents
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.visibleOrGone
import org.threeten.bp.Instant
import org.threeten.bp.format.DateTimeFormatter
import java.util.UUID

sealed class RecentPatientScreenItemTypes<VH : ViewHolder>(adapterId: Long) : GroupieItemWithUiEvents<VH>(adapterId) {
  override lateinit var uiEvents: Subject<UiEvent>
}

data class RecentPatientItem(
    val uuid: UUID,
    val name: String,
    val age: Int,
    val lastBp: LastBp?,
    val gender: Gender,
    val updatedAt: Instant
) : RecentPatientScreenItemTypes<RecentPatientItemViewHolder>(uuid.hashCode().toLong()) {

  override fun getLayout(): Int = R.layout.recent_patient_item_view

  override fun createViewHolder(itemView: View): RecentPatientItemViewHolder {
    return RecentPatientItemViewHolder(itemView)
  }

  @SuppressLint("SetTextI18n")
  override fun bind(viewHolder: RecentPatientItemViewHolder, position: Int) {
    viewHolder.apply {
      title.text = "$name, $age"

      val lastBpText = lastBpText(itemView.context)
      lastBp.text = lastBpText
      lastBpLabel.visibleOrGone(lastBpText.isNotBlank())
      genderImageView.setImageResource(gender.displayIconRes)

      itemView.setOnClickListener {
        uiEvents.onNext(RecentPatientItemClicked(patientUuid = uuid))
      }
    }
  }

  private fun lastBpText(context: Context): String =
      lastBp?.run { "$systolic/$diastolic, ${updatedAtRelativeTimestamp.displayText(context)}" } ?: ""

  data class LastBp(
      val systolic: Int,
      val diastolic: Int,
      val updatedAtRelativeTimestamp: org.simple.clinic.summary.RelativeTimestamp
  )

  class RecentPatientItemViewHolder(rootView: View) : ViewHolder(rootView) {
    val title by bindView<TextView>(R.id.recentpatient_item_title)
    val lastBp by bindView<TextView>(R.id.recentpatient_item_last_bp)
    val lastBpLabel by bindView<View>(R.id.recentpatient_item_last_bp_label)
    val genderImageView by bindView<ImageView>(R.id.recentpatient_item_gender)
  }
}

data class DateHeader(
    private val relativeTimestamp: RelativeTimestamp,
    private val dateTimeFormatter: DateTimeFormatter
) : RecentPatientScreenItemTypes<DateHeaderViewHolder>(relativeTimestamp.hashCode().toLong()) {

  override fun getLayout(): Int = R.layout.recentpatient_date_header_item_view

  override fun createViewHolder(itemView: View): DateHeaderViewHolder {
    return DateHeaderViewHolder(itemView)
  }

  @SuppressLint("SetTextI18n")
  override fun bind(viewHolder: DateHeaderViewHolder, position: Int) {
    val context = viewHolder.itemView.context

    viewHolder.title.text = when (relativeTimestamp) {
      Today -> context.getString(R.string.timestamp_today)
      Yesterday -> context.getString(R.string.timestamp_yesterday)
      is OlderThanTwoDays -> dateTimeFormatter.format(relativeTimestamp.date)
    }
  }

  class DateHeaderViewHolder(rootView: View) : ViewHolder(rootView) {
    val title by bindView<TextView>(R.id.dateheader_title)
  }

}
