package org.resolvetosavelives.red.newentry.search

import com.nhaarman.mockito_kotlin.argThat
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.resolvetosavelives.red.AppDatabase
import org.resolvetosavelives.red.sync.PatientAddressPayload
import org.resolvetosavelives.red.sync.PatientPayload

@RunWith(JUnitParamsRunner::class)
class PatientRepositoryTest {

  private lateinit var repository: PatientRepository
  private lateinit var database: AppDatabase

  @Before
  fun setUp() {
    database = mock()
    repository = PatientRepository(database)
  }

  @Test
  @Parameters(value = [
    "PENDING, false",
    "IN_FLIGHT, false",
    "INVALID, true",
    "DONE, true"])
  fun `when merging patients with server records, ignore records that already exist locally and are syncing or pending-sync`(
      syncStatusOfLocalCopy: SyncStatus,
      serverRecordExpectedToBeSaved: Boolean
  ) {
    val mockPatientDao = mock<Patient.RoomDao>()
    val mockPatientAddressDao = mock<PatientAddress.RoomDao>()
    whenever(database.patientDao()).thenReturn(mockPatientDao)
    whenever(database.addressDao()).thenReturn(mockPatientAddressDao)

    val localCopy = Patient("uuid", "uuid-address", "uuid-phone", "name", mock(), mock(), 0, mock(), mock(), mock(), syncStatusOfLocalCopy)
    whenever(mockPatientDao.get("uuid")).thenReturn(localCopy)

    val serverAddress = PatientAddressPayload("uuid-address", "colony", "district", "state", "country", mock(), mock())
    val serverPatient = PatientPayload("uuid", "name", mock(), mock(), 0, mock(), mock(), mock(), serverAddress)

    repository.mergeWithLocalData(listOf(serverPatient)).blockingAwait()

    if (serverRecordExpectedToBeSaved) {
      verify(mockPatientDao).save(argThat<List<Patient>> { isNotEmpty() })
      verify(mockPatientAddressDao).save(argThat<List<PatientAddress>> { isNotEmpty() })

    } else {
      verify(mockPatientDao).save(argThat<List<Patient>> { isEmpty() })
      verify(mockPatientAddressDao).save(argThat<List<PatientAddress>> { isEmpty() })
    }
  }
}
