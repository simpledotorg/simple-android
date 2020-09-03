package org.simple.clinic.home.overdue

import android.annotation.SuppressLint
import android.content.Context
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.recyclerview.widget.DiffUtil
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.item_overdue_list_patient.*
import org.simple.clinic.R
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.patient.DateOfBirth
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.displayIconRes
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.toLocalDateAtZone
import org.simple.clinic.widgets.PagingItemAdapter
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.recyclerview.ViewHolderX
import org.simple.clinic.widgets.setCompoundDrawableStart
import org.simple.clinic.widgets.visibleOrGone
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.UUID

data class OverdueAppointmentRow(
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
) : PagingItemAdapter.Item<UiEvent> {

  companion object {

    fun from(
        appointments: List<OverdueAppointment>,
        clock: UserClock,
        dateFormatter: DateTimeFormatter,
        isDiabetesManagementEnabled: Boolean
    ): List<OverdueAppointmentRow> {
      return appointments.map { overdueAppointment -> from(overdueAppointment, clock, dateFormatter, isDiabetesManagementEnabled) }
    }

    private fun from(
        overdueAppointment: OverdueAppointment,
        clock: UserClock,
        dateFormatter: DateTimeFormatter,
        isDiabetesManagementEnabled: Boolean
    ): OverdueAppointmentRow {
      return OverdueAppointmentRow(
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

    private fun daysBetweenNowAndDate(
        date: LocalDate,
        clock: UserClock
    ): Int {
      return ChronoUnit.DAYS.between(date, LocalDate.now(clock)).toInt()
    }
  }

  override fun layoutResId(): Int = R.layout.item_overdue_list_patient

  override fun render(holder: ViewHolderX, subject: Subject<UiEvent>) {
    setupEvents(holder, subject)
    bindUi(holder)
  }

  private fun setupEvents(
      holder: ViewHolderX,
      eventSubject: Subject<UiEvent>
  ) {
    holder.callButton.setOnClickListener {
      eventSubject.onNext(CallPatientClicked(patientUuid))
    }

    holder.patientNameTextView.setOnClickListener {
      eventSubject.onNext(PatientNameClicked(patientUuid))
    }
  }

  @SuppressLint("SetTextI18n")
  private fun bindUi(holder: ViewHolderX) {
    val containerView = holder.containerView
    val context = containerView.context

    holder.patientNameTextView.text = context.getString(R.string.overdue_list_item_name_age, name, age.toString())
    holder.patientNameTextView.setCompoundDrawableStart(gender.displayIconRes)
    holder.patientAddressTextView.text = when {
      !patientAddress.streetAddress.isNullOrBlank() && !patientAddress.colonyOrVillage.isNullOrBlank() -> {
        "${patientAddress.streetAddress}, ${patientAddress.colonyOrVillage}"
      }
      !patientAddress.streetAddress.isNullOrBlank() -> patientAddress.streetAddress
      !patientAddress.colonyOrVillage.isNullOrBlank() -> patientAddress.colonyOrVillage
      else -> "${patientAddress.district}, ${patientAddress.state}"
    }

    holder.patientLastSeenTextView.text = lastSeenDate

    holder.callButton.visibility = if (phoneNumber == null) GONE else VISIBLE

    holder.isAtHighRiskTextView.visibility = if (isAtHighRisk) VISIBLE else GONE

    holder.overdueDaysTextView.text = context.resources.getQuantityString(
        R.plurals.overdue_list_item_appointment_overdue_days,
        overdueDays,
        "$overdueDays"
    )

    holder.diagnosisLabelContainer.visibleOrGone(showDiagnosisLabel)
    holder.diagnosisTextView.text = diagnosisText(context)

    val showTransferLabel = isAppointmentAtAssignedFacility.not()
    holder.patientTransferredContainer.visibleOrGone(showTransferLabel)
    holder.patientTransferredTextView.text = appointmentFacilityName
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

  class DiffCallback : DiffUtil.ItemCallback<OverdueAppointmentRow>() {
    override fun areItemsTheSame(oldItem: OverdueAppointmentRow, newItem: OverdueAppointmentRow): Boolean {
      return oldItem.patientUuid == newItem.patientUuid
    }

    override fun areContentsTheSame(oldItem: OverdueAppointmentRow, newItem: OverdueAppointmentRow): Boolean {
      return oldItem == newItem
    }
  }
}
