package org.simple.clinic.storage.migrations

import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.assertValues
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.patient.businessid.BusinessId
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.string
import java.util.UUID
import javax.inject.Inject

class Migration57AndroidTest : BaseDatabaseMigrationTest(56, 57) {

  @Inject
  lateinit var testData: TestData

  @Before
  override fun setUp() {
    super.setUp()
    TestClinicApp.appComponent().inject(this)
  }

  @Suppress("IllegalIdentifier")
  @Test
  fun migrating_to_57_should_mark_all_patients_with_blank_identifiers_as_sync_pending() {
    // given
    val `uuid of synced patient without bangladesh ID` = UUID.fromString("1c36ac74-a6ae-4ed2-afb1-42175e3625f9")
    val `synced patient without bangladesh ID` = testData
        .patientProfile(
            patientUuid = `uuid of synced patient without bangladesh ID`,
            generatePhoneNumber = false,
            businessId = testData.businessId(
                uuid = UUID.fromString("3fff1513-6906-40ff-bbf7-5ab901c5eaa4"),
                patientUuid = `uuid of synced patient without bangladesh ID`,
                meta = ""
            )
        )
        .withSyncStatus(SyncStatus.DONE)

    val `uuid of sync pending patient without bangladesh ID` = UUID.fromString("0713baa6-0db1-4d38-a502-03dc4ee97771")
    val `sync pending patient without bangladesh ID` = testData
        .patientProfile(
            patientUuid = `uuid of sync pending patient without bangladesh ID`,
            generatePhoneNumber = false,
            businessId = testData.businessId(
                uuid = UUID.fromString("4b6daae6-2fe1-4a40-bd94-621cf3da9822"),
                patientUuid = `uuid of sync pending patient without bangladesh ID`,
                meta = ""
            )
        )
        .withSyncStatus(SyncStatus.PENDING)


    val `uuid of first sync failed patient with blank bangladesh ID` = UUID.fromString("40427b8e-e6ba-424d-a91c-c282fcc5061e")
    val `first sync failed patient with blank bangladesh ID` = testData
        .patientProfile(
            patientUuid = `uuid of first sync failed patient with blank bangladesh ID`,
            generatePhoneNumber = false,
            businessId = testData.businessId(
                uuid = UUID.fromString("8455663a-7ddc-44fc-bacd-a801d85c3632"),
                patientUuid = `uuid of first sync failed patient with blank bangladesh ID`,
                meta = ""
            )
        )
        .withBangladeshNationalId(UUID.fromString("5f84e1e1-7c34-4005-91b1-1fddc9ad0cd0"), id = "")
        .withSyncStatus(SyncStatus.INVALID)

    val `uuid of sync pending patient with blank bangladesh ID` = UUID.fromString("168f9b6b-e831-49de-80ae-65b3f7d6d688")
    val `sync pending patient with blank bangladesh ID` = testData
        .patientProfile(
            patientUuid = `uuid of sync pending patient with blank bangladesh ID`,
            generatePhoneNumber = false,
            businessId = testData.businessId(
                uuid = UUID.fromString("0caa9b1a-c85f-4437-b535-08e727760ce0"),
                patientUuid = `uuid of sync pending patient with blank bangladesh ID`,
                meta = ""
            )
        )
        .withBangladeshNationalId(UUID.fromString("46824088-0b57-41d2-a6c3-86677d6940ec"), id = "")
        .withSyncStatus(SyncStatus.PENDING)

    val `uuid of sync failed patient without bangladesh ID` = UUID.fromString("822007ff-a98c-4f45-8847-c8ff92693ba3")
    val `sync failed patient without bangladesh ID` = testData
        .patientProfile(
            patientUuid = `uuid of sync failed patient without bangladesh ID`,
            generatePhoneNumber = false,
            businessId = testData.businessId(
                uuid = UUID.fromString("faaa15e9-59c6-477f-a595-c0332930e401"),
                patientUuid = `uuid of sync failed patient without bangladesh ID`,
                meta = ""
            )
        )
        .withSyncStatus(SyncStatus.INVALID)

    val `uuid of second sync failed patient with blank bangladesh ID` = UUID.fromString("f01d777b-93ad-4f2b-ada9-89736ed86a2e")
    val `second sync failed patient with blank bangladesh ID` = testData
        .patientProfile(
            patientUuid = `uuid of second sync failed patient with blank bangladesh ID`,
            generatePhoneNumber = false,
            businessId = testData.businessId(
                uuid = UUID.fromString("80ea8ba3-57d3-4b9b-af34-8fa3a5c73f95"),
                patientUuid = `uuid of second sync failed patient with blank bangladesh ID`,
                meta = ""
            )
        )
        .withBangladeshNationalId(UUID.fromString("db67eb0a-a712-4935-85f7-0e7c59afe117"), id = "")
        .withSyncStatus(SyncStatus.INVALID)

    val `uuid of synced patient with non-blank bangladesh ID` = UUID.fromString("fb7bb116-4057-4970-a0b5-bec437655cc6")
    val `synced patient with non-blank bangladesh ID` = testData
        .patientProfile(
            patientUuid = `uuid of synced patient with non-blank bangladesh ID`,
            generatePhoneNumber = false,
            businessId = testData.businessId(
                uuid = UUID.fromString("4107567f-7c6f-4e83-bcc9-66f1238823e6"),
                patientUuid = `uuid of synced patient with non-blank bangladesh ID`,
                meta = ""
            )
        )
        .withBangladeshNationalId(UUID.fromString("4362679e-2950-46e9-9e19-79db50ab6dd7"), id = "123456abcd")
        .withSyncStatus(SyncStatus.DONE)

    before.savePatientProfile(`synced patient without bangladesh ID`)
    before.savePatientProfile(`sync pending patient without bangladesh ID`)
    before.savePatientProfile(`first sync failed patient with blank bangladesh ID`)
    before.savePatientProfile(`sync pending patient with blank bangladesh ID`)
    before.savePatientProfile(`sync failed patient without bangladesh ID`)
    before.savePatientProfile(`second sync failed patient with blank bangladesh ID`)
    before.savePatientProfile(`synced patient with non-blank bangladesh ID`)

    // then
    after.assertPatient(`synced patient without bangladesh ID`.patient)
    after.assertPatient(`sync pending patient without bangladesh ID`.patient)
    after.assertPatient(`first sync failed patient with blank bangladesh ID`.withSyncStatus(SyncStatus.PENDING).patient)
    after.assertPatient(`sync pending patient with blank bangladesh ID`.patient)
    after.assertPatient(`sync failed patient without bangladesh ID`.patient)
    after.assertPatient(`second sync failed patient with blank bangladesh ID`.withSyncStatus(SyncStatus.PENDING).patient)
    after.assertPatient(`synced patient with non-blank bangladesh ID`.patient)
  }

  @Suppress("IllegalIdentifier")
  @Test
  fun migrating_to_57_should_delete_all_blank_identifiers() {
    // given
    val `uuid of patient without bangladesh ID` = UUID.fromString("1c36ac74-a6ae-4ed2-afb1-42175e3625f9")
    val `patient without bangladesh ID` = testData
        .patientProfile(
            patientUuid = `uuid of patient without bangladesh ID`,
            generatePhoneNumber = false,
            businessId = testData.businessId(
                uuid = UUID.fromString("3fff1513-6906-40ff-bbf7-5ab901c5eaa4"),
                patientUuid = `uuid of patient without bangladesh ID`,
                meta = ""
            )
        )

    val `uuid of patient with blank bangladesh ID` = UUID.fromString("ed8df359-3965-4de1-9a25-d00533c1d292")
    val `patient with blank bangladesh ID` = testData
        .patientProfile(
            patientUuid = `uuid of patient with blank bangladesh ID`,
            generatePhoneNumber = false,
            businessId = testData.businessId(
                uuid = UUID.fromString("c5345a7a-fdcc-45e9-87f9-525856123382"),
                patientUuid = `uuid of patient with blank bangladesh ID`,
                meta = ""
            )
        )
        .withBangladeshNationalId(UUID.fromString("46824088-0b57-41d2-a6c3-86677d6940ec"), id = "")
    val `expected business IDs after migration for patient with blank bangladesh ID` = listOf(`patient with blank bangladesh ID`.businessIds[0])

    val `uuid of patient with multiple bangladesh IDs` = UUID.fromString("0c4f3c02-c17f-45f1-822b-6e79c5a21ccf")
    val `patient with multiple bangladesh IDs` = testData
        .patientProfile(
            patientUuid = `uuid of patient with multiple bangladesh IDs`,
            generatePhoneNumber = false,
            businessId = testData.businessId(
                uuid = UUID.fromString("8da804d9-6624-46ca-b007-dc94bce5d59c"),
                patientUuid = `uuid of patient with multiple bangladesh IDs`,
                meta = ""
            )
        )
        .withBangladeshNationalId(UUID.fromString("9a674529-4ccd-4eb0-a290-d49056721587"), id = "123456abcd")
        .withBangladeshNationalId(UUID.fromString("c2572fd7-e42a-497e-9c19-c2272924476f"), id = "")
    val `expected business IDs after migration for patient with multiple bangladesh IDs` = listOf(
        `patient with multiple bangladesh IDs`.businessIds[0],
        `patient with multiple bangladesh IDs`.businessIds[1]
    )

    before.savePatientProfile(`patient without bangladesh ID`)
    before.savePatientProfile(`patient with blank bangladesh ID`)
    before.savePatientProfile(`patient with multiple bangladesh IDs`)

    // then
    after.assertBusinessIdsForPatient(
        patientUuid = `uuid of patient without bangladesh ID`,
        expectedBusinessIds = `patient without bangladesh ID`.businessIds
    )
    after.assertBusinessIdsForPatient(
        patientUuid = `uuid of patient with blank bangladesh ID`,
        expectedBusinessIds = `expected business IDs after migration for patient with blank bangladesh ID`
    )
    after.assertBusinessIdsForPatient(
        patientUuid = `uuid of patient with multiple bangladesh IDs`,
        expectedBusinessIds = `expected business IDs after migration for patient with multiple bangladesh IDs`
    )
  }

  private fun PatientProfile.withBangladeshNationalId(uuid: UUID, id: String): PatientProfile {
    val bangladeshNationalId = testData.businessId(
        uuid = uuid,
        patientUuid = this.patientUuid,
        identifier = Identifier(id, Identifier.IdentifierType.BangladeshNationalId),
        meta = "meta"
    )
    return this.copy(businessIds = this.businessIds + bangladeshNationalId)
  }

  private fun SupportSQLiteDatabase.assertBusinessIdsForPatient(
      patientUuid: UUID,
      expectedBusinessIds: List<BusinessId>
  ) {
    val businessIdMap = expectedBusinessIds.associateBy { it.uuid }

    query(""" SELECT * FROM "BusinessId" WHERE "patientUuid" = '$patientUuid' """).use { cursor ->
      assertThat(cursor.count).isEqualTo(expectedBusinessIds.size)

      generateSequence { cursor.takeIf { it.moveToNext() } }
          .iterator()
          .forEach {
            val businessIdUuid = UUID.fromString(it.string("uuid"))
            val businessId = businessIdMap.getValue(businessIdUuid)

            with(businessId) {
              it.assertValues(mapOf(
                  "uuid" to uuid,
                  "patientUuid" to patientUuid,
                  "identifier" to identifier.value,
                  "identifierType" to Identifier.IdentifierType.TypeAdapter.knownMappings[identifier.type],
                  "metaVersion" to BusinessId.MetaDataVersion.TypeAdapter.knownMappings[metaDataVersion],
                  "meta" to metaData,
                  "createdAt" to createdAt,
                  "updatedAt" to updatedAt,
                  "deletedAt" to deletedAt
              ))
            }
          }
    }
  }
}
