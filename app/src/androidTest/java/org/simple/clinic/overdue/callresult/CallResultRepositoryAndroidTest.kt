package org.simple.clinic.overdue.callresult

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.simple.clinic.AppDatabase
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.overdue.AppointmentCancelReason.Dead
import org.simple.clinic.overdue.AppointmentCancelReason.InvalidPhoneNumber
import org.simple.clinic.overdue.AppointmentCancelReason.PatientNotResponding
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.rules.LocalAuthenticationRule
import org.simple.clinic.rules.SaveDatabaseRule
import org.simple.clinic.util.Rules
import java.time.Instant
import java.util.Optional
import java.util.UUID
import javax.inject.Inject

class CallResultRepositoryAndroidTest {

  @Inject
  lateinit var callResultRepository: CallResultRepository

  @Inject
  lateinit var appDatabase: AppDatabase

  private val callResultDao by lazy { appDatabase.callResultDao() }

  @get:Rule
  val rules: RuleChain = Rules
      .global()
      .around(LocalAuthenticationRule())
      .around(SaveDatabaseRule())

  @Before
  fun setup() {
    TestClinicApp.appComponent().inject(this)
  }

  @Test
  fun load_call_result_for_the_appointment() {
    // given
    val appointmentId1 = UUID.fromString("8af440c8-84d8-4587-a0f0-d3285c140bf9")
    val appointmentId2 = UUID.fromString("09e5ace9-ccd3-4d46-922a-e075e0b141a4")
    val appointmentId3 = UUID.fromString("99ee62bb-f11f-476a-b816-4242adbe049a")

    val callResult1 = TestData.callResult(
        id = UUID.fromString("46077959-0a48-4c22-ac5b-dbb2d5d74b2d"),
        appointmentId = appointmentId1,
        removeReason = PatientNotResponding,
        deletedAt = null,
        syncStatus = SyncStatus.DONE
    )
    val callResult2 = TestData.callResult(
        id = UUID.fromString("9c7af70a-985f-43e7-8bf4-1f0515872577"),
        appointmentId = appointmentId2,
        removeReason = Dead,
        deletedAt = null,
        syncStatus = SyncStatus.DONE
    )
    val callResult3 = TestData.callResult(
        id = UUID.fromString("0f59e8bb-9806-4134-af7f-a11bea2c244d"),
        appointmentId = appointmentId3,
        removeReason = InvalidPhoneNumber,
        deletedAt = null,
        syncStatus = SyncStatus.DONE,
        createdAt = Instant.parse("2018-01-01T00:00:00Z")
    )

    val callResult4 = TestData.callResult(
        id = UUID.fromString("1072a2b4-34ab-4581-bac2-35a4d220bf07"),
        appointmentId = appointmentId3,
        removeReason = InvalidPhoneNumber,
        deletedAt = null,
        syncStatus = SyncStatus.DONE,
        createdAt = Instant.parse("2018-01-01T00:01:00Z")
    )

    val callResult5 = TestData.callResult(
        id = UUID.fromString("f42aa744-2025-48bd-b787-b1f71569c578"),
        appointmentId = appointmentId3,
        removeReason = InvalidPhoneNumber,
        deletedAt = null,
        syncStatus = SyncStatus.DONE,
        createdAt = Instant.parse("2018-01-01T00:02:00Z")
    )

    val callResultData = listOf(
        callResult1,
        callResult2,
        callResult3,
        callResult4,
        callResult5
    )

    callResultDao.save(callResultData)

    // then
    assertThat(callResultRepository.callResultForAppointment(appointmentId1)).isEqualTo(Optional.of(callResult1))
    assertThat(callResultRepository.callResultForAppointment(appointmentId2)).isEqualTo(Optional.of(callResult2))
    assertThat(callResultRepository.callResultForAppointment(appointmentId3)).isEqualTo(Optional.of(callResult5))
  }
}
