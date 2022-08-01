package org.simple.clinic.home.overdue.search

import android.content.Context
import android.graphics.Rect
import android.view.TouchDelegate
import android.view.View
import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
import androidx.recyclerview.widget.DiffUtil
import io.reactivex.subjects.Subject
import org.simple.clinic.R
import org.simple.clinic.databinding.ListItemOverduePatientBinding
import org.simple.clinic.databinding.ListItemSearchOverdueSelectAllButtonBinding
import org.simple.clinic.home.overdue.OverdueAppointment
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

sealed class OverdueAppointmentSearchListItem : PagingItemAdapter.Item<UiEvent> {

  companion object {

    fun from(
        appointments: PagingData<OverdueAppointment>,
        selectedOverdueAppointments: Set<UUID>,
        clock: UserClock,
        isOverdueSelectAndDownloadEnabled: Boolean,
    ): PagingData<OverdueAppointmentSearchListItem> {
      val overdueAppointments = appointments
          .map { overdueAppointment ->
            val isAppointmentSelected = selectedOverdueAppointments.contains(overdueAppointment.appointment.uuid)
            overdueAppointmentSearchListItem(
                overdueAppointment = overdueAppointment,
                clock = clock,
                isOverdueSelectAndDownloadEnabled = isOverdueSelectAndDownloadEnabled,
                isSelected = isAppointmentSelected
            )
          }
          .insertSeparators { before, after ->
            if (before == null && after != null && isOverdueSelectAndDownloadEnabled) {
              SelectAllOverdueAppointmentButton
            } else {
              null
            }
          }

      return overdueAppointments
    }

    private fun overdueAppointmentSearchListItem(
        overdueAppointment: OverdueAppointment,
        clock: UserClock,
        isOverdueSelectAndDownloadEnabled: Boolean,
        isSelected: Boolean
    ): OverdueAppointmentSearchListItem {
      return OverdueAppointmentRow(
          appointmentUuid = overdueAppointment.appointment.uuid,
          patientUuid = overdueAppointment.appointment.patientUuid,
          name = overdueAppointment.fullName,
          gender = overdueAppointment.gender,
          age = overdueAppointment.ageDetails.estimateAge(clock),
          phoneNumber = overdueAppointment.phoneNumber?.number,
          overdueDays = daysBetweenNowAndDate(overdueAppointment.appointment.scheduledDate, clock),
          isAtHighRisk = overdueAppointment.isAtHighRisk,
          villageName = overdueAppointment.patientAddress.colonyOrVillage,
          isOverdueSelectAndDownloadEnabled = isOverdueSelectAndDownloadEnabled,
          isSelected = isSelected
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
      val villageName: String?,
      val isOverdueSelectAndDownloadEnabled: Boolean,
      val isSelected: Boolean
  ) : OverdueAppointmentSearchListItem() {

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

      binding.checkbox.setOnClickListener {
        eventSubject.onNext(OverdueAppointmentCheckBoxClicked(appointmentUuid))
      }
    }

    private fun bindUi(holder: BindingViewHolder) {
      val binding = holder.binding as ListItemOverduePatientBinding
      val context = holder.itemView.context

      renderPatientName(context, binding)

      binding.patientGenderIcon.setImageResource(gender.displayIconRes)

      renderVillageName(binding)

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

      binding.checkbox.visibleOrGone(isOverdueSelectAndDownloadEnabled)
      binding.patientGenderIcon.visibleOrGone(!isOverdueSelectAndDownloadEnabled)

      binding.checkbox.isChecked = isSelected
    }

    private fun renderPatientName(
        context: Context,
        binding: ListItemOverduePatientBinding
    ) {
      val nameAndAge = context.getString(R.string.overdue_list_item_name_age, name, age.toString())
      binding.patientNameTextView.text = nameAndAge
    }

    private fun renderVillageName(
        binding: ListItemOverduePatientBinding
    ) {
      if (!villageName.isNullOrBlank()) {
        binding.villageTextView.visibility = View.VISIBLE
        binding.villageTextView.text = villageName
      } else {
        binding.villageTextView.visibility = View.GONE
      }
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

  object SelectAllOverdueAppointmentButton : OverdueAppointmentSearchListItem() {

    override fun layoutResId(): Int = R.layout.list_item_search_overdue_select_all_button

    override fun render(holder: BindingViewHolder, subject: Subject<UiEvent>) {
      val binding = holder.binding as ListItemSearchOverdueSelectAllButtonBinding
      binding.root.setOnClickListener {
        subject.onNext(SelectAllButtonClicked)
      }
    }
  }

  class DiffCallback : DiffUtil.ItemCallback<OverdueAppointmentSearchListItem>() {
    override fun areItemsTheSame(
        oldItem: OverdueAppointmentSearchListItem,
        newItem: OverdueAppointmentSearchListItem
    ): Boolean {
      return when {
        oldItem is OverdueAppointmentRow && newItem is OverdueAppointmentRow -> oldItem.patientUuid == newItem.patientUuid
        oldItem is SelectAllOverdueAppointmentButton && newItem is SelectAllOverdueAppointmentButton -> true
        else -> false
      }
    }

    override fun areContentsTheSame(
        oldItem: OverdueAppointmentSearchListItem,
        newItem: OverdueAppointmentSearchListItem
    ): Boolean {
      return oldItem == newItem
    }
  }
}
