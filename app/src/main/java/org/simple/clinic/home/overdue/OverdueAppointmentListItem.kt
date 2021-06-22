package org.simple.clinic.home.overdue

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.paging.PagingData
import androidx.paging.map
import androidx.recyclerview.widget.DiffUtil
import io.reactivex.subjects.Subject
import org.simple.clinic.R
import org.simple.clinic.databinding.ItemOverdueListPatientOldBinding
import org.simple.clinic.databinding.ListItemOverduePatientBinding
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.patient.DateOfBirth
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.displayIconRes
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.toLocalDateAtZone
import org.simple.clinic.widgets.PagingItemAdapter
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.recyclerview.BindingViewHolder
import org.simple.clinic.widgets.setCompoundDrawableStart
import org.simple.clinic.widgets.visibleOrGone
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.UUID

sealed class OverdueAppointmentListItem : PagingItemAdapter.Item<UiEvent> {

  companion object {

    fun from(
        appointments: PagingData<OverdueAppointment>,
        clock: UserClock,
        dateFormatter: DateTimeFormatter,
        isDiabetesManagementEnabled: Boolean,
        overdueListChangesEnabled: Boolean
    ): PagingData<OverdueAppointmentListItem> {
      return appointments
          .map { overdueAppointment -> from(overdueAppointment, clock, dateFormatter, isDiabetesManagementEnabled, overdueListChangesEnabled) }
    }

    private fun from(
        overdueAppointment: OverdueAppointment,
        clock: UserClock,
        dateFormatter: DateTimeFormatter,
        isDiabetesManagementEnabled: Boolean,
        overdueListChangesEnabled: Boolean
    ): OverdueAppointmentListItem {
      return if (overdueListChangesEnabled) {
        OverdueAppointmentRow(
            appointmentUuid = overdueAppointment.appointment.uuid,
            patientUuid = overdueAppointment.appointment.patientUuid,
            name = overdueAppointment.fullName,
            gender = overdueAppointment.gender,
            age = DateOfBirth.fromOverdueAppointment(overdueAppointment, clock).estimateAge(clock),
            phoneNumber = overdueAppointment.phoneNumber?.number,
            overdueDays = daysBetweenNowAndDate(overdueAppointment.appointment.scheduledDate, clock),
            isAtHighRisk = overdueAppointment.isAtHighRisk,
            villageName = overdueAppointment.patientAddress.colonyOrVillage
        )
      } else {
        OverdueAppointmentRow_Old(
            appointmentUuid = overdueAppointment.appointment.uuid,
            patientUuid = overdueAppointment.appointment.patientUuid,
            name = overdueAppointment.fullName,
            gender = overdueAppointment.gender,
            age = DateOfBirth.fromOverdueAppointment(overdueAppointment, clock).estimateAge(clock),
            phoneNumber = overdueAppointment.phoneNumber?.number,
            overdueDays = daysBetweenNowAndDate(overdueAppointment.appointment.scheduledDate, clock),
            isAtHighRisk = overdueAppointment.isAtHighRisk,
            lastSeenDate = dateFormatter.format(overdueAppointment.patientLastSeen.toLocalDateAtZone(clock.zone)),
            diagnosedWithDiabetes = overdueAppointment.diagnosedWithDiabetes,
            diagnosedWithHypertension = overdueAppointment.diagnosedWithHypertension,
            showDiagnosisLabel = isDiabetesManagementEnabled,
            patientAddress = overdueAppointment.patientAddress,
            isAppointmentAtAssignedFacility = overdueAppointment.isAppointmentAtAssignedFacility,
            appointmentFacilityName = overdueAppointment.appointmentFacilityName
        )
      }
    }

    private fun daysBetweenNowAndDate(
        date: LocalDate,
        clock: UserClock
    ): Int {
      return ChronoUnit.DAYS.between(date, LocalDate.now(clock)).toInt()
    }
  }

  @Suppress("ClassName")
  data class OverdueAppointmentRow_Old(
      val appointmentUuid: UUID,
      val patientUuid: UUID,
      val name: String,
      val gender: Gender,
      val age: Int,
      val phoneNumber: String? = null,
      val overdueDays: Int,
      val isAtHighRisk: Boolean,
      val lastSeenDate: String,
      val diagnosedWithDiabetes: Answer?,
      val diagnosedWithHypertension: Answer?,
      val showDiagnosisLabel: Boolean,
      val patientAddress: OverduePatientAddress,
      val isAppointmentAtAssignedFacility: Boolean,
      val appointmentFacilityName: String?
  ) : OverdueAppointmentListItem() {

    override fun layoutResId(): Int = R.layout.item_overdue_list_patient_old

    override fun render(holder: BindingViewHolder, subject: Subject<UiEvent>) {
      val binding = holder.binding as ItemOverdueListPatientOldBinding
      setupEvents(binding, subject)
      bindUi(holder)
    }

    private fun setupEvents(
        binding: ItemOverdueListPatientOldBinding,
        eventSubject: Subject<UiEvent>
    ) {
      binding.callButton.setOnClickListener {
        eventSubject.onNext(CallPatientClicked(patientUuid))
      }

      binding.patientNameTextView.setOnClickListener {
        eventSubject.onNext(PatientNameClicked(patientUuid))
      }
    }

    @SuppressLint("SetTextI18n")
    private fun bindUi(holder: BindingViewHolder) {
      val binding = holder.binding as ItemOverdueListPatientOldBinding
      val context = holder.itemView.context

      binding.patientNameTextView.text = context.getString(R.string.overdue_list_item_name_age, name, age.toString())
      binding.patientNameTextView.setCompoundDrawableStart(gender.displayIconRes)
      binding.patientAddressTextView.text = when {
        !patientAddress.streetAddress.isNullOrBlank() && !patientAddress.colonyOrVillage.isNullOrBlank() -> {
          "${patientAddress.streetAddress}, ${patientAddress.colonyOrVillage}"
        }
        !patientAddress.streetAddress.isNullOrBlank() -> patientAddress.streetAddress
        !patientAddress.colonyOrVillage.isNullOrBlank() -> patientAddress.colonyOrVillage
        else -> "${patientAddress.district}, ${patientAddress.state}"
      }

      binding.patientLastSeenTextView.text = lastSeenDate

      binding.callButton.visibility = if (phoneNumber == null) View.GONE else View.VISIBLE

      binding.isAtHighRiskTextView.visibility = if (isAtHighRisk) View.VISIBLE else View.GONE

      binding.overdueDaysTextView.text = context.resources.getQuantityString(
          R.plurals.overdue_list_item_appointment_overdue_days,
          overdueDays,
          "$overdueDays"
      )

      binding.diagnosisLabelContainer.visibleOrGone(showDiagnosisLabel)
      binding.diagnosisTextView.text = diagnosisText(context)

      val showTransferLabel = isAppointmentAtAssignedFacility.not()
      binding.patientTransferredContainer.visibleOrGone(showTransferLabel)
      binding.patientTransferredTextView.text = appointmentFacilityName
    }

    private fun diagnosisText(context: Context): CharSequence {
      return listOf(
          diagnosedWithDiabetes to R.string.overdue_list_item_diagnosis_diabetes,
          diagnosedWithHypertension to R.string.overdue_list_item_diagnosis_hypertension
      )
          .filter { (answer, _) -> answer is Answer.Yes }
          .map { (_, diagnosisTitle) -> diagnosisTitle }
          .ifEmpty { listOf(R.string.overdue_list_item_diagnosis_none) }
          .joinToString { context.getString(it) }
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

      binding.patientNameTextView.setOnClickListener {
        eventSubject.onNext(PatientNameClicked(patientUuid))
      }
    }

    private fun bindUi(holder: BindingViewHolder) {
      val binding = holder.binding as ListItemOverduePatientBinding
      val context = holder.itemView.context

      binding.patientNameTextView.text = context.getString(R.string.overdue_list_item_name_age, name, age.toString())
      binding.patientNameTextView.setCompoundDrawableStart(gender.displayIconRes)
      binding.villageTextView.text = villageName.orEmpty()
      binding.villageTextView.visibleOrGone(isVisible = !villageName.isNullOrBlank())

      val callButtonDrawable = if (phoneNumber.isNullOrBlank()) {
        R.drawable.ic_overdue_no_phone_number
      } else {
        R.drawable.ic_overdue_call
      }
      binding.callButton.setImageResource(callButtonDrawable)

      binding.isAtHighRiskTextView.visibility = if (isAtHighRisk) View.VISIBLE else View.GONE

      binding.overdueDaysTextView.text = context.resources.getQuantityString(
          R.plurals.overdue_list_item_appointment_overdue_days,
          overdueDays,
          "$overdueDays"
      )
    }
  }

  class DiffCallback : DiffUtil.ItemCallback<OverdueAppointmentListItem>() {
    override fun areItemsTheSame(
        oldItem: OverdueAppointmentListItem,
        newItem: OverdueAppointmentListItem
    ): Boolean {
      return when {
        oldItem is OverdueAppointmentRow && newItem is OverdueAppointmentRow -> oldItem.patientUuid == newItem.patientUuid
        oldItem is OverdueAppointmentRow_Old && newItem is OverdueAppointmentRow_Old -> oldItem.patientUuid == newItem.patientUuid
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
