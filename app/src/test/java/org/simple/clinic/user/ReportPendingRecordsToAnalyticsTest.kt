package org.simple.clinic.user

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Single
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.analytics.Analytics
import org.simple.clinic.analytics.MockAnalyticsReporter
import org.simple.clinic.analytics.MockAnalyticsReporter.Event
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.overdue.Appointment
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.SyncStatus.PENDING
import org.threeten.bp.Duration
import org.threeten.bp.Instant

@RunWith(JUnitParamsRunner::class)
class ReportPendingRecordsToAnalyticsTest {

  val patientRepository = mock<PatientRepository>()
  val bloodPressureRepository = mock<BloodPressureRepository>()
  val appointmentRepository = mock<AppointmentRepository>()
  val prescriptionRepository = mock<PrescriptionRepository>()
  val medicalHistoryRepository = mock<MedicalHistoryRepository>()

  val reporter = MockAnalyticsReporter()

  val reportPendingRecordsToAnalytics = ReportPendingRecordsToAnalytics(
      patientRepository = patientRepository,
      bloodPressureRepository = bloodPressureRepository,
      appointmentRepository = appointmentRepository,
      prescriptionRepository = prescriptionRepository,
      medicalHistoryRepository = medicalHistoryRepository
  )

  @Before
  fun setUp() {
    Analytics.addReporter(reporter)

    whenever(patientRepository.recordsWithSyncStatus(PENDING)).thenReturn(Single.just(emptyList()))
    whenever(bloodPressureRepository.recordsWithSyncStatus(PENDING)).thenReturn(Single.just(emptyList()))
    whenever(appointmentRepository.recordsWithSyncStatus(PENDING)).thenReturn(Single.just(emptyList()))
    whenever(prescriptionRepository.recordsWithSyncStatus(PENDING)).thenReturn(Single.just(emptyList()))
    whenever(medicalHistoryRepository.recordsWithSyncStatus(PENDING)).thenReturn(Single.just(emptyList()))
  }

  @After
  fun tearDown() {
    Analytics.clearReporters()
    reporter.clear()
  }

  @Test
  fun `pending patient records must be reported to analytics`() {
    val now = Instant.now()
    val patientRecords = listOf(
        patientRecord(now),
        patientRecord(now),
        patientRecord(now)
    )
    whenever(patientRepository.recordsWithSyncStatus(PENDING)).thenReturn(Single.just(patientRecords))

    reportPendingRecordsToAnalytics.report().blockingAwait()

    assertThat(reporter.receivedEvents).isEqualTo(listOf(
        Event(name = "DataCleared", props = mapOf(
            "pendingPatientCount" to 3,
            "pendingBpCount" to 0,
            "pendingAppointmentCount" to 0,
            "pendingPrescribedDrugCount" to 0,
            "pendingMedicalHistoryCount" to 0,
            "since" to now.toString()
        ))
    ))
  }

  @Test
  fun `pending bp records should be reported to analytics`() {
    val now = Instant.now()
    val bpRecords = listOf(
        PatientMocker.bp(updatedAt = now),
        PatientMocker.bp(updatedAt = now),
        PatientMocker.bp(updatedAt = now),
        PatientMocker.bp(updatedAt = now),
        PatientMocker.bp(updatedAt = now)
    )
    whenever(bloodPressureRepository.recordsWithSyncStatus(PENDING)).thenReturn(Single.just(bpRecords))

    reportPendingRecordsToAnalytics.report().blockingAwait()

    assertThat(reporter.receivedEvents).isEqualTo(listOf(
        Event(name = "DataCleared", props = mapOf(
            "pendingPatientCount" to 0,
            "pendingBpCount" to 5,
            "pendingAppointmentCount" to 0,
            "pendingPrescribedDrugCount" to 0,
            "pendingMedicalHistoryCount" to 0,
            "since" to now.toString()
        ))
    ))
  }

  @Test
  fun `pending appointment records should be reported to analytics`() {
    val now = Instant.now()
    val appointmentRecords = listOf(
        PatientMocker.appointment(updatedAt = now),
        PatientMocker.appointment(updatedAt = now),
        PatientMocker.appointment(updatedAt = now),
        PatientMocker.appointment(updatedAt = now)
    )
    whenever(appointmentRepository.recordsWithSyncStatus(PENDING)).thenReturn(Single.just(appointmentRecords))

    reportPendingRecordsToAnalytics.report().blockingAwait()

    assertThat(reporter.receivedEvents).isEqualTo(listOf(
        Event(name = "DataCleared", props = mapOf(
            "pendingPatientCount" to 0,
            "pendingBpCount" to 0,
            "pendingAppointmentCount" to 4,
            "pendingPrescribedDrugCount" to 0,
            "pendingMedicalHistoryCount" to 0,
            "since" to now.toString()
        ))
    ))
  }

  @Test
  fun `pending prescribed drug records should be reported to analytics`() {
    val now = Instant.now()
    val prescribedDrugRecords = listOf(
        PatientMocker.prescription(updatedAt = now),
        PatientMocker.prescription(updatedAt = now)
    )
    whenever(prescriptionRepository.recordsWithSyncStatus(PENDING)).thenReturn(Single.just(prescribedDrugRecords))

    reportPendingRecordsToAnalytics.report().blockingAwait()

    assertThat(reporter.receivedEvents).isEqualTo(listOf(
        Event(name = "DataCleared", props = mapOf(
            "pendingPatientCount" to 0,
            "pendingBpCount" to 0,
            "pendingAppointmentCount" to 0,
            "pendingPrescribedDrugCount" to 2,
            "pendingMedicalHistoryCount" to 0,
            "since" to now.toString()
        ))
    ))
  }

  @Test
  fun `pending medical history records should be reported to analytics`() {
    val now = Instant.now()
    val medicalHistoryRecords = listOf(
        PatientMocker.medicalHistory(updatedAt = now),
        PatientMocker.medicalHistory(updatedAt = now),
        PatientMocker.medicalHistory(updatedAt = now),
        PatientMocker.medicalHistory(updatedAt = now),
        PatientMocker.medicalHistory(updatedAt = now),
        PatientMocker.medicalHistory(updatedAt = now)
    )
    whenever(medicalHistoryRepository.recordsWithSyncStatus(PENDING)).thenReturn(Single.just(medicalHistoryRecords))

    reportPendingRecordsToAnalytics.report().blockingAwait()

    assertThat(reporter.receivedEvents).isEqualTo(listOf(
        Event(name = "DataCleared", props = mapOf(
            "pendingPatientCount" to 0,
            "pendingBpCount" to 0,
            "pendingAppointmentCount" to 0,
            "pendingPrescribedDrugCount" to 0,
            "pendingMedicalHistoryCount" to 6,
            "since" to now.toString()
        ))
    ))
  }

  @Test
  fun `when there are no pending records, nothing should be reported to analytics`() {
    reportPendingRecordsToAnalytics.report().blockingAwait()

    assertThat(reporter.receivedEvents).isEmpty()
  }

  @Test
  @Parameters(method = "params for reporting oldest record timestamp")
  fun `the oldest record timestamp must be reported to analytics`(
      patientRecords: List<PatientProfile>,
      bpRecords: List<BloodPressureMeasurement>,
      appointmentRecords: List<Appointment>,
      prescribedDrugRecords: List<PrescribedDrug>,
      medicalHistoryRecords: List<MedicalHistory>,
      expectedSince: Instant
  ) {
    whenever(patientRepository.recordsWithSyncStatus(PENDING)).thenReturn(Single.just(patientRecords))
    whenever(bloodPressureRepository.recordsWithSyncStatus(PENDING)).thenReturn(Single.just(bpRecords))
    whenever(appointmentRepository.recordsWithSyncStatus(PENDING)).thenReturn(Single.just(appointmentRecords))
    whenever(prescriptionRepository.recordsWithSyncStatus(PENDING)).thenReturn(Single.just(prescribedDrugRecords))
    whenever(medicalHistoryRepository.recordsWithSyncStatus(PENDING)).thenReturn(Single.just(medicalHistoryRecords))

    reportPendingRecordsToAnalytics.report().blockingAwait()

    assertThat(reporter.receivedEvents).isEqualTo(listOf(
        Event(name = "DataCleared", props = mapOf(
            "pendingPatientCount" to patientRecords.size,
            "pendingBpCount" to bpRecords.size,
            "pendingAppointmentCount" to appointmentRecords.size,
            "pendingPrescribedDrugCount" to prescribedDrugRecords.size,
            "pendingMedicalHistoryCount" to medicalHistoryRecords.size,
            "since" to expectedSince.toString()
        ))
    ))
  }

  @Suppress("Unused")
  private fun `params for reporting oldest record timestamp`(): List<List<Any>> {
    fun testCase(
        patientRecords: List<PatientProfile>,
        bpRecords: List<BloodPressureMeasurement>,
        appointmentRecords: List<Appointment>,
        prescribedDrugRecords: List<PrescribedDrug>,
        medicalHistoryRecords: List<MedicalHistory>,
        expectedSince: Instant
    ): List<Any> {
      return listOf(patientRecords, bpRecords, appointmentRecords, prescribedDrugRecords, medicalHistoryRecords, expectedSince)
    }

    val now = Instant.now()
    val twoHoursEarlier = now.minus(Duration.ofHours(2L))
    val oneDayEarlier = now.minus(Duration.ofDays(1L))
    val twoHoursLater = now.plus(Duration.ofHours(2L))
    val oneDayLater = now.plus(Duration.ofDays(1L))

    return listOf(
        testCase(
            patientRecords = listOf(patientRecord(updatedAt = now)),
            bpRecords = listOf(PatientMocker.bp(updatedAt = twoHoursEarlier)),
            appointmentRecords = listOf(PatientMocker.appointment(updatedAt = twoHoursLater)),
            prescribedDrugRecords = listOf(PatientMocker.prescription(updatedAt = oneDayLater)),
            medicalHistoryRecords = listOf(PatientMocker.medicalHistory(updatedAt = twoHoursLater)),
            expectedSince = twoHoursEarlier
        ),
        testCase(
            patientRecords = emptyList(),
            bpRecords = emptyList(),
            appointmentRecords = listOf(PatientMocker.appointment(updatedAt = twoHoursLater)),
            prescribedDrugRecords = emptyList(),
            medicalHistoryRecords = emptyList(),
            expectedSince = twoHoursLater
        ),
        testCase(
            patientRecords = emptyList(),
            bpRecords = emptyList(),
            appointmentRecords = listOf(PatientMocker.appointment(updatedAt = oneDayLater)),
            prescribedDrugRecords = emptyList(),
            medicalHistoryRecords = listOf(PatientMocker.medicalHistory(updatedAt = oneDayLater)),
            expectedSince = oneDayLater
        ),
        testCase(
            patientRecords = listOf(
                patientRecord(updatedAt = oneDayEarlier),
                patientRecord(updatedAt = now),
                patientRecord(updatedAt = twoHoursEarlier)
            ),
            bpRecords = listOf(
                PatientMocker.bp(updatedAt = now),
                PatientMocker.bp(updatedAt = twoHoursEarlier),
                PatientMocker.bp(updatedAt = oneDayLater),
                PatientMocker.bp(updatedAt = twoHoursEarlier)
            ),
            appointmentRecords = emptyList(),
            prescribedDrugRecords = listOf(
                PatientMocker.prescription(updatedAt = now),
                PatientMocker.prescription(updatedAt = twoHoursEarlier),
                PatientMocker.prescription(updatedAt = twoHoursLater)
            ),
            medicalHistoryRecords = emptyList(),
            expectedSince = oneDayEarlier
        ),
        testCase(
            patientRecords = emptyList(),
            bpRecords = listOf(
                PatientMocker.bp(updatedAt = now),
                PatientMocker.bp(updatedAt = twoHoursLater)
            ),
            appointmentRecords = listOf(
                PatientMocker.appointment(updatedAt = now),
                PatientMocker.appointment(updatedAt = twoHoursEarlier),
                PatientMocker.appointment(updatedAt = now)
            ),
            prescribedDrugRecords = emptyList(),
            medicalHistoryRecords = listOf(
                PatientMocker.medicalHistory(updatedAt = now),
                PatientMocker.medicalHistory(updatedAt = oneDayLater),
                PatientMocker.medicalHistory(updatedAt = twoHoursLater)
            ),
            expectedSince = twoHoursEarlier
        ),
        testCase(
            patientRecords = listOf(patientRecord(updatedAt = oneDayLater)),
            bpRecords = emptyList(),
            appointmentRecords = listOf(
                PatientMocker.appointment(updatedAt = twoHoursLater),
                PatientMocker.appointment(updatedAt = oneDayLater)
            ),
            prescribedDrugRecords = listOf(
                PatientMocker.prescription(updatedAt = now),
                PatientMocker.prescription(updatedAt = twoHoursLater)
            ),
            medicalHistoryRecords = emptyList(),
            expectedSince = now
        ),
        testCase(
            patientRecords = listOf(
                patientRecord(updatedAt = now),
                patientRecord(updatedAt = now),
                patientRecord(updatedAt = twoHoursLater)
            ),
            bpRecords = listOf(
                PatientMocker.bp(updatedAt = oneDayLater),
                PatientMocker.bp(updatedAt = now),
                PatientMocker.bp(updatedAt = twoHoursEarlier)
            ),
            appointmentRecords = emptyList(),
            prescribedDrugRecords = listOf(PatientMocker.prescription(updatedAt = twoHoursEarlier)),
            medicalHistoryRecords = listOf(
                PatientMocker.medicalHistory(updatedAt = now),
                PatientMocker.medicalHistory(updatedAt = oneDayEarlier),
                PatientMocker.medicalHistory(updatedAt = twoHoursLater)
            ),
            expectedSince = oneDayEarlier
        )
    )
  }

  private fun patientRecord(updatedAt: Instant): PatientProfile {
    return PatientMocker
        .patientProfile()
        .let { patientProfile ->
          patientProfile.copy(patient = patientProfile.patient.copy(updatedAt = updatedAt))
        }
  }
}
