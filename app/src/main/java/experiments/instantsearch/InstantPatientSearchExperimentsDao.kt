package experiments.instantsearch

import android.database.Cursor
import androidx.sqlite.db.SupportSQLiteDatabase
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import org.simple.clinic.AppDatabase
import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.patient.PatientStatus
import java.util.UUID

class InstantPatientSearchExperimentsDao(private val appDatabase: AppDatabase) : PatientSearchResult.RoomDao {

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

  fun namePhoneNumber(): Observable<List<PatientNamePhoneNumber>> {
    return Observable.fromCallable { appDatabase.openHelper.readableDatabase }
        .map(this::loadPatientNamePhoneNumberFromDb)
  }

  private fun loadPatientNamePhoneNumberFromDb(database: SupportSQLiteDatabase): List<PatientNamePhoneNumber> {
    val sql = """
      |SELECT P."uuid", P."fullName", PPN."number" 
      |FROM "Patient" P
      |LEFT JOIN "PatientPhoneNumber" PPN ON (P."uuid" = PPN."patientUuid" AND PPN."deletedAt" IS NULL)
      |WHERE P."status" = 'active' AND P."deletedAt" IS NULL
    """.trimMargin()

    return database.query(sql)
        .use { cursor ->
          generateSequence { cursor.takeIf { it.moveToNext() } }
              .map(::PatientNamePhoneNumber)
              .toList()
        }
  }

  data class PatientNamePhoneNumber(val patientUuid: UUID, val patientName: String, val patientPhoneNumber: String?) {

    constructor(cursor: Cursor) : this(
        patientUuid = UUID.fromString(cursor.getString(0)),
        patientName = cursor.getString(1),
        patientPhoneNumber = cursor.getString(2)
    )
  }
}
