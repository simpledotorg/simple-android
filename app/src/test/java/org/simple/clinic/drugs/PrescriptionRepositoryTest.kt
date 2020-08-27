package org.simple.clinic.drugs

import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.AppDatabase
import org.simple.clinic.TestData
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

    val localCopy = TestData.prescription(prescriptionUuid, syncStatus = syncStatusOfLocalCopy)
    whenever(dao.getOne(prescriptionUuid)).thenReturn(localCopy)

    val serverBp = TestData.prescription(prescriptionUuid, syncStatus = SyncStatus.DONE).toPayload()
    repository.mergeWithLocalData(listOf(serverBp))

    if (serverRecordExpectedToBeSaved) {
      verify(dao).save(argThat { isNotEmpty() })
    } else {
      verify(dao).save(argThat { isEmpty() })
    }
  }
}
