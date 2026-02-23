package org.simple.clinic.patient.medicalRecords

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.simple.clinic.TestData
import org.simple.clinic.patient.CompleteMedicalRecord
import org.simple.clinic.patient.sync.PatientSyncApi
import org.simple.clinic.sync.DataPushResponse
import org.simple.clinic.util.TestUtcClock
import retrofit2.Call
import retrofit2.Response
import java.time.Instant

class PushMedicalRecordsOnlineTest {

  private val patientSyncApi = mock<PatientSyncApi>()

  private val clock = TestUtcClock(
      Instant.parse("2018-01-01T00:00:00Z")
  )

  private val pushMedicalRecordsOnline = PushMedicalRecordsOnline(
      patientSyncApi = patientSyncApi,
      clock = clock,
  )


  private fun mockCall(): Call<DataPushResponse> = mock()

  private fun fakeMedicalRecord(): CompleteMedicalRecord = TestData.completeMedicalRecord()


  @Test
  fun `when no medical records then return NothingToPush and do not call api`() {
    val result = pushMedicalRecordsOnline
        .pushAllMedicalRecordsOnServer(emptyList())

    assertEquals(
        PushMedicalRecordsOnline.Result.NothingToPush,
        result
    )

    verify(patientSyncApi, org.mockito.kotlin.never())
        .pushAllPatientsData(any())
  }

  @Test
  fun `when server returns 200 then return Success`() {
    val call = mockCall()

    whenever(patientSyncApi.pushAllPatientsData(any()))
        .thenReturn(call)

    whenever(call.execute())
        .thenReturn(
            Response.success(
                DataPushResponse(
                    validationErrors = emptyList()
                )
            )
        )

    val result = pushMedicalRecordsOnline.pushAllMedicalRecordsOnServer(
        listOf(fakeMedicalRecord())
    )

    assertEquals(
        PushMedicalRecordsOnline.Result.Success,
        result
    )

    verify(patientSyncApi).pushAllPatientsData(any())
  }

  @Test
  fun `when server returns non-200 then return ServerError`() {
    val call = mockCall()

    whenever(patientSyncApi.pushAllPatientsData(any()))
        .thenReturn(call)

    whenever(call.execute())
        .thenReturn(
            Response.error(
                500,
                "boom".toResponseBody("text/plain".toMediaType())
            )
        )

    val result = pushMedicalRecordsOnline.pushAllMedicalRecordsOnServer(
        listOf(fakeMedicalRecord())
    )

    result as PushMedicalRecordsOnline.Result.ServerError

    assertEquals(500, result.code)
    assertEquals("boom", result.message)
  }

  @Test
  fun `when api throws exception then return NetworkError`() {
    val call = mockCall()

    whenever(patientSyncApi.pushAllPatientsData(any()))
        .thenReturn(call)

    whenever(call.execute())
        .thenThrow(RuntimeException("network down"))

    val result = pushMedicalRecordsOnline.pushAllMedicalRecordsOnServer(
        listOf(fakeMedicalRecord())
    )

    assert(result is PushMedicalRecordsOnline.Result.NetworkError)
  }
}
