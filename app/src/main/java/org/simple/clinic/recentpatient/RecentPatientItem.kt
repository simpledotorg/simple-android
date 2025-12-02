package org.simple.clinic.recentpatient

import androidx.paging.PagingData
import androidx.paging.map
import androidx.recyclerview.widget.DiffUtil
import io.reactivex.subjects.Subject
import org.simple.clinic.patient.RecentPatient
import org.simple.clinic.util.UserClock
import org.simple.clinic.widgets.PagingItemAdapter
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.recyclerview.BindingViewHolder
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

data class RecentPatientItem(
    private val model: RecentPatientUiModel
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
      val model = RecentPatientUiModel.from(
          recentPatient = recentPatient,
          today = today,
          userClock = userClock,
          dateFormatter = dateFormatter,
          isEligibleForReassignment = false
      )

      return RecentPatientItem(model)
    }
  }

  val uuid: UUID get() = model.uuid

  override fun layoutResId(): Int = org.simple.clinic.R.layout.recent_patient_item_view

  override fun render(holder: BindingViewHolder, subject: Subject<UiEvent>) {
    RecentPatientViewBinder.bind(holder, model) {
      subject.onNext(RecentPatientItemClicked(patientUuid = uuid))
    }
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
