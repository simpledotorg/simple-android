package org.simple.clinic.storage.migrations

import org.junit.Test
import org.simple.clinic.assertIndexDoesNotExist
import org.simple.clinic.assertIndexExists

class Migration99AndroidTest : BaseDatabaseMigrationTest(
    fromVersion = 98,
    toVersion = 99
) {

  @Test
  fun migrating_should_create_the_appointment_patient_id_index_table() {
    before.assertIndexDoesNotExist("index_Appointment_patientUuid")

    after.assertIndexExists("index_Appointment_patientUuid")
  }

  @Test
  fun migrating_should_create_the_blood_sugar_patient_id_index_table() {
    before.assertIndexDoesNotExist("index_BloodSugarMeasurements_patientUuid")

    after.assertIndexExists("index_BloodSugarMeasurements_patientUuid")
  }

  @Test
  fun migrating_should_create_the_medical_history_patient_id_index_table() {
    before.assertIndexDoesNotExist("index_MedicalHistory_patientUuid")

    after.assertIndexExists("index_MedicalHistory_patientUuid")
  }
}
