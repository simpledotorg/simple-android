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

  @field:Inject
  lateinit var businessIdMetaDataAdapter: BusinessIdMetaDataAdapter

  @get:Rule
  val rules: RuleChain = Rules.global()

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
  }

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
                assigningUserUuid = UUID.randomUUID(),
                assigningFacilityUuid = UUID.randomUUID()
            )
            BusinessId.MetaDataVersion.BangladeshNationalIdMetaDataV1 -> BusinessIdMetaData.BangladeshNationalIdMetaDataV1(
                assigningUserUuid = UUID.randomUUID(),
                assigningFacilityUuid = UUID.randomUUID()
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
