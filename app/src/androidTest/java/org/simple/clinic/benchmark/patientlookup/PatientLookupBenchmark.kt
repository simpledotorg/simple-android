package org.simple.clinic.benchmark.patientlookup

import org.junit.Test
import org.simple.clinic.PagingTestCase
import org.simple.clinic.TestClinicApp
import org.simple.clinic.benchmark.BaseBenchmarkTest
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientSearchResult
import java.util.UUID
import javax.inject.Inject
import javax.inject.Provider

class PatientLookupBenchmark : BaseBenchmarkTest() {

  @Inject
  lateinit var patientDaoProvider: Provider<Patient.RoomDao>

  @Inject
  lateinit var patientSearchDaoProvider: Provider<PatientSearchResult.RoomDao>

  init {
    TestClinicApp.appComponent().inject(this)
  }

  @Test
  fun querying_the_patient_by_business_id() {
    val dao = patientDaoProvider.get()

    dao.findPatientsWithBusinessIdImmediate("ae17c64d-36fc-4a18-b904-0980cc1aa2c8")
  }

  @Test
  fun looking_for_patient_by_name() {
    val dao = patientSearchDaoProvider.get()
    val pagingTestCase = PagingTestCase(
        pagingSource = dao.searchByNamePagingSource(
            name = "Shyam",
            facilityId = UUID.fromString("c68603b3-9293-4783-bd76-0dc425c0c5d2")
        ),
        loadSize = 50
    )

    pagingTestCase.loadPage().data
  }
}
