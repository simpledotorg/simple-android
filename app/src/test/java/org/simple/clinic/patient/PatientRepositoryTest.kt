package org.simple.clinic.patient

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argThat
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.AppDatabase
import org.simple.clinic.newentry.DateOfBirthFormatValidator
import org.simple.clinic.patient.sync.PatientPayload
import org.simple.clinic.patient.sync.PatientPhoneNumberPayload
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class PatientRepositoryTest {

  private lateinit var repository: PatientRepository
  private lateinit var database: AppDatabase

  private val mockPatientDao = mock<Patient.RoomDao>()
  private val mockPatientAddressDao = mock<PatientAddress.RoomDao>()
  private val mockPatientPhoneNumberDao = mock<PatientPhoneNumber.RoomDao>()
  private val dobValidator = mock<DateOfBirthFormatValidator>()

  @Before
  fun setUp() {
    database = mock()
    repository = PatientRepository(database, dobValidator)
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
    whenever(database.patientDao()).thenReturn(mockPatientDao)
    whenever(database.addressDao()).thenReturn(mockPatientAddressDao)

    val patientUuid = UUID.randomUUID()
    val addressUuid = UUID.randomUUID()

    val localPatientCopy = PatientMocker.patient(uuid = patientUuid, addressUuid = addressUuid, syncStatus = syncStatusOfLocalCopy)
    whenever(mockPatientDao.getOne(patientUuid)).thenReturn(localPatientCopy)

    val serverAddress = PatientMocker.address(uuid = addressUuid).toPayload()
    val serverPatientWithoutPhone = PatientPayload(
        uuid = patientUuid,
        fullName = "name",
        gender = mock(),
        dateOfBirth = mock(),
        age = 0,
        ageUpdatedAt = mock(),
        status = mock(),
        createdAt = mock(),
        updatedAt = mock(),
        address = serverAddress,
        phoneNumbers = null)

    repository.mergeWithLocalData(listOf(serverPatientWithoutPhone)).blockingAwait()

    if (serverRecordExpectedToBeSaved) {
      verify(mockPatientDao).save(argThat<List<Patient>> { isNotEmpty() })
      verify(mockPatientAddressDao).save(argThat<List<PatientAddress>> { isNotEmpty() })

    } else {
      verify(mockPatientDao).save(argThat<List<Patient>> { isEmpty() })
      verify(mockPatientAddressDao).save(argThat<List<PatientAddress>> { isEmpty() })
    }
  }

  @Test
  @Parameters(value = [
    "PENDING, false",
    "IN_FLIGHT, false",
    "INVALID, true",
    "DONE, true"])
  fun `that already exist locally and are syncing or pending-sync`(
      syncStatusOfLocalCopy: SyncStatus,
      serverRecordExpectedToBeSaved: Boolean
  ) {
    whenever(database.patientDao()).thenReturn(mockPatientDao)
    whenever(database.addressDao()).thenReturn(mockPatientAddressDao)
    whenever(database.phoneNumberDao()).thenReturn(mockPatientPhoneNumberDao)

    val patientUuid = UUID.randomUUID()
    val addressUuid = UUID.randomUUID()

    val localPatientCopy = PatientMocker.patient(uuid = patientUuid, addressUuid = addressUuid, syncStatus = syncStatusOfLocalCopy)
    whenever(mockPatientDao.getOne(patientUuid)).thenReturn(localPatientCopy)

    val serverAddress = PatientMocker.address(uuid = addressUuid).toPayload()
    val serverPatientWithPhone = PatientPayload(
        uuid = patientUuid,
        fullName = "name",
        gender = mock(),
        dateOfBirth = mock(),
        age = 0,
        ageUpdatedAt = mock(),
        status = mock(),
        createdAt = mock(),
        updatedAt = mock(),

        address = serverAddress,
        phoneNumbers = listOf(PatientPhoneNumberPayload(UUID.randomUUID(), "1232", mock(), false, mock(), mock())))

    repository.mergeWithLocalData(listOf(serverPatientWithPhone)).blockingAwait()

    if (serverRecordExpectedToBeSaved) {
      verify(mockPatientAddressDao).save(argThat<List<PatientAddress>> { isNotEmpty() })
      verify(mockPatientDao).save(argThat<List<Patient>> { isNotEmpty() })
      verify(mockPatientPhoneNumberDao).save(argThat { isNotEmpty() })

    } else {
      verify(mockPatientAddressDao).save(argThat<List<PatientAddress>> { isEmpty() })
      verify(mockPatientDao).save(argThat<List<Patient>> { isEmpty() })
      verify(mockPatientPhoneNumberDao, never()).save(any())
    }
  }
}
