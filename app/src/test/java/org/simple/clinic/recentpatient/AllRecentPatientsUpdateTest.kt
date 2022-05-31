package org.simple.clinic.recentpatient

import androidx.paging.PagingData
import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.sharedTestCode.TestData
import org.simple.clinic.patient.PatientAgeDetails
import java.time.LocalDate
import java.util.UUID

class AllRecentPatientsUpdateTest {

  private val defaultModel = AllRecentPatientsModel
  private val updateSpec = UpdateSpec(AllRecentPatientsUpdate())

  @Test
  fun `when recent patients are loaded, then show recent patients`() {
    val recentPatients = PagingData.from(listOf(
        TestData.recentPatient(
            uuid = UUID.fromString("68bb2fba-512f-4419-bdda-9b661e90b805"),
            patientAgeDetails = PatientAgeDetails(
                ageValue = null,
                ageUpdatedAt = null,
                dateOfBirth = LocalDate.parse("1976-01-01")
            )
        ),
        TestData.recentPatient(
            uuid = UUID.fromString("1fb172bc-b705-4ffb-a5bd-425811c430ab"),
            patientAgeDetails = PatientAgeDetails(
                ageValue = null,
                ageUpdatedAt = null,
                dateOfBirth = LocalDate.parse("1976-01-01")
            )
        )
    ))

    updateSpec
        .given(defaultModel)
        .whenEvent(RecentPatientsLoaded(recentPatients))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowRecentPatients(recentPatients))
        ))
  }

  @Test
  fun `when recent patient is clicked, then open patient summary`() {
    val patientUuid = UUID.fromString("61891a99-b9f1-48bc-99d2-0495f16c6de5")

    updateSpec
        .given(defaultModel)
        .whenEvent(RecentPatientItemClicked(patientUuid = patientUuid))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenPatientSummary(patientUuid))
        ))
  }
}
