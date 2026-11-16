package org.simple.clinic.summary

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BangladeshNationalId
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BangladeshSharedHealthRecordId
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import java.util.UUID

class PatientSummaryProfileTest {

  private val patientUuid = UUID.fromString("2b1f3a0e-6d41-4c3e-9b5c-7f2a8e1d4c90")
  private val patient = TestData.patient(uuid = patientUuid)
  private val address = TestData.patientAddress(uuid = patient.addressUuid)

  private val bpPassport = TestData.businessId(
      patientUuid = patientUuid,
      identifier = Identifier("e116bf4c-53e0-46a3-b95d-295d7178d66e", BpPassport)
  )
  private val nationalId = TestData.businessId(
      patientUuid = patientUuid,
      identifier = Identifier("1234567890123", BangladeshNationalId)
  )
  private val shrId = TestData.businessId(
      patientUuid = patientUuid,
      identifier = Identifier("98146050888", BangladeshSharedHealthRecordId)
  )

  private val profile = PatientSummaryProfile(
      patient = patient,
      address = address,
      phoneNumber = null,
      bpPassport = bpPassport,
      alternativeId = nationalId,
      shrId = shrId,
      facility = null
  )

  @Test
  fun `the profile should have the SHR ID, bp passport and alternative ID`() {
    assertThat(profile.hasIdentifier(shrId.identifier)).isTrue()
    assertThat(profile.hasIdentifier(bpPassport.identifier)).isTrue()
    assertThat(profile.hasIdentifier(nationalId.identifier)).isTrue()
  }

  @Test
  fun `the profile should not have an SHR ID that belongs to someone else`() {
    val otherShrId = Identifier("91111111111", BangladeshSharedHealthRecordId)

    assertThat(profile.hasIdentifier(otherShrId)).isFalse()
  }

  @Test
  fun `a profile without an SHR ID should not have an SHR ID`() {
    val profileWithoutShrId = profile.copy(shrId = null)

    assertThat(profileWithoutShrId.hasIdentifier(shrId.identifier)).isFalse()
  }
}
