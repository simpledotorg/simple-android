package org.simple.clinic.recentpatientsview

import androidx.recyclerview.widget.DiffUtil
import io.reactivex.subjects.Subject
import org.simple.clinic.R
import org.simple.clinic.databinding.RecentPatientItemViewBinding
import org.simple.clinic.databinding.SeeAllItemViewBinding
import org.simple.clinic.patient.DateOfBirth
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.RecentPatient
import org.simple.clinic.patient.displayIconRes
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.toLocalDateAtZone
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.recyclerview.BindingViewHolder
import org.simple.clinic.widgets.visibleOrGone
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

sealed class RecentPatientItemType : ItemAdapter.Item<UiEvent> {

  companion object {

    fun create(
        recentPatients: List<RecentPatient>,
        userClock: UserClock,
        dateFormatter: DateTimeFormatter
    ): List<RecentPatientItemType> {
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
          age = age(recentPatient, userClock),
          gender = recentPatient.gender,
          updatedAt = recentPatient.updatedAt,
          dateFormatter = dateFormatter,
          clock = userClock,
          isNewRegistration = isNewRegistration
      )
    }

    private fun age(
        recentPatient: RecentPatient,
        userClock: UserClock
    ): Int {
      return DateOfBirth.fromRecentPatient(recentPatient, userClock).estimateAge(userClock)
    }
  }
}

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

object SeeAllItem : RecentPatientItemType() {
  override fun layoutResId(): Int = R.layout.see_all_item_view

  override fun render(holder: BindingViewHolder, subject: Subject<UiEvent>) {
    val binding = holder.binding as SeeAllItemViewBinding

    binding.seeAllButton.setOnClickListener {
      subject.onNext(SeeAllItemClicked)
    }
  }
}

class RecentPatientItemTypeDiffCallback : DiffUtil.ItemCallback<RecentPatientItemType>() {
  override fun areItemsTheSame(
      oldItem: RecentPatientItemType,
      newItem: RecentPatientItemType
  ): Boolean {
    return when {
      oldItem is SeeAllItem && newItem is SeeAllItem -> true
      oldItem is RecentPatientItem && newItem is RecentPatientItem -> oldItem.uuid == newItem.uuid
      else -> false
    }
  }

  override fun areContentsTheSame(
      oldItem: RecentPatientItemType,
      newItem: RecentPatientItemType
  ): Boolean {
    return oldItem == newItem
  }
}
