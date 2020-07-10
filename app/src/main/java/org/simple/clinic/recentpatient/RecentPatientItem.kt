package org.simple.clinic.recentpatient

import androidx.recyclerview.widget.DiffUtil
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.recent_patient_item_view.*
import org.simple.clinic.R
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.displayIconRes
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.toLocalDateAtZone
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.recyclerview.ViewHolderX
import org.simple.clinic.widgets.visibleOrGone
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.UUID

data class RecentPatientItem(
    val uuid: UUID,
    val name: String,
    val age: Int,
    val gender: Gender,
    val lastSeen: Instant,
    val dateFormatter: DateTimeFormatter,
    val clock: UserClock,
    val isNewRegistration: Boolean
) : ItemAdapter.Item<UiEvent> {

  override fun layoutResId(): Int = R.layout.recent_patient_item_view

  override fun render(holder: ViewHolderX, subject: Subject<UiEvent>) {
    with(holder) {
      patientNameTextView.text = itemView.context.getString(R.string.recent_patients_itemview_title, name, age.toString())

      lastSeenTextView.text = dateFormatter.format(lastSeen.toLocalDateAtZone(clock.zone))
      genderImageView.setImageResource(gender.displayIconRes)

      newRegistrationTextView.visibleOrGone(isNewRegistration)

      itemView.setOnClickListener {
        subject.onNext(RecentPatientItemClicked(patientUuid = uuid))
      }
    }
  }

  class DiffCallback : DiffUtil.ItemCallback<RecentPatientItem>() {
    override fun areItemsTheSame(oldItem: RecentPatientItem, newItem: RecentPatientItem): Boolean {
      return oldItem.uuid == newItem.uuid
    }

    override fun areContentsTheSame(oldItem: RecentPatientItem, newItem: RecentPatientItem): Boolean {
      return oldItem == newItem
    }
  }
}
