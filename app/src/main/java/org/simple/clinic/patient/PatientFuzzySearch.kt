package org.simple.clinic.patient

import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteOpenHelper
import io.reactivex.Single
import io.reactivex.SingleTransformer
import java.util.UUID

class PatientFuzzySearch {

  data class UuidToScore(val uuid: UUID, val score: Int)

  // TODO: See if this can merged with the PatientSearchDao.
  interface PatientFuzzySearchDao {

    fun searchForPatientsWithNameLike(query: String): Single<List<PatientSearchResult>>
  }

  class PatientFuzzySearchDaoImpl(
      private val sqLiteOpenHelper: SupportSQLiteOpenHelper,
      private val patientSearchDao: PatientSearchResult.RoomDao
  ) : PatientFuzzySearchDao {

    override fun searchForPatientsWithNameLike(query: String): Single<List<PatientSearchResult>> {
      return patientUuidsMatching(query).flatMap { uuidsSortedByScore ->
        val uuids = uuidsSortedByScore.map { it.uuid }
        patientSearchDao
            .searchByIds(uuids, PatientStatus.Active)
            .compose(sortPatientSearchResultsByScore(uuidsSortedByScore))
      }
    }

    private fun patientUuidsMatching(query: String): Single<List<UuidToScore>> {
      return Single.fromCallable {
        val searchQuery = SimpleSQLiteQuery("""
          SELECT "Patient"."uuid", editdist3('$query', "Patient"."searchableName") "score"
          FROM "Patient" WHERE "score" < 750
          ORDER BY "score" LIMIT 100
        """.trimIndent())

        sqLiteOpenHelper.readableDatabase.query(searchQuery)
            .use { cursor ->
              val uuidIndex = cursor.getColumnIndex("uuid")
              val scoreIndex = cursor.getColumnIndex("score")

              generateSequence { cursor.takeIf { it.moveToNext() } }
                  .map { UuidToScore(UUID.fromString(it.getString(uuidIndex)), it.getInt(scoreIndex)) }
                  .toList()
            }
      }
    }

    private fun sortPatientSearchResultsByScore(uuidsSortedByScore: List<UuidToScore>) =
        SingleTransformer<List<PatientSearchResult>, List<PatientSearchResult>> { upstream ->
          upstream
              .map { results ->
                val resultsByUuid = results.associateBy { it.uuid }
                uuidsSortedByScore
                    .filter { it.uuid in resultsByUuid }
                    .map { resultsByUuid[it.uuid]!! }
              }
        }
  }
}
