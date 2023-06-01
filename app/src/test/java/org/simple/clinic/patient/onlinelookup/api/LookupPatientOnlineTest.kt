package org.simple.clinic.patient.onlinelookup.api

import com.google.common.truth.Truth.assertThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.junit.Test
import org.simple.clinic.FakeCall
import org.simple.clinic.patient.onlinelookup.api.LookupPatientOnline.Result.NotFound
import org.simple.clinic.patient.onlinelookup.api.LookupPatientOnline.Result.OtherError
import org.simple.clinic.patient.sync.PatientSyncApi
import org.simple.sharedTestCode.util.TestUtcClock
import java.time.Duration
import java.time.Instant

class LookupPatientOnlineTest {

  private val patientSyncApi = mock<PatientSyncApi>()

  private val clock = TestUtcClock(Instant.parse("2018-01-01T00:00:00Z"))

  private val lookupPatientOnline = LookupPatientOnline(
      patientSyncApi = patientSyncApi,
      clock = clock,
      fallbackRecordRetentionDuration = Duration.ZERO
  )

  @Test
  fun `when the server returns HTTP 404, the not found result must be returned`() {
    // given
    val call = FakeCall.error<OnlineLookupResponsePayload>(
        data = "",
        responseCode = 404
    )
    val identifier = "ec1286b8-6603-4eef-8576-9777cd989502"
    val lookupRequest = PatientOnlineLookupRequest(identifier)
    whenever(patientSyncApi.lookup(lookupRequest)).thenReturn(call)

    // when
    val result = lookupPatientOnline.lookupWithIdentifier(identifier)

    // then
    assertThat(result).isEqualTo(NotFound(identifier))
  }

  @Test
  fun `when no patients are found on the server, the not found result must be returned`() {
    // given
    val call = FakeCall.success(
        response = OnlineLookupResponsePayload(patients = emptyList())
    )
    val identifier = "ec1286b8-6603-4eef-8576-9777cd989502"
    val lookupRequest = PatientOnlineLookupRequest(identifier)
    whenever(patientSyncApi.lookup(lookupRequest)).thenReturn(call)

    // when
    val result = lookupPatientOnline.lookupWithIdentifier(identifier)

    // then
    assertThat(result).isEqualTo(NotFound(identifier))
  }

  @Test
  fun `when any response code apart from 200, 404 is received, the other error result must be returned`() {
    // given
    val call = FakeCall.error<OnlineLookupResponsePayload>(
        data = "",
        responseCode = 500
    )
    val identifier = "ec1286b8-6603-4eef-8576-9777cd989502"
    val lookupRequest = PatientOnlineLookupRequest(identifier)
    whenever(patientSyncApi.lookup(lookupRequest)).thenReturn(call)

    // when
    val result = lookupPatientOnline.lookupWithIdentifier(identifier)

    // then
    assertThat(result).isEqualTo(OtherError)
  }

  @Test
  fun `when the network call fails, the other error result must be returned`() {
    // given
    val call = FakeCall.failure<OnlineLookupResponsePayload>(RuntimeException())
    val identifier = "ec1286b8-6603-4eef-8576-9777cd989502"
    val lookupRequest = PatientOnlineLookupRequest(identifier)
    whenever(patientSyncApi.lookup(lookupRequest)).thenReturn(call)

    // when
    val result = lookupPatientOnline.lookupWithIdentifier(identifier)

    // then
    assertThat(result).isEqualTo(OtherError)
  }
}
