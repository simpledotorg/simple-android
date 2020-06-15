package org.simple.clinic.recentpatientsview

import androidx.recyclerview.widget.DiffUtil
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.recent_patient_item_view.*
import kotlinx.android.synthetic.main.see_all_item_view.*
import org.simple.clinic.R
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.displayIconRes
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.toLocalDateAtZone
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.recyclerview.ViewHolderX
import org.simple.clinic.widgets.visibleOrGone
import org.threeten.bp.Instant
import org.threeten.bp.format.DateTimeFormatter
import java.util.UUID

sealed class RecentPatientItemType : ItemAdapter.Item<UiEvent>

data class RecentPatientItem(
    val uuid: UUID,
    val name: String,
    val age: Int,
    val gender: Gender,
    val updatedAt: Instant,
    val dateFormatter: DateTimeFormatter,
    val clock: UserClock,
    val isNewRegistration: Boolean
) : RecentPatientItemType() {

  override fun layoutResId(): Int = R.layout.recent_patient_item_view

  override fun render(holder: ViewHolderX, subject: Subject<UiEvent>) {
    val context = holder.itemView.context

    holder.itemView.setOnClickListener {
      subject.onNext(RecentPatientItemClicked(patientUuid = uuid))
    }

    holder.newRegistrationTextView.visibleOrGone(isNewRegistration)
    holder.patientNameTextView.text = context.resources.getString(R.string.patients_recentpatients_nameage, name, age.toString())
    holder.genderImageView.setImageResource(gender.displayIconRes)
    holder.lastSeenTextView.text = dateFormatter.format(updatedAt.toLocalDateAtZone(clock.zone))
  }
}

object SeeAllItem : RecentPatientItemType() {
  override fun layoutResId(): Int = R.layout.see_all_item_view

  override fun render(holder: ViewHolderX, subject: Subject<UiEvent>) {
    holder.seeAllButton.setOnClickListener {
      subject.onNext(SeeAllItemClicked)
    }
  }
}

class RecentPatientItemTTypeDiffCallback : DiffUtil.ItemCallback<RecentPatientItemType>() {
  override fun areItemsTheSame(oldItem: RecentPatientItemType, newItem: RecentPatientItemType): Boolean {
    return when {
      oldItem is SeeAllItem && newItem is SeeAllItem -> true
      oldItem is RecentPatientItem && newItem is RecentPatientItem -> oldItem.uuid == newItem.uuid
      else -> false
    }
  }

  override fun areContentsTheSame(oldItem: RecentPatientItemType, newItem: RecentPatientItemType): Boolean {
    return oldItem == newItem
  }
}
