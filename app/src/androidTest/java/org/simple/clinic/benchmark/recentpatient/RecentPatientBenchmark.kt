package org.simple.clinic.benchmark.recentpatient

import android.annotation.SuppressLint
import org.junit.Test
import org.simple.clinic.AppDatabase
import org.simple.clinic.PagingTestCase
import org.simple.clinic.TestClinicApp
import org.simple.clinic.benchmark.BaseBenchmarkTest
import org.simple.clinic.overdue.Appointment.AppointmentType.Manual
import org.simple.clinic.overdue.Appointment.Status.Scheduled
import org.simple.clinic.patient.PatientStatus.Active
import java.util.UUID
import javax.inject.Inject
import javax.inject.Provider

class RecentPatientsBenchmark : BaseBenchmarkTest() {

  @Inject
  lateinit var database: Provider<AppDatabase>

  init {
    TestClinicApp.appComponent().inject(this)
  }

  @Test
  @SuppressLint("CheckResult")
  fun querying_recent_patients_with_limit() {
    val dao = database.get().recentPatientDao()

    dao.recentPatientsWithLimit(
        facilityUuid = UUID.fromString("c68603b3-9293-4783-bd76-0dc425c0c5d2"),
        appointmentStatus = Scheduled,
        appointmentType = Manual,
        patientStatus = Active,
        limit = 10
    ).blockingFirst()
  }

  @Test
  fun querying_recent_patients() {
    val dao = database.get().recentPatientDao()
    val pagingTestCase = PagingTestCase(
        pagingSource = dao.recentPatients(
            facilityUuid = UUID.fromString("c68603b3-9293-4783-bd76-0dc425c0c5d2"),
            appointmentStatus = Scheduled,
            appointmentType = Manual,
            patientStatus = Active
        ),
        loadSize = 50
    )

    pagingTestCase.loadPage().data
  }
}
