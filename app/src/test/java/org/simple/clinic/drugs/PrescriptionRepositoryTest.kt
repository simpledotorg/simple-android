package org.simple.clinic.drugs

import com.nhaarman.mockito_kotlin.argThat
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.AppDatabase
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUtcClock
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class PrescriptionRepositoryTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val database = mock<AppDatabase>()
  private val dao = mock<PrescribedDrug.RoomDao>()
  private val testClock = TestUtcClock()

  private val repository = PrescriptionRepository(database, dao, testClock)

  @Test
  @Parameters(value = [
    "PENDING, false",
    "INVALID, true",
    "DONE, true"])
  fun `when merging prescriptions with server records, ignore records that already exist locally and are syncing or pending-sync`(
      syncStatusOfLocalCopy: SyncStatus,
      serverRecordExpectedToBeSaved: Boolean
  ) {
    val prescriptionUuid = UUID.randomUUID()

    val localCopy = PatientMocker.prescription(prescriptionUuid, syncStatus = syncStatusOfLocalCopy)
    whenever(dao.getOne(prescriptionUuid)).thenReturn(localCopy)

    val serverBp = PatientMocker.prescription(prescriptionUuid, syncStatus = SyncStatus.DONE).toPayload()
    repository.mergeWithLocalData(listOf(serverBp)).blockingAwait()

    if (serverRecordExpectedToBeSaved) {
      verify(dao).save(argThat { isNotEmpty() })
    } else {
      verify(dao).save(argThat { isEmpty() })
    }
  }
}
