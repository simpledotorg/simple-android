package org.simple.clinic.patient

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.di.NetworkModule
import org.simple.clinic.patient.PatientSummaryResult.Saved
import java.util.UUID

class PatientSummaryResultTest {

  /**
   * If you're seeing this test fail, it means that you modified
   * [PatientSummaryResult] without thinking about migration.
   *
   * The SharedPreferences key for [PatientSummaryResult] is
   * versioned, so the migration can potentially happen when it's
   * being read from SharedPreferences.
   */

  @Test
  fun `fail when migration is required`() {

    val moshi = NetworkModule().moshi()
    val adapter = moshi.adapter(PatientSummaryResult::class.java)

    val expectedJson = """
      {
      "patient_summary_result":"result_saved",
      "patientUuid":"b8d2e316-ac37-474a-a8da-815b96a1122e"
      }
      """

    val deserialize = adapter.fromJson(expectedJson)
    assertThat(deserialize).isEqualTo(Saved(patientUuid = UUID.fromString("b8d2e316-ac37-474a-a8da-815b96a1122e")))
  }
}
