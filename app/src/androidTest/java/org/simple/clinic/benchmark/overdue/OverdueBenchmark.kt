package org.simple.clinic.benchmark.overdue

import android.annotation.SuppressLint
import org.junit.Test
import org.simple.clinic.PagingTestCase
import org.simple.clinic.TestClinicApp
import org.simple.clinic.benchmark.BaseBenchmarkTest
import org.simple.clinic.overdue.AppointmentRepository
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

class OverdueBenchmark : BaseBenchmarkTest() {

  @Inject
  lateinit var appointmentRepository: AppointmentRepository

  init {
    TestClinicApp.appComponent().inject(this)
  }

  @SuppressLint("CheckResult")
  @Test
  fun fetching_overdue_appointments_in_a_facility() {
    appointmentRepository.overdueAppointmentsInFacility(
        since = LocalDate.now(),
        facilityId = UUID.fromString("c68603b3-9293-4783-bd76-0dc425c0c5d2")
    ).blockingFirst()
  }

  @Test
  fun searching_overdue_patients_in_a_facility() {
    val pagingTestCase = PagingTestCase(
        pagingSource = appointmentRepository.searchOverduePatient(
            searchInputs = listOf("Ani"),
            since = LocalDate.now(),
            facilityId = UUID.fromString("c68603b3-9293-4783-bd76-0dc425c0c5d2")
        ),
        loadSize = 50
    )
    pagingTestCase.loadPage().data
  }
}
