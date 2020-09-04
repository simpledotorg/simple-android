package org.simple.clinic.storage.migrations

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.insert
import java.time.Instant

class Migration45AndroidTest : BaseDatabaseMigrationTest(
    fromVersion = 44,
    toVersion = 45
) {

  @Test
  fun migrating_to_45_should_maintain_the_foreign_key_references_to_the_patient_table() {
    // given
    val instant = Instant.parse("2018-01-01T00:00:00.000Z")

    val patientAddressUuid = "05f9c798-1701-4379-b14a-b1b18d937a33"
    val patientUuid = "0b18af95-e73f-43ea-9838-34abe9d7858e"
    val patientPhoneNumberUuid = "d039c3cd-7c8c-49e5-8f58-edc482728a46"
    val businessIdUuid = "07e6bb07-46ad-46c6-bb46-ec939ba96ccd"

    before.insert("PatientAddress", mapOf(
        "uuid" to patientAddressUuid,
        "colonyOrVillage" to "Colony",
        "district" to "District",
        "state" to "State",
        "country" to "Country",
        "createdAt" to instant.toString(),
        "updatedAt" to instant.toString(),
        "deletedAt" to null
    ))

    before.insert(
        "Patient",
        mapOf(
            "uuid" to patientUuid,
            "addressUuid" to patientAddressUuid,
            "fullName" to "Anish Acharya",
            "searchableName" to "anishacharya",
            "gender" to "male",
            "dateOfBirth" to "1990-01-01",
            "age_value" to 40,
            "age_updatedAt" to instant.toString(),
            "age_computedDateOfBirth" to "1990-01-02",
            "status" to "active",
            "createdAt" to instant.plusSeconds(1).toString(),
            "updatedAt" to instant.plusSeconds(2).toString(),
            "recordedAt" to instant.minusSeconds(1).toString(),
            "syncStatus" to "PENDING"
        )
    )

    before.insert(
        "PatientPhoneNumber",
        mapOf(
            "uuid" to patientPhoneNumberUuid,
            "patientUuid" to patientUuid,
            "number" to "123456",
            "phoneType" to "mobile",
            "active" to true,
            "createdAt" to instant.toString(),
            "updatedAt" to instant.toString(),
            "deletedAt" to null
        )
    )

    before.insert(
        "BusinessId",
        mapOf(
            "uuid" to businessIdUuid,
            "patientUuid" to patientUuid,
            "identifier" to "fdc0706b-3cbf-4644-9cf3-c5b794514dd6",
            "identifierType" to "simple_bp_passport",
            "metaVersion" to "org.simple.bppassport.meta.v1",
            "meta" to "",
            "createdAt" to instant.toString(),
            "updatedAt" to instant.toString(),
            "deletedAt" to null
        )
    )

    // when
    after.execSQL("""DELETE FROM "PatientAddress" WHERE "uuid" = '$patientAddressUuid'""")

    // then
    after
        .query("""SELECT "uuid" FROM "Patient" WHERE "uuid" == '$patientUuid'""")
        .use { cursor ->
          assertThat(cursor.count).isEqualTo(0)
        }

    after
        .query("""SELECT "uuid" FROM "PatientPhoneNumber" WHERE "uuid" == '$patientPhoneNumberUuid'""")
        .use { cursor ->
          assertThat(cursor.count).isEqualTo(0)
        }

    after
        .query("""SELECT "uuid" FROM "BusinessId" WHERE "uuid" == '$businessIdUuid'""")
        .use { cursor ->
          assertThat(cursor.count).isEqualTo(0)
        }
  }
}
