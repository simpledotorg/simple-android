package experiments.instantsearch

import io.reactivex.Flowable
import io.reactivex.Single
import org.simple.clinic.AppDatabase
import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.patient.PatientStatus
import java.util.UUID

class InstantPatientSearchExperimentsDao(private val appDatabase: AppDatabase): PatientSearchResult.RoomDao {

  private val wrappedDao = appDatabase.patientSearchDao()

  override fun searchByIds(uuids: List<UUID>, status: PatientStatus): Single<List<PatientSearchResult>> {
    return wrappedDao.searchByIds(uuids, status)
  }

  override fun nameAndId(status: PatientStatus): Flowable<List<PatientSearchResult.PatientNameAndId>> {
    return wrappedDao.nameAndId(status)
  }

  override fun searchInFacilityAndSortByName(facilityUuid: UUID, status: PatientStatus): Flowable<List<PatientSearchResult>> {
    return wrappedDao.searchInFacilityAndSortByName(facilityUuid, status)
  }

  override fun searchByPhoneNumber(phoneNumber: String): Flowable<List<PatientSearchResult>> {
    return wrappedDao.searchByPhoneNumber(phoneNumber)
  }
}
