package org.simple.clinic.patient.businessid

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.simple.clinic.TestClinicApp
import org.simple.clinic.util.Rules
import java.util.UUID
import javax.inject.Inject


class BusinessIdMetaDataAdapterAndroidTest {

  @Inject
  lateinit var businessIdMetaDataAdapter: BusinessIdMetaDataAdapter

  @get:Rule
  val rules: RuleChain = Rules.global()

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
  }

  private val assigningUserUuid: UUID = UUID.fromString("22219626-2b2e-46c9-9e3d-889580adae83")
  private val assigningFacilityUuid: UUID = UUID.fromString("c4718eb9-26b7-436c-998e-9ae2ee22c4fd")

  @Test
  fun all_known_mappings_of_business_meta_version_must_be_de_serialized_correctly() {
    // This test is to ensure that whenever a new meta version is added, its corresponding
    // json adapter is also added to the MoshiBusinessIdMetaDataAdapter.
    val businessIdMetaData = BusinessId.MetaDataVersion
        .values()
        .map { metaDataVersion ->
          metaDataVersion to when (metaDataVersion) {
            is BusinessId.MetaDataVersion.Unknown -> throw RuntimeException("$metaDataVersion should be a part of known mappings!")
            BusinessId.MetaDataVersion.BpPassportMetaDataV1 -> BusinessIdMetaData.BpPassportMetaDataV1(
                assigningUserUuid = assigningUserUuid,
                assigningFacilityUuid = assigningFacilityUuid
            )
            BusinessId.MetaDataVersion.BangladeshNationalIdMetaDataV1 -> BusinessIdMetaData.BangladeshNationalIdMetaDataV1(
                assigningUserUuid = assigningUserUuid,
                assigningFacilityUuid = assigningFacilityUuid
            )
            BusinessId.MetaDataVersion.MedicalRecordNumberMetaDataV1 -> BusinessIdMetaData.MedicalRecordNumberMetaDataV1(
                assigningUserUuid = assigningUserUuid,
                assigningFacilityUuid = assigningFacilityUuid
            )
          }
        }

    businessIdMetaData
        .forEach { (metaDataVersion, meta) ->
          val serialized = businessIdMetaDataAdapter.serialize(meta, metaDataVersion)
          val deserialized = businessIdMetaDataAdapter.deserialize(serialized, metaDataVersion)
          assertThat(deserialized).isEqualTo(meta)
        }
  }
}
