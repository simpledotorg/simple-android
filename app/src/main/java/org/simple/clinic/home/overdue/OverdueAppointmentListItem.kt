package org.simple.clinic.home.overdue

import android.graphics.Rect
import android.view.TouchDelegate
import android.view.View
import androidx.annotation.StringRes
import androidx.recyclerview.widget.DiffUtil
import io.reactivex.subjects.Subject
import org.simple.clinic.R
import org.simple.clinic.databinding.ListItemOverdueListSectionHeaderBinding
import org.simple.clinic.databinding.ListItemOverduePatientBinding
import org.simple.clinic.databinding.ListItemOverduePendingListFooterBinding
import org.simple.clinic.databinding.ListItemSearchOverduePatientButtonBinding
import org.simple.clinic.home.overdue.OverdueAppointmentSectionTitle.AGREED_TO_VISIT
import org.simple.clinic.home.overdue.OverdueAppointmentSectionTitle.MORE_THAN_A_YEAR_OVERDUE
import org.simple.clinic.home.overdue.OverdueAppointmentSectionTitle.PENDING_TO_CALL
import org.simple.clinic.home.overdue.OverdueAppointmentSectionTitle.REMIND_TO_CALL
import org.simple.clinic.home.overdue.OverdueAppointmentSectionTitle.REMOVED_FROM_OVERDUE
import org.simple.clinic.home.overdue.PendingListState.SEE_ALL
import org.simple.clinic.home.overdue.PendingListState.SEE_LESS
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.displayIconRes
import org.simple.clinic.util.UserClock
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.dp
import org.simple.clinic.widgets.executeOnNextMeasure
import org.simple.clinic.widgets.recyclerview.BindingViewHolder
import org.simple.clinic.widgets.setCompoundDrawableEnd
import org.simple.clinic.widgets.visibleOrGone
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID

sealed class OverdueAppointmentListItem : ItemAdapter.Item<UiEvent> {

  companion object {

    fun from(
        overdueAppointmentSections: OverdueAppointmentSections,
        clock: UserClock,
        pendingListDefaultStateSize: Int,
        overdueListSectionStates: OverdueListSectionStates,
        isOverdueInstantSearchEnabled: Boolean,
        isOverdueSelectAndDownloadEnabled: Boolean,
        selectedOverdueAppointments: Set<UUID>
    ): List<OverdueAppointmentListItem> {
      val searchOverduePatientsButtonListItem = searchOverduePatientItem(isOverdueInstantSearchEnabled)

      val pendingToCallListItem = pendingToCallItem(
          overdueAppointmentSections,
          clock,
          pendingListDefaultStateSize,
          overdueListSectionStates,
          isOverdueSelectAndDownloadEnabled,
          selectedOverdueAppointments
      )
      val agreedToVisitListItem = agreedToVisitItem(
          overdueAppointmentSections,
          clock,
          overdueListSectionStates,
          isOverdueSelectAndDownloadEnabled,
          selectedOverdueAppointments
      )
      val remindToCallListItem = remindToCallItem(
          overdueAppointmentSections,
          clock,
          overdueListSectionStates,
          isOverdueSelectAndDownloadEnabled,
          selectedOverdueAppointments
      )
      val removedFromOverdueListItem = removedFromOverdueItem(
          overdueAppointmentSections,
          clock,
          overdueListSectionStates,
          isOverdueSelectAndDownloadEnabled,
          selectedOverdueAppointments
      )
      val moreThanAnOneYearOverdueListItem = moreThanAnOneYearOverdueItem(
          overdueAppointmentSections,
          clock,
          overdueListSectionStates,
          isOverdueSelectAndDownloadEnabled,
          selectedOverdueAppointments
      )
      val dividerListItem = listOf(Divider)

      return searchOverduePatientsButtonListItem +
          pendingToCallListItem + dividerListItem +
          agreedToVisitListItem + dividerListItem +
          remindToCallListItem + dividerListItem +
          removedFromOverdueListItem + dividerListItem +
          moreThanAnOneYearOverdueListItem
    }

    private fun searchOverduePatientItem(isOverdueInstantSearchEnabled: Boolean) =
        if (isOverdueInstantSearchEnabled) listOf(SearchOverduePatientsButtonItem) else emptyList()

    private fun moreThanAnOneYearOverdueItem(
        overdueAppointmentSections: OverdueAppointmentSections,
        clock: UserClock,
        overdueListSectionStates: OverdueListSectionStates,
        isOverdueSelectAndDownloadEnabled: Boolean,
        selectedOverdueAppointments: Set<UUID>
    ): List<OverdueAppointmentListItem> {
      val moreThanAnOneYearOverdueHeader = listOf(
          OverdueSectionHeader(R.string.overdue_no_visit_in_one_year_call_header,
              overdueAppointmentSections.moreThanAnYearOverdueAppointments.size,
              overdueListSectionStates.isMoreThanAnOneYearOverdueHeader,
              MORE_THAN_A_YEAR_OVERDUE
          ))

      val moreThanAnOneYearOverdueListItems = expandedOverdueAppointmentList(
          overdueListSectionStates.isMoreThanAnOneYearOverdueHeader,
          overdueAppointmentSections.moreThanAnYearOverdueAppointments,
          clock,
          isOverdueSelectAndDownloadEnabled,
          selectedOverdueAppointments)

      return moreThanAnOneYearOverdueHeader + moreThanAnOneYearOverdueListItems
    }

    private fun removedFromOverdueItem(
        overdueAppointmentSections: OverdueAppointmentSections,
        clock: UserClock,
        overdueListSectionStates: OverdueListSectionStates,
        isOverdueSelectAndDownloadEnabled: Boolean,
        selectedOverdueAppointments: Set<UUID>
    ): List<OverdueAppointmentListItem> {
      val removedFromOverdueListHeader = listOf(
          OverdueSectionHeader(R.string.overdue_removed_from_list_call_header,
              overdueAppointmentSections.removedFromOverdueAppointments.size,
              overdueListSectionStates.isRemovedFromOverdueListHeaderExpanded,
              REMOVED_FROM_OVERDUE
          ))

      val removedFromOverdueListItems = expandedOverdueAppointmentList(
          overdueListSectionStates.isRemovedFromOverdueListHeaderExpanded,
          overdueAppointmentSections.removedFromOverdueAppointments,
          clock,
          isOverdueSelectAndDownloadEnabled,
          selectedOverdueAppointments)

      return removedFromOverdueListHeader + removedFromOverdueListItems
    }

    private fun remindToCallItem(
        overdueAppointmentSections: OverdueAppointmentSections,
        clock: UserClock,
        overdueListSectionStates: OverdueListSectionStates,
        isOverdueSelectAndDownloadEnabled: Boolean,
        selectedOverdueAppointments: Set<UUID>
    ): List<OverdueAppointmentListItem> {
      val remindToCallHeader = listOf(
          OverdueSectionHeader(R.string.overdue_remind_to_call_header,
              overdueAppointmentSections.remindToCallLaterAppointments.size,
              overdueListSectionStates.isRemindToCallLaterHeaderExpanded,
              REMIND_TO_CALL
          ))

      val remindToCallListItems = expandedOverdueAppointmentList(
          overdueListSectionStates.isRemindToCallLaterHeaderExpanded,
          overdueAppointmentSections.remindToCallLaterAppointments,
          clock,
          isOverdueSelectAndDownloadEnabled,
          selectedOverdueAppointments)

      return remindToCallHeader + remindToCallListItems
    }

    private fun agreedToVisitItem(
        overdueAppointmentSections: OverdueAppointmentSections,
        clock: UserClock,
        overdueListSectionStates: OverdueListSectionStates,
        isOverdueSelectAndDownloadEnabled: Boolean,
        selectedOverdueAppointments: Set<UUID>
    ): List<OverdueAppointmentListItem> {
      val agreedToVisitHeader = listOf(
          OverdueSectionHeader(R.string.overdue_agreed_to_visit_call_header,
              overdueAppointmentSections.agreedToVisitAppointments.size,
              overdueListSectionStates.isAgreedToVisitHeaderExpanded,
              AGREED_TO_VISIT
          ))

      val agreedToVisitListItems = expandedOverdueAppointmentList(
          overdueListSectionStates.isAgreedToVisitHeaderExpanded,
          overdueAppointmentSections.agreedToVisitAppointments,
          clock,
          isOverdueSelectAndDownloadEnabled,
          selectedOverdueAppointments)

      return agreedToVisitHeader + agreedToVisitListItems
    }

    private fun pendingToCallItem(
        overdueAppointmentSections: OverdueAppointmentSections,
        clock: UserClock,
        pendingListDefaultStateSize: Int,
        overdueListSectionStates: OverdueListSectionStates,
        isOverdueSelectAndDownloadEnabled: Boolean,
        selectedOverdueAppointments: Set<UUID>
    ): List<OverdueAppointmentListItem> {
      val pendingAppointments = overdueAppointmentSections.pendingAppointments
      val pendingToCallHeader = listOf(
          OverdueSectionHeader(R.string.overdue_pending_to_call_header,
              overdueAppointmentSections.pendingAppointments.size,
              overdueListSectionStates.isPendingHeaderExpanded,
              PENDING_TO_CALL
          ))
      val pendingAppointmentsContent = generatePendingAppointmentsContent(
          overdueAppointmentSections,
          clock,
          pendingListDefaultStateSize,
          overdueListSectionStates,
          isOverdueSelectAndDownloadEnabled,
          selectedOverdueAppointments
      )

      val showPendingListFooter = pendingAppointments.size > pendingListDefaultStateSize && overdueListSectionStates.isPendingHeaderExpanded
      val pendingListFooterItem = if (showPendingListFooter) listOf(PendingListFooter(overdueListSectionStates.pendingListState)) else emptyList()

      return pendingToCallHeader + pendingAppointmentsContent + pendingListFooterItem
    }

    private fun generatePendingAppointmentsContent(
        overdueAppointmentSections: OverdueAppointmentSections,
        clock: UserClock,
        pendingListDefaultStateSize: Int,
        overdueListSectionStates: OverdueListSectionStates,
        isOverdueSelectAndDownloadEnabled: Boolean,
        selectedOverdueAppointments: Set<UUID>
    ): List<OverdueAppointmentListItem> {
      val pendingAppointmentsList = when (overdueListSectionStates.pendingListState) {
        SEE_LESS -> overdueAppointmentSections.pendingAppointments.take(pendingListDefaultStateSize)
        SEE_ALL -> overdueAppointmentSections.pendingAppointments
      }

      val expandedPendingAppointmentList = expandedOverdueAppointmentList(
          overdueListSectionStates.isPendingHeaderExpanded,
          pendingAppointmentsList,
          clock,
          isOverdueSelectAndDownloadEnabled,
          selectedOverdueAppointments)

      return if (pendingAppointmentsList.isEmpty() && overdueListSectionStates.isPendingHeaderExpanded) {
        listOf(NoPendingPatients)
      } else {
        expandedPendingAppointmentList
      }
    }

    private fun expandedOverdueAppointmentList(
        isListExpanded: Boolean,
        overdueAppointment: List<OverdueAppointment>,
        clock: UserClock,
        isOverdueSelectAndDownloadEnabled: Boolean,
        selectedOverdueAppointments: Set<UUID>
    ): List<OverdueAppointmentListItem> {
      return if (isListExpanded) {
        overdueAppointment.map {
          val isAppointmentSelected = selectedOverdueAppointments.contains(it.appointment.uuid)
          from(it, clock, isOverdueSelectAndDownloadEnabled, isAppointmentSelected)
        }
      } else {
        emptyList()
      }
    }

    private fun from(
        overdueAppointment: OverdueAppointment,
        clock: UserClock,
        isOverdueSelectAndDownloadEnabled: Boolean,
        isAppointmentSelected: Boolean
    ): OverdueAppointmentListItem {
      return OverdueAppointmentRow(
          appointmentUuid = overdueAppointment.appointment.uuid,
          patientUuid = overdueAppointment.appointment.patientUuid,
          name = overdueAppointment.fullName,
          gender = overdueAppointment.gender,
          age = overdueAppointment.ageDetails.estimateAge(clock),
          phoneNumber = overdueAppointment.phoneNumber?.number,
          overdueDays = daysBetweenNowAndDate(overdueAppointment.appointment.scheduledDate, clock),
          villageName = overdueAppointment.patientAddress.colonyOrVillage,
          isOverdueSelectAndDownloadEnabled = isOverdueSelectAndDownloadEnabled,
          isAppointmentSelected = isAppointmentSelected,
          isEligibleForReassignment = overdueAppointment.isEligibleForReassignment,
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
      val villageName: String?,
      val isOverdueSelectAndDownloadEnabled: Boolean,
      val isAppointmentSelected: Boolean,
      val isEligibleForReassignment: Boolean,
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

      binding.checkbox.setOnClickListener {
        eventSubject.onNext(OverdueAppointmentCheckBoxClicked(appointmentUuid))
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

      binding.overdueDaysTextView.text = context.resources.getQuantityString(
          R.plurals.overdue_list_item_appointment_overdue_days,
          overdueDays,
          "$overdueDays"
      )

      binding.checkbox.isChecked = isAppointmentSelected

      binding.checkbox.visibleOrGone(isOverdueSelectAndDownloadEnabled)
      binding.patientGenderIcon.visibleOrGone(!isOverdueSelectAndDownloadEnabled)
      binding.facilityReassignmentView.root.visibleOrGone(isEligibleForReassignment)
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

  data class OverdueSectionHeader(
      @StringRes val headerText: Int,
      val count: Int,
      val isOverdueSectionHeaderExpanded: Boolean,
      val overdueAppointmentSectionTitle: OverdueAppointmentSectionTitle
  ) : OverdueAppointmentListItem() {
    override fun layoutResId(): Int = R.layout.list_item_overdue_list_section_header

    override fun render(holder: BindingViewHolder, subject: Subject<UiEvent>) {
      val binding = holder.binding as ListItemOverdueListSectionHeaderBinding

      binding.overdueSectionHeaderTextView.setText(headerText)
      binding.overdueSectionHeaderIcon.text = count.toString()
      binding.root.setOnClickListener {
        subject.onNext(ChevronClicked(overdueAppointmentSectionTitle))
      }

      if (isOverdueSectionHeaderExpanded) {
        binding.overdueSectionHeaderIcon.setCompoundDrawableEnd(R.drawable.ic_chevron_down_24)
      } else {
        binding.overdueSectionHeaderIcon.setCompoundDrawableEnd(R.drawable.ic_chevron_right_24px)
      }
    }
  }

  data class PendingListFooter(val pendingListState: PendingListState) : OverdueAppointmentListItem() {
    override fun layoutResId(): Int = R.layout.list_item_overdue_pending_list_footer

    override fun render(holder: BindingViewHolder, subject: Subject<UiEvent>) {
      val binding = holder.binding as ListItemOverduePendingListFooterBinding

      binding.overduePendingSeeAllOrLessButton.setOnClickListener {
        subject.onNext(PendingListFooterClicked)
      }

      binding.overduePendingSeeAllOrLessButton.setText(pendingListFooterStringRes())
    }

    private fun pendingListFooterStringRes() = when (pendingListState) {
      SEE_ALL -> R.string.overdue_pending_list_button_see_less
      SEE_LESS -> R.string.overdue_pending_list_button_see_all
    }
  }

  object NoPendingPatients : OverdueAppointmentListItem() {

    override fun layoutResId(): Int = R.layout.list_item_no_pending_patients

    override fun render(holder: BindingViewHolder, subject: Subject<UiEvent>) {
      /* no-op */
    }
  }

  object SearchOverduePatientsButtonItem : OverdueAppointmentListItem() {

    override fun layoutResId(): Int = R.layout.list_item_search_overdue_patient_button

    override fun render(holder: BindingViewHolder, subject: Subject<UiEvent>) {
      val binding = holder.binding as ListItemSearchOverduePatientButtonBinding
      binding.root.setOnClickListener {
        subject.onNext(OverdueSearchButtonClicked)
      }
    }
  }

  object Divider : OverdueAppointmentListItem() {

    override fun layoutResId(): Int = R.layout.list_item_divider

    override fun render(holder: BindingViewHolder, subject: Subject<UiEvent>) {
      /* no-op */
    }
  }

  class DiffCallback : DiffUtil.ItemCallback<OverdueAppointmentListItem>() {

    override fun areItemsTheSame(
        oldItem: OverdueAppointmentListItem,
        newItem: OverdueAppointmentListItem
    ): Boolean {
      return when {
        oldItem is OverdueAppointmentRow && newItem is OverdueAppointmentRow -> oldItem.patientUuid == newItem.patientUuid
        oldItem is OverdueSectionHeader && newItem is OverdueSectionHeader -> oldItem.headerText == newItem.headerText
        oldItem is PendingListFooter && newItem is PendingListFooter -> oldItem.pendingListState == newItem.pendingListState
        oldItem is NoPendingPatients && newItem is NoPendingPatients -> true
        oldItem is Divider && newItem is Divider -> true
        oldItem is SearchOverduePatientsButtonItem && newItem is SearchOverduePatientsButtonItem -> true
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
