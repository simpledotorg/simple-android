package org.simple.clinic.storage.migrations

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.string

class Migration36AndroidTest : BaseDatabaseMigrationTest(
    fromVersion = 35,
    toVersion = 36
) {

  @Test
  fun migrate_prescription_from_35_to_36() {
    val tableName = "PrescribedDrug"
    val uuid = "drug-uuid"
    before.execSQL("""
      INSERT INTO $tableName VALUES(
      '$uuid',
      'drug',
      'dosage',
      'rxNormCode',
      0,
      1,
      'patientUuid',
      'facilityUuid',
      'PENDING',
      'created-at',
      'updatedAt',
      'null',
      'recorded-at')
    """)

    after.query("SELECT * FROM $tableName").use {
      assertThat(it.count).isEqualTo(1)
      assertThat(it.getColumnIndex("recordedAt")).isEqualTo(-1)
      it.moveToNext()
      assertThat(it.columnCount).isEqualTo(12)
      assertThat(it.string("uuid")).isEqualTo(uuid)
    }
  }

  @Test
  fun migrate_appointment_from_35_to_36() {
    val tableName = "Appointment"
    val appointmentUuid = "uuid"

    before.execSQL("""
      INSERT INTO $tableName VALUES(
      '$appointmentUuid',
      'patientUuid',
      'facility-uuid',
      'scheduled-date',
      'status',
      'cancel-reason',
      'remind-on',
      '1',
      'manual',
      'PENDING',
      'created-at',
      'updated-at',
      'null',
      'recordedAt')
    """)

    after.query("SELECT * FROM $tableName").use {
      assertThat(it.count).isEqualTo(1)
      assertThat(it.getColumnIndex("recordedAt")).isEqualTo(-1)
      it.moveToNext()
      assertThat(it.columnCount).isEqualTo(13)
      assertThat(it.string("uuid")).isEqualTo(appointmentUuid)
    }
  }

  @Test
  fun migrate_medical_histories_from_35_to_36() {
    val tableName = "MedicalHistory"
    val mhUuid = "uuid"

    before.execSQL("""
      INSERT INTO $tableName VALUES(
      '$mhUuid',
      'patientUuid',
      0,
      1,
      0,
      1,
      0,
      1,
      'PENDING',
      'created-at',
      'updated-at',
      'null',
      'recorded-at')
    """)

    after.query("SELECT * FROM $tableName").use {
      assertThat(it.count).isEqualTo(1)
      assertThat(it.getColumnIndex("recordedAt")).isEqualTo(-1)
      it.moveToNext()
      assertThat(it.columnCount).isEqualTo(12)
      assertThat(it.string("uuid")).isEqualTo(mhUuid)
    }
  }

  @Test
  fun migrate_patient_address_from_35_to_36() {
    val addressTableName = "PatientAddress"
    val patientTable = "Patient"
    val addressUuid = "uuid"

    before.execSQL("""
      INSERT INTO $addressTableName VALUES(
        '$addressUuid',
        'colony or village',
        'district',
        'state',
        'country',
        'created-at',
        'updated-at',
        NULL,
        'recorded-at')
    """)

    before.execSQL("""
      INSERT INTO $patientTable VALUES(
        'patientUuid',
        '$addressUuid',
        'patient-name',
        'name',
        'MALE',
        NULL,
        'ACTIVE',
        'created-at',
        'updated-at',
        'null',
        'IN_FLIGHT',
        'recorded-at',
        25,
        'age-updated-at',
        'dob');
    """)

    after.query("SELECT * FROM $addressTableName").use {
      assertThat(it.count).isEqualTo(1)
      assertThat(it.getColumnIndex("recordedAt")).isEqualTo(-1)
      it.moveToNext()
      assertThat(it.columnCount).isEqualTo(8)
      assertThat(it.string("uuid")).isEqualTo(addressUuid)
    }

    after.query("SELECT * FROM $patientTable").use {
      assertThat(it.count).isEqualTo(1)
      it.moveToNext()
      assertThat(it.string("addressUuid")).isEqualTo(addressUuid)
      assertThat(it.string("uuid")).isEqualTo("patientUuid")
    }
  }

  @Test
  fun migrate_phone_number_from_35_to_36() {
    val phoneNumberTableName = "PatientPhoneNumber"
    val patientTable = "Patient"
    val patientUuid = "patientUuid"
    val phoneUuid = "phoneUuid"

    before.execSQL("""
      INSERT INTO "PatientAddress" VALUES(
        'addressUuid',
        'colony or village',
        'district',
        'state',
        'country',
        'created-at',
        'updated-at',
        NULL,
        'recorded-at')
    """)

    before.execSQL("""
      INSERT INTO $patientTable VALUES(
        '$patientUuid',
        'addressUuid',
        'patient-name',
        'name',
        'MALE',
        NULL,
        'ACTIVE',
        'created-at',
        'updated-at',
        'null',
        'IN_FLIGHT',
        'recorded-at',
        25,
        'age-updated-at',
        'dob');
    """)

    before.execSQL("""
      INSERT INTO $phoneNumberTableName VALUES(
      '$phoneUuid',
      '$patientUuid',
      '981615191',
      'mobile',
      0,
      'created-at',
      'updated-at',
      'null',
      'recorded-at')
    """)

    after.query("SELECT * FROM $phoneNumberTableName").use {
      assertThat(it.count).isEqualTo(1)
      assertThat(it.getColumnIndex("recordedAt")).isEqualTo(-1)
      it.moveToNext()
      assertThat(it.columnCount).isEqualTo(8)
      assertThat(it.string("patientUuid")).isEqualTo(patientUuid)
      assertThat(it.string("uuid")).isEqualTo(phoneUuid)
    }
  }

  @Test
  fun migrate_business_ids_from_35_to_36() {
    val patientTable = "Patient"
    val businessIdTable = "BusinessId"
    val patientUuid = "patientUuid"
    val businessUuid = "businessUuid"

    before.execSQL("""
      INSERT INTO "PatientAddress" VALUES(
        'addressUuid',
        'colony or village',
        'district',
        'state',
        'country',
        'created-at',
        'updated-at',
        NULL,
        'recorded-at')
    """)

    before.execSQL("""
      INSERT INTO $patientTable VALUES(
        '$patientUuid',
        'addressUuid',
        'patient-name',
        'name',
        'MALE',
        NULL,
        'ACTIVE',
        'created-at',
        'updated-at',
        'null',
        'IN_FLIGHT',
        'recorded-at',
        25,
        'age-updated-at',
        'dob')
    """)

    before.execSQL("""
      INSERT INTO $businessIdTable VALUES (
      '$businessUuid',
      'patientUuid',
      'meta-version',
      'data',
      'created-at',
      'updated-at',
      'null',
      'recorded-at',
      'simple-uuid',
      'bp-passport'
      )
    """)

    after.query("SELECT * FROM $businessIdTable").use {
      assertThat(it.count).isEqualTo(1)
      assertThat(it.getColumnIndex("recordedAt")).isEqualTo(-1)
      it.moveToNext()
      assertThat(it.columnCount).isEqualTo(9)
      assertThat(it.string("patientUuid")).isEqualTo(patientUuid)
      assertThat(it.string("uuid")).isEqualTo(businessUuid)
    }
  }
}
