package org.simple.clinic.bp.entry

import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.PatientUuid
import org.simple.clinic.user.User
import org.threeten.bp.LocalDate
import java.util.UUID

sealed class SaveBpData {

  data class ReadyToCreate(
      val date: LocalDate,
      val systolic: Int,
      val diastolic: Int,
      val patientUuid: PatientUuid,
      val loggedInUser: User,
      val currentFacility: Facility
  ) : SaveBpData()

  data class ReadyToUpdate(
      val date: LocalDate,
      val systolic: Int,
      val diastolic: Int,
      val bpUuid: UUID,
      val loggedInUser: User,
      val currentFacility: Facility
  ) : SaveBpData()

  object NeedsCorrection : SaveBpData()
}
