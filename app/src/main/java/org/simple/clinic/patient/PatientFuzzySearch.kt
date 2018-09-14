package org.simple.clinic.patient

import android.arch.persistence.db.SimpleSQLiteQuery
import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.db.SupportSQLiteOpenHelper
import android.database.Cursor
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.SingleTransformer
import java.util.UUID

class PatientFuzzySearch {

  companion object {

    // TODO: I am really unhappy about putting this here, but I can't figure out where else to place it.
    //
    // This needs to be accessible both from the AppDatabase builder (since we can't create this table
    // using the Room classes and requires running a specific SQL query) as well as the migration.
    //
    // At the same time, this is very specific to the database and should not be exposed to the rest
    // of the system as a top-level function.
    @JvmStatic
    fun createTable(database: SupportSQLiteDatabase) {
      database.execSQL("""CREATE VIRTUAL TABLE "PatientFuzzySearch" USING spellfix1""")
    }

    @JvmStatic
    fun clearTable(database: SupportSQLiteDatabase) {
      database.execSQL("""DELETE FROM "PatientFuzzySearch"""")
    }
  }

  data class FuzzySearchResult(
      val rowId: Long,
      val uuid: UUID,
      val word: String,
      val rank: Int,
      val distance: Int,
      val score: Int,
      val matchLength: Int,
      val scope: Int
  )

  data class UuidToScore(val uuid: UUID, val score: Int)

  interface PatientFuzzySearchDao {

    // Used for testing
    fun savedEntries(): Single<List<FuzzySearchResult>>

    // Used for testing
    fun getEntriesForPatientIds(uuids: List<UUID>): Single<List<FuzzySearchResult>>

    fun updateTableForPatients(uuids: List<UUID>): Completable

    fun searchForPatientsWithNameLike(query: String): Single<List<PatientSearchResult>>

    fun searchForPatientsWithNameLikeAndAgeWithin(query: String, dobUpperBound: String, dobLowerBound: String): Single<List<PatientSearchResult>>

    fun clearAll()

    fun count(): Int
  }

  class PatientFuzzySearchDaoImpl(
      private val sqLiteOpenHelper: SupportSQLiteOpenHelper,
      private val patientSearchDao: PatientSearchResult.RoomDao
  ) : PatientFuzzySearchDao {

    private val fuzzyColumnsToFetch = """
      "P"."uuid" "uuid", "PFS"."rowid" "rowid","PFS"."word" "word","PFS"."rank" "rank","PFS"."distance" "distance","PFS"."score" "score","PFS"."matchlen" "matchlen","PFS"."scope" "scope"
    """.trimIndent()

    private fun resultsFromCursor(cursor: Cursor) =
        cursor.use {
          val uuid = it.getColumnIndex("uuid")
          val rowId = it.getColumnIndex("rowid")
          val word = it.getColumnIndex("word")
          val rank = it.getColumnIndex("rank")
          val distance = it.getColumnIndex("distance")
          val score = it.getColumnIndex("score")
          val matchLength = it.getColumnIndex("matchlen")
          val scope = it.getColumnIndex("scope")
          generateSequence { it.takeIf { it.moveToNext() } }
              .map {
                FuzzySearchResult(
                    rowId = it.getLong(rowId),
                    uuid = UUID.fromString(it.getString(uuid)),
                    word = it.getString(word),
                    rank = it.getInt(rank),
                    distance = it.getInt(distance),
                    score = it.getInt(score),
                    matchLength = it.getInt(matchLength),
                    scope = it.getInt(scope)
                )
              }.toList()
        }

    override fun savedEntries() = Single.fromCallable {
      sqLiteOpenHelper.readableDatabase.query("""
          SELECT $fuzzyColumnsToFetch FROM "PatientFuzzySearch" "PFS"
          INNER JOIN "Patient" "P" ON "P"."rowid"="PFS"."rowid"
        """.trimIndent()).use { resultsFromCursor(it) }
    }!!

    override fun getEntriesForPatientIds(uuids: List<UUID>) = Single.fromCallable {
      sqLiteOpenHelper.readableDatabase.query("""
          SELECT $fuzzyColumnsToFetch FROM "PatientFuzzySearch" "PFS"
          INNER JOIN "Patient" "P" ON "P"."rowid"="PFS"."rowid" WHERE "uuid" IN (${uuids.joinToString(",", transform = { "'$it'" })})
        """.trimIndent()).use { resultsFromCursor(it) }
    }!!

    override fun updateTableForPatients(uuids: List<UUID>) =
        Completable.fromAction {
          sqLiteOpenHelper.writableDatabase.execSQL("""
            INSERT OR IGNORE INTO "PatientFuzzySearch" ("rowid","word")
            SELECT "rowid","searchableName" FROM "Patient" WHERE "uuid" in (${uuids.joinToString(",", transform = { "'$it'" })})
            """.trimIndent())
        }!!

    override fun count(): Int {
      return sqLiteOpenHelper.readableDatabase.query("""
        SELECT COUNT("rowid") FROM "PatientFuzzySearch"
      """.trimIndent()).use {
        it.moveToFirst()
        it.getInt(0)
      }
    }

    private fun patientUuidsMatching(query: String) =
        Single.fromCallable {
          val searchQuery = SimpleSQLiteQuery("""
            SELECT "P"."uuid", "PFS"."score"
            FROM "Patient" "P" INNER JOIN "PatientFuzzySearch" "PFS"
            ON "P"."rowid"="PFS"."rowid" WHERE "PFS"."word" MATCH '$query' AND "top"=5
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

    override fun searchForPatientsWithNameLike(query: String) =
        patientUuidsMatching(query)
            .flatMap { uuidsSortedByScore ->
              val uuids = uuidsSortedByScore.map { it.uuid }
              patientSearchDao
                  .searchByIds(uuids)
                  .compose(sortPatientSearchResultsByScore(uuidsSortedByScore))
            }!!

    override fun searchForPatientsWithNameLikeAndAgeWithin(query: String, dobUpperBound: String, dobLowerBound: String) =
        patientUuidsMatching(query).flatMap { uuidsSortedByScore ->
          val uuids = uuidsSortedByScore.map { it.uuid }
          patientSearchDao
              .searchByIds(uuids, dobUpperBound, dobLowerBound)
              .compose(sortPatientSearchResultsByScore(uuidsSortedByScore))
        }!!

    override fun clearAll() {
      sqLiteOpenHelper.writableDatabase.execSQL("""DELETE FROM "PatientFuzzySearch"""")
    }
  }
}
