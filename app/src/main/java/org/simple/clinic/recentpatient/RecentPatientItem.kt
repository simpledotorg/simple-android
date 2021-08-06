package org.simple.clinic.recentpatient

import androidx.paging.PagingData
import androidx.paging.map
import androidx.recyclerview.widget.DiffUtil
import io.reactivex.subjects.Subject
import org.simple.clinic.R
import org.simple.clinic.databinding.RecentPatientItemViewBinding
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.RecentPatient
import org.simple.clinic.patient.displayIconRes
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.toLocalDateAtZone
import org.simple.clinic.widgets.PagingItemAdapter
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.recyclerview.BindingViewHolder
import org.simple.clinic.widgets.visibleOrGone
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

data class RecentPatientItem(
    val uuid: UUID,
    val name: String,
    val age: Int,
    val gender: Gender,
    val updatedAt: Instant,
    val dateFormatter: DateTimeFormatter,
    val clock: UserClock,
    val isNewRegistration: Boolean
) : PagingItemAdapter.Item<UiEvent> {

  companion object {
    fun create(
        recentPatients: PagingData<RecentPatient>,
        userClock: UserClock,
        dateFormatter: DateTimeFormatter
    ): PagingData<RecentPatientItem> {
      val today = LocalDate.now(userClock)

      return recentPatients.map { recentPatientItem(it, today, userClock, dateFormatter) }
    }

    private fun recentPatientItem(
        recentPatient: RecentPatient,
        today: LocalDate,
        userClock: UserClock,
        dateFormatter: DateTimeFormatter
    ): RecentPatientItem {
      val patientRegisteredOnDate = recentPatient.patientRecordedAt.toLocalDateAtZone(userClock.zone)
      val isNewRegistration = today == patientRegisteredOnDate

      return RecentPatientItem(
          uuid = recentPatient.uuid,
          name = recentPatient.fullName,
          age = recentPatient.ageDetails.estimateAge(userClock),
          gender = recentPatient.gender,
          updatedAt = recentPatient.updatedAt,
          dateFormatter = dateFormatter,
          clock = userClock,
          isNewRegistration = isNewRegistration
      )
    }
  }

  override fun layoutResId(): Int = R.layout.recent_patient_item_view

  override fun render(holder: BindingViewHolder, subject: Subject<UiEvent>) {
    val context = holder.itemView.context
    val binding = holder.binding as RecentPatientItemViewBinding

    holder.itemView.setOnClickListener {
      subject.onNext(RecentPatientItemClicked(patientUuid = uuid))
    }

    binding.newRegistrationTextView.visibleOrGone(isNewRegistration)
    binding.patientNameTextView.text = context.resources.getString(R.string.patients_recentpatients_nameage, name, age.toString())
    binding.genderImageView.setImageResource(gender.displayIconRes)
    binding.lastSeenTextView.text = dateFormatter.format(updatedAt.toLocalDateAtZone(clock.zone))
  }
}

class RecentPatientItemDiffCallback : DiffUtil.ItemCallback<RecentPatientItem>() {
  override fun areItemsTheSame(oldItem: RecentPatientItem, newItem: RecentPatientItem): Boolean {
    return oldItem.uuid == newItem.uuid
  }

  override fun areContentsTheSame(oldItem: RecentPatientItem, newItem: RecentPatientItem): Boolean {
    return oldItem == newItem
  }
}
