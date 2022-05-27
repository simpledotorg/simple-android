package org.simple.clinic.home.overdue

import android.graphics.Rect
import android.view.TouchDelegate
import android.view.View
import androidx.paging.PagingData
import androidx.paging.map
import androidx.recyclerview.widget.DiffUtil
import io.reactivex.subjects.Subject
import org.simple.clinic.R
import org.simple.clinic.databinding.ListItemOverduePatientBinding
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.displayIconRes
import org.simple.clinic.util.UserClock
import org.simple.clinic.widgets.PagingItemAdapter
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.dp
import org.simple.clinic.widgets.executeOnNextMeasure
import org.simple.clinic.widgets.recyclerview.BindingViewHolder
import org.simple.clinic.widgets.visibleOrGone
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID

sealed class OverdueAppointmentListItem : PagingItemAdapter.Item<UiEvent> {

  companion object {

    fun from(
        appointments: PagingData<OverdueAppointment_Old>,
        clock: UserClock
    ): PagingData<OverdueAppointmentListItem> {
      return appointments
          .map { overdueAppointment -> from(overdueAppointment, clock) }
    }

    private fun from(
        overdueAppointmentOld: OverdueAppointment_Old,
        clock: UserClock
    ): OverdueAppointmentListItem {
      return OverdueAppointmentRow(
          appointmentUuid = overdueAppointmentOld.appointment.uuid,
          patientUuid = overdueAppointmentOld.appointment.patientUuid,
          name = overdueAppointmentOld.fullName,
          gender = overdueAppointmentOld.gender,
          age = overdueAppointmentOld.ageDetails.estimateAge(clock),
          phoneNumber = overdueAppointmentOld.phoneNumber?.number,
          overdueDays = daysBetweenNowAndDate(overdueAppointmentOld.appointment.scheduledDate, clock),
          isAtHighRisk = overdueAppointmentOld.isAtHighRisk,
          villageName = overdueAppointmentOld.patientAddress.colonyOrVillage
      )
    }

    private fun daysBetweenNowAndDate(
        date: LocalDate,
        clock: UserClock
    ): Int {
      return ChronoUnit.DAYS.between(date, LocalDate.now(clock)).toInt()
    }
  }

  data class OverdueAppointmentRow(
      val appointmentUuid: UUID,
      val patientUuid: UUID,
      val name: String,
      val gender: Gender,
      val age: Int,
      val phoneNumber: String? = null,
      val overdueDays: Int,
      val isAtHighRisk: Boolean,
      val villageName: String?
  ) : OverdueAppointmentListItem() {

    override fun layoutResId(): Int = R.layout.list_item_overdue_patient

    override fun render(holder: BindingViewHolder, subject: Subject<UiEvent>) {
      val binding = holder.binding as ListItemOverduePatientBinding
      setupEvents(binding, subject)
      bindUi(holder)
    }

    private fun setupEvents(
        binding: ListItemOverduePatientBinding,
        eventSubject: Subject<UiEvent>
    ) {
      binding.callButton.setOnClickListener {
        eventSubject.onNext(CallPatientClicked(patientUuid))
      }

      binding.overdueCardView.setOnClickListener {
        eventSubject.onNext(OverduePatientClicked(patientUuid))
      }
    }

    private fun bindUi(holder: BindingViewHolder) {
      val binding = holder.binding as ListItemOverduePatientBinding
      val context = holder.itemView.context

      binding.patientNameTextView.text = context.getString(R.string.overdue_list_item_name_age, name, age.toString())
      binding.patientGenderIcon.setImageResource(gender.displayIconRes)
      binding.villageTextView.text = villageName.orEmpty()
      binding.villageTextView.visibleOrGone(isVisible = !villageName.isNullOrBlank())

      val callButtonDrawable = if (phoneNumber.isNullOrBlank()) {
        R.drawable.ic_overdue_no_phone_number
      } else {
        R.drawable.ic_overdue_call
      }
      binding.callButton.setImageResource(callButtonDrawable)
      increaseCallButtonTapArea(callButton = binding.callButton)

      binding.isAtHighRiskTextView.visibility = if (isAtHighRisk) View.VISIBLE else View.GONE

      binding.overdueDaysTextView.text = context.resources.getQuantityString(
          R.plurals.overdue_list_item_appointment_overdue_days,
          overdueDays,
          "$overdueDays"
      )
    }

    private fun increaseCallButtonTapArea(callButton: View) {
      val parent = callButton.parent as View

      parent.executeOnNextMeasure {
        val touchableArea = Rect()
        callButton.getHitRect(touchableArea)

        val buttonHeight = callButton.height
        val parentHeight = parent.height

        val verticalSpace = (parentHeight - buttonHeight) / 2
        val horizontalSpace = 24.dp

        with(touchableArea) {
          left -= horizontalSpace
          top -= verticalSpace
          right += horizontalSpace
          bottom += verticalSpace
        }

        parent.touchDelegate = TouchDelegate(touchableArea, callButton)
      }
    }
  }

  class DiffCallback : DiffUtil.ItemCallback<OverdueAppointmentListItem>() {
    override fun areItemsTheSame(
        oldItem: OverdueAppointmentListItem,
        newItem: OverdueAppointmentListItem
    ): Boolean {
      return when {
        oldItem is OverdueAppointmentRow && newItem is OverdueAppointmentRow -> oldItem.patientUuid == newItem.patientUuid
        else -> false
      }
    }

    override fun areContentsTheSame(
        oldItem: OverdueAppointmentListItem,
        newItem: OverdueAppointmentListItem
    ): Boolean {
      return oldItem == newItem
    }
  }
}
