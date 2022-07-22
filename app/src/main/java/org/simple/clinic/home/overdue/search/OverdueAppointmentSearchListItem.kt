package org.simple.clinic.home.overdue.search

import android.content.Context
import android.graphics.Rect
import android.view.TouchDelegate
import android.view.View
import androidx.core.text.backgroundColor
import androidx.core.text.buildSpannedString
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
import org.simple.clinic.util.resolveColor
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
        searchQuery: String?,
        isOverdueSelectAndDownloadEnabled: Boolean,
    ): PagingData<OverdueAppointmentSearchListItem> {
      val overdueAppointments = appointments
          .map { overdueAppointment ->
            val isAppointmentSelected = selectedOverdueAppointments.contains(overdueAppointment.appointment.uuid)
            overdueAppointmentSearchListItem(
                overdueAppointment = overdueAppointment,
                clock = clock,
                searchQuery = searchQuery,
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
        searchQuery: String?,
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
          searchQuery = searchQuery,
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
      val searchQuery: String?,
      val isOverdueSelectAndDownloadEnabled: Boolean,
      val isSelected: Boolean
  ) : OverdueAppointmentSearchListItem() {

    override fun layoutResId(): Int = R.layout.list_item_overdue_patient

    override fun render(holder: BindingViewHolder, subject: Subject<UiEvent>) {
      val binding = holder.binding as ListItemOverduePatientBinding
      setupEvents(binding, subject)
      bindUi(holder, searchQuery)
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

    private fun bindUi(holder: BindingViewHolder, searchQuery: String?) {
      val binding = holder.binding as ListItemOverduePatientBinding
      val context = holder.itemView.context

      renderPatientName(context, binding, searchQuery)

      binding.patientGenderIcon.setImageResource(gender.displayIconRes)

      renderVillageName(context, binding, searchQuery)

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
        binding: ListItemOverduePatientBinding,
        searchQuery: String?
    ) {
      val nameAndAge = context.getString(R.string.overdue_list_item_name_age, name, age.toString())
      binding.patientNameTextView.text = when (val it = getHighlightedPatientAttribute(searchQuery, nameAndAge)) {
        is PatientAttribute.Highlighted -> highlight(
            context = context,
            text = it.value,
            startIndex = it.highlightStart,
            endIndex = it.highlightEnd
        )
        is PatientAttribute.Plain -> it.value
      }
    }

    private fun renderVillageName(
        context: Context,
        binding: ListItemOverduePatientBinding,
        searchQuery: String?
    ) {
      if (!villageName.isNullOrBlank()) {
        binding.villageTextView.visibility = View.VISIBLE

        binding.villageTextView.text = getHighlightedVillageName(context, searchQuery, villageName)
      } else {
        binding.villageTextView.visibility = View.GONE
      }
    }

    private fun getHighlightedVillageName(
        context: Context,
        searchQuery: String?,
        villageName: String
    ) = when (val it = getHighlightedPatientAttribute(searchQuery, villageName)) {
      is PatientAttribute.Highlighted -> highlight(
          context = context,
          text = it.value,
          startIndex = it.highlightStart,
          endIndex = it.highlightEnd
      )
      is PatientAttribute.Plain -> it.value
    }

    private fun highlight(
        context: Context,
        text: String,
        startIndex: Int,
        endIndex: Int
    ) = buildSpannedString {
      append(text.substring(0, startIndex))
      backgroundColor(context.resolveColor(colorRes = R.color.search_query_highlight)) {
        append(text.substring(startIndex, endIndex))
      }
      append(text.substring(endIndex, text.length))
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

  fun getHighlightedPatientAttribute(searchQuery: String?, patientAttribute: String): PatientAttribute {
    val canHighlight = !searchQuery.isNullOrBlank() && patientAttribute.contains(searchQuery, ignoreCase = true)
    return if (canHighlight) {
      val startingIndexForHighlight = patientAttribute.indexOf(searchQuery!!, ignoreCase = true)
      PatientAttribute.Highlighted(
          value = patientAttribute,
          highlightStart = startingIndexForHighlight,
          highlightEnd = startingIndexForHighlight + searchQuery.length
      )
    } else {
      PatientAttribute.Plain(value = patientAttribute)
    }
  }

  sealed class PatientAttribute(open val value: String) {
    data class Highlighted(
        override val value: String,
        val highlightStart: Int,
        val highlightEnd: Int
    ) : PatientAttribute(value)

    data class Plain(override val value: String) : PatientAttribute(value)
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
