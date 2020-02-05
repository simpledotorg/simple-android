package org.simple.clinic.summary

import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.facility.FacilityConfig
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BangladeshNationalId
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.summary.OpenIntention.ViewExistingPatient
import java.util.UUID

class PatientSummaryUpdateTest {

  private val defaultModel = PatientSummaryModel.from(ViewExistingPatient, UUID.fromString("93a131b0-890e-41a3-88ec-b35b48efc6c5"))
  private val updateSpec = UpdateSpec(PatientSummaryUpdate())

  @Test
  fun `when the current facility is loaded, update the UI`() {
    val facility = PatientMocker.facility(
        uuid = UUID.fromString("abe86f8e-1828-48fe-afb5-d697b3ce36bb"),
        facilityConfig = FacilityConfig(diabetesManagementEnabled = true)
    )

    updateSpec
        .given(defaultModel)
        .whenEvent(CurrentFacilityLoaded(facility))
        .then(
            assertThatNext(
                hasModel(defaultModel.currentFacilityLoaded(facility)),
                hasNoEffects()
            )
        )
  }

  @Test
  fun `when the patient summary profile is loaded, then update the UI`() {
    val patientUuid = UUID.fromString("c995c56d-9515-4991-9c63-dfcca06d93b0")
    val patient = PatientMocker.patient(patientUuid)
    val patientAddress = PatientMocker.address(patient.addressUuid)
    val phoneNumber = PatientMocker.phoneNumber(patientUuid = patientUuid)
    val bpPassport = PatientMocker.businessId(patientUuid = patientUuid, identifier = Identifier("526 780", BpPassport))
    val bangladeshNationalId = PatientMocker.businessId(patientUuid = patientUuid, identifier = Identifier("123456789012", BangladeshNationalId))

    val patientSummaryProfile = PatientSummaryProfile(
        patient = patient,
        address = patientAddress,
        phoneNumber = phoneNumber,
        bpPassport = bpPassport,
        bangladeshNationalId = bangladeshNationalId
    )

    updateSpec
        .given(defaultModel)
        .whenEvent(PatientSummaryProfileLoaded(patientSummaryProfile))
        .then(assertThatNext(
            hasModel(defaultModel.patientSummaryProfileLoaded(patientSummaryProfile)),
            hasNoEffects()
        ))
  }
}
