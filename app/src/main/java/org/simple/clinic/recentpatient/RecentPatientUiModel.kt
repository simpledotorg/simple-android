package org.simple.clinic.recentpatient

import org.simple.clinic.R
import org.simple.clinic.databinding.RecentPatientItemViewBinding
import org.simple.clinic.medicalhistory.Answer.Suspected
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.RecentPatient
import org.simple.clinic.patient.displayIconRes
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.toLocalDateAtZone
import org.simple.clinic.widgets.recyclerview.BindingViewHolder
import org.simple.clinic.widgets.visibleOrGone
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * Shared UI model used by both paged and non-paged adapters.
 */
data class RecentPatientUiModel(
    val uuid: UUID,
    val name: String,
    val age: Int,
    val gender: Gender,
    val updatedAt: Instant,
    val dateFormatter: DateTimeFormatter,
    val clock: UserClock,
    val isNewRegistration: Boolean,
    val isEligibleForReassignment: Boolean = false,
    val isSuspectedForHypertension: Boolean = false,
    val isSuspectedForDiabetes: Boolean = false
) {
  companion object {
    fun from(
        recentPatient: RecentPatient,
        today: LocalDate,
        userClock: UserClock,
        dateFormatter: DateTimeFormatter,
        isEligibleForReassignment: Boolean = false
    ): RecentPatientUiModel {
      val patientRegisteredOnDate = recentPatient.patientRecordedAt.toLocalDateAtZone(userClock.zone)
      val isNewRegistration = today == patientRegisteredOnDate

      return RecentPatientUiModel(
          uuid = recentPatient.uuid,
          name = recentPatient.fullName,
          age = recentPatient.ageDetails.estimateAge(userClock),
          gender = recentPatient.gender,
          updatedAt = recentPatient.updatedAt,
          dateFormatter = dateFormatter,
          clock = userClock,
          isNewRegistration = isNewRegistration,
          isEligibleForReassignment = isEligibleForReassignment,
          isSuspectedForHypertension = recentPatient.diagnosedWithHypertension == Suspected,
          isSuspectedForDiabetes = recentPatient.diagnosedWithDiabetes == Suspected
      )
    }
  }
}

object RecentPatientViewBinder {

  fun bind(
      holder: BindingViewHolder,
      model: RecentPatientUiModel,
      onClick: (UUID) -> Unit
  ) {
    val context = holder.itemView.context
    val binding = holder.binding as RecentPatientItemViewBinding

    holder.itemView.setOnClickListener {
      onClick(model.uuid)
    }

    val statusText: String? = when {
      model.isSuspectedForHypertension && model.isSuspectedForDiabetes ->
        context.getString(R.string.recent_patients_itemview_suspected_for_hypertension_and_diabetes)

      model.isSuspectedForHypertension ->
        context.getString(R.string.recent_patients_itemview_suspected_for_hypertension)

      model.isSuspectedForDiabetes ->
        context.getString(R.string.recent_patients_itemview_suspected_for_diabetes)

      model.isNewRegistration ->
        context.getString(R.string.recent_patients_itemview_new_registration)

      else -> null
    }

    binding.patientStatusTextView.visibleOrGone(statusText != null)
    binding.patientStatusTextView.text = statusText
    binding.facilityReassignmentView.root.visibleOrGone(model.isEligibleForReassignment)

    binding.patientNameTextView.text =
        context.resources.getString(R.string.patients_recentpatients_nameage, model.name, model.age.toString())

    binding.genderImageView.setImageResource(model.gender.displayIconRes)
    binding.lastSeenTextView.text = model.dateFormatter.format(model.updatedAt.toLocalDateAtZone(model.clock.zone))
  }
}
