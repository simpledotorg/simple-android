package org.simple.clinic.patient.businessid

import androidx.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.TestClinicApp
import java.util.UUID
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class BusinessIdMetaDataAdapterAndroidTest {

  @field:Inject
  lateinit var businessIdMetaDataAdapter: BusinessIdMetaDataAdapter

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
            is BusinessId.MetaDataVersion.Unknown -> throw RuntimeException("$metaDataVersion should not be a part of known mappings!")
            is BusinessId.MetaDataVersion.BpPassportV1 -> BusinessIdMetaData.BpPassportV1(
                assigningUserUuid = UUID.randomUUID(),
                assigningFacilityUuid = UUID.randomUUID()
            )
            else -> throw RuntimeException("$metaDataVersion has not been handled!")
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
