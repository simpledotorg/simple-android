package org.simple.clinic.drugs

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.argThat
import com.nhaarman.mockito_kotlin.check
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.AppDatabase
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.user.UserSession
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class PrescriptionRepositoryTest {

  private val database = mock<AppDatabase>()
  private val dao = mock<PrescribedDrug.RoomDao>()
  private val facilityRepository = mock<FacilityRepository>()
  private val userSession = mock<UserSession>()

  private lateinit var repository: PrescriptionRepository

  @Before
  fun setUp() {
    repository = PrescriptionRepository(database, dao, facilityRepository, userSession)
  }

  @Test
  fun `when saving a prescription, correctly get the current facility ID`() {
    val facility = PatientMocker.facility()
    whenever(facilityRepository.currentFacility(userSession)).thenReturn(Observable.just(facility))

    val patientUuid = UUID.randomUUID()
    repository
        .savePrescription(patientUuid, name = "Drug name", dosage = "dosage", rxNormCode = "rx-norm-code", isProtocolDrug = true)
        .subscribe()

    verify(dao).save(check {
      assertThat(it.first().name).isEqualTo("Drug name")
      assertThat(it.first().dosage).isEqualTo("dosage")
      assertThat(it.first().rxNormCode).isEqualTo("rx-norm-code")
      assertThat(it.first().facilityUuid).isEqualTo(facility.uuid)
      assertThat(it.first().patientUuid).isEqualTo(patientUuid)
    })
  }

  @Test
  @Parameters(value = [
    "PENDING, false",
    "IN_FLIGHT, false",
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
