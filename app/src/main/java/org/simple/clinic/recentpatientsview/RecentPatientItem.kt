package org.simple.clinic.recentpatientsview

import androidx.recyclerview.widget.DiffUtil
import io.reactivex.subjects.Subject
import org.simple.clinic.R
import org.simple.clinic.databinding.SeeAllItemViewBinding
import org.simple.clinic.patient.Answer
import org.simple.clinic.patient.RecentPatient
import org.simple.clinic.recentpatient.RecentPatientUiModel
import org.simple.clinic.recentpatient.RecentPatientViewBinder
import org.simple.clinic.util.UserClock
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.recyclerview.BindingViewHolder
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID


sealed class RecentPatientItemType : ItemAdapter.Item<UiEvent> {

  companion object {

    fun create(
        recentPatients: List<RecentPatient>,
        userClock: UserClock,
        dateFormatter: DateTimeFormatter,
        isPatientReassignmentFeatureEnabled: Boolean,
    ): List<RecentPatientItemType> {
      val today = LocalDate.now(userClock)

      return recentPatients.map {
        recentPatientItem(
            it,
            today,
            userClock,
            dateFormatter,
            isPatientReassignmentFeatureEnabled
        )
      }
    }

    private fun recentPatientItem(
        recentPatient: RecentPatient,
        today: LocalDate,
        userClock: UserClock,
        dateFormatter: DateTimeFormatter,
        isPatientReassignmentFeatureEnabled: Boolean,
    ): RecentPatientItem {
      val model = RecentPatientUiModel.from(
          recentPatient = recentPatient,
          today = today,
          userClock = userClock,
          dateFormatter = dateFormatter,
          isEligibleForReassignment = isPatientReassignmentFeatureEnabled &&
              recentPatient.eligibleForReassignment == Answer.Yes
      )

      return RecentPatientItem(model)
    }
  }
}

data class RecentPatientItem(
    private val model: RecentPatientUiModel
) : RecentPatientItemType() {

  val uuid: UUID get() = model.uuid

  override fun layoutResId(): Int = R.layout.recent_patient_item_view

  override fun render(holder: BindingViewHolder, subject: Subject<UiEvent>) {
    RecentPatientViewBinder.bind(holder, model) {
      subject.onNext(RecentPatientItemClicked(patientUuid = uuid))
    }
  }
}

data object SeeAllItem : RecentPatientItemType() {
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
