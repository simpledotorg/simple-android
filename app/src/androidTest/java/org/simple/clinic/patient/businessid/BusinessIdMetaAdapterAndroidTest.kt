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
class BusinessIdMetaAdapterAndroidTest {

  @field:Inject
  lateinit var businessIdMetaAdapter: BusinessIdMetaAdapter

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
  }

  @Test
  fun all_known_mappings_of_business_meta_version_must_be_de_serialized_correctly() {
    // This test is to ensure that whenever a new meta version is added, its corresponding
    // json adapter is also added to the MoshiBusinessIdMetaAdapter.
    val businessIdMetas = BusinessId.MetaVersion
        .values()
        .map { metaVersion ->
          metaVersion to when (metaVersion) {
            is BusinessId.MetaVersion.Unknown -> throw RuntimeException("$metaVersion should not be a part of known mappings!")
            is BusinessId.MetaVersion.BpPassportV1 -> BusinessIdMeta.BpPassportV1(
                assigningUserUuid = UUID.randomUUID(),
                assigningFacilityUuid = UUID.randomUUID()
            )
            else -> throw RuntimeException("$metaVersion has not been handled!")
          }
        }

    businessIdMetas
        .forEach { (metaVersion, meta) ->
          val serialized = businessIdMetaAdapter.serialize(meta, metaVersion)
          val deserialized = businessIdMetaAdapter.deserialize(serialized, metaVersion)
          assertThat(deserialized).isEqualTo(meta)
        }
  }
}
