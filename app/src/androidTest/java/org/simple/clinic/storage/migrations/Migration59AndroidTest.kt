package org.simple.clinic.storage.migrations

import org.junit.Before
import org.junit.Test
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.patient.SyncStatus.DONE
import org.simple.clinic.patient.SyncStatus.PENDING
import java.util.UUID
import javax.inject.Inject

class Migration59AndroidTest : BaseDatabaseMigrationTest(58, 59) {

  @Inject
  lateinit var testData: TestData

  @Before
  override fun setUp() {
    super.setUp()
    TestClinicApp.appComponent().inject(this)
  }

  @Suppress("IllegalIdentifier")
  @Test
  fun migrating_to_59_should_set_sync_status_for_all_patients_as_pending_who_have_non_blank_street_address_or_zone() {
    val patientWithBlankZoneAndBlankStreet = patientWithZoneAndStreet(
        uuid = UUID.fromString("9c7174f3-3bf0-46de-b4a4-15e30dfc631c"),
        zone = "",
        street = ""
    )

    val patientWithNonBlankZoneAndBlankStreet = patientWithZoneAndStreet(
        uuid = UUID.fromString("69be9ba7-2034-47ca-994e-fb7f1c6e6d7a"),
        zone = "Zone"
    )

    val patientWithBlankZoneAndNonBlankStreet = patientWithZoneAndStreet(
        uuid = UUID.fromString("dc25eea8-4d14-420e-921c-c6b0fdf23252"),
        street = "Street"
    )

    val patientWithNonBlankZoneAndNonBlankStreet = patientWithZoneAndStreet(
        uuid = UUID.fromString("1d99e47d-7221-45ca-bc6e-ece5e352f8d8"),
        zone = "Zone",
        street = "Street"
    )

    before.savePatientProfile(patientWithBlankZoneAndBlankStreet)
    before.savePatientProfile(patientWithNonBlankZoneAndBlankStreet)
    before.savePatientProfile(patientWithBlankZoneAndNonBlankStreet)
    before.savePatientProfile(patientWithNonBlankZoneAndNonBlankStreet)

    after.assertPatient(patientWithBlankZoneAndBlankStreet.withSyncStatus(DONE).patient)
    after.assertPatient(patientWithNonBlankZoneAndBlankStreet.withSyncStatus(PENDING).patient)
    after.assertPatient(patientWithBlankZoneAndNonBlankStreet.withSyncStatus(PENDING).patient)
    after.assertPatient(patientWithNonBlankZoneAndNonBlankStreet.withSyncStatus(PENDING).patient)
  }

  private fun patientWithZoneAndStreet(
      uuid: UUID,
      zone: String = "",
      street: String = ""
  ): PatientProfile {
    val profile = testData.patientProfile(
        patientUuid = uuid,
        syncStatus = DONE,
        generateBusinessId = false
    )
    return profile.copy(address = profile.address.copy(zone = zone, streetAddress = street))
  }
}
