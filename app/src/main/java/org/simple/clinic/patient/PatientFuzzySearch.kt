package org.simple.clinic.patient

import android.arch.persistence.db.SimpleSQLiteQuery
import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.db.SupportSQLiteOpenHelper
import io.reactivex.Completable
import io.reactivex.Single
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
      val word: String
  )

  interface PatientFuzzySearchDao {

    // Used for testing
    fun savedEntries(): Single<List<FuzzySearchResult>>

    // Used for testing
    fun getEntriesForPatientIds(uuids: List<UUID>): Single<List<FuzzySearchResult>>

    fun updateTableForPatients(uuids: List<UUID>): Completable

    fun searchForPatientsWithNameLike(query: String): Single<List<PatientSearchResult>>

    fun searchForPatientsWithNameLikeAndAgeWithin(query: String, dobUpperBound: String, dobLowerBound: String): Single<List<PatientSearchResult>>
  }

  class PatientFuzzySearchDaoImpl(
      private val sqLiteOpenHelper: SupportSQLiteOpenHelper,
      private val patientSearchDao: PatientSearchResult.RoomDao
  ) : PatientFuzzySearchDao {

    override fun savedEntries() = Single.fromCallable {
      sqLiteOpenHelper.readableDatabase.query("""
          SELECT "PFS"."rowid" "rowid","PFS"."word" "word","P"."uuid" "uuid" FROM "PatientFuzzySearch" "PFS"
          INNER JOIN "Patient" "P" ON "P"."rowid"="PFS"."rowid"
        """.trimIndent())
          .use {
            val rowIdIndex = it.getColumnIndex("rowid")
            val wordIndex = it.getColumnIndex("word")
            val uuidIndex = it.getColumnIndex("uuid")

            generateSequence { it.takeIf { it.moveToNext() } }
                .map { FuzzySearchResult(it.getLong(rowIdIndex), UUID.fromString(it.getString(uuidIndex)), it.getString(wordIndex)) }
                .toList()
          }
    }!!

    override fun getEntriesForPatientIds(uuids: List<UUID>) = Single.fromCallable {
      sqLiteOpenHelper.readableDatabase.query("""
          SELECT "PFS"."rowid" "rowid","PFS"."word" "word","P"."uuid" "uuid" FROM "PatientFuzzySearch" "PFS"
          INNER JOIN "Patient" "P" ON "P"."rowid"="PFS"."rowid" WHERE "uuid" IN (${uuids.joinToString(",", transform = { "'$it'" })})
        """.trimIndent())
          .use {
            val rowIdIndex = it.getColumnIndex("rowid")
            val wordIndex = it.getColumnIndex("word")
            val uuidIndex = it.getColumnIndex("uuid")

            generateSequence { it.takeIf { it.moveToNext() } }
                .map { FuzzySearchResult(it.getLong(rowIdIndex), UUID.fromString(it.getString(uuidIndex)), it.getString(wordIndex)) }
                .toList()
          }
    }!!

    override fun updateTableForPatients(uuids: List<UUID>) =
        Completable.fromAction {
          sqLiteOpenHelper.writableDatabase.execSQL("""
            INSERT OR IGNORE INTO "PatientFuzzySearch" ("rowid","word")
            SELECT "rowid","searchableName" FROM "Patient" WHERE "uuid" in (${uuids.joinToString(",", transform = { "'$it'" })})
            """.trimIndent())
        }!!

    private fun patientUuidsMatching(query: String) =
        Single.fromCallable {
          val searchQuery = SimpleSQLiteQuery("""
            SELECT "P"."uuid" "uuid"
            FROM "Patient" "P" INNER JOIN "PatientFuzzySearch" "PFS"
              ON "P"."rowid"="PFS"."rowid" WHERE "PFS"."word" MATCH '$query*' AND "score" < 500 AND "top"=5 ORDER BY "score" ASC
            """.trimIndent())

          sqLiteOpenHelper.readableDatabase.query(searchQuery)
              .use { cursor ->
                val uuidColumnIndex = cursor.getColumnIndex("uuid")

                generateSequence { cursor.takeIf { it.moveToNext() } }
                    .map { UUID.fromString(it.getString(uuidColumnIndex)) }
                    .toList()
              }
        }

    override fun searchForPatientsWithNameLike(query: String) =
        patientUuidsMatching(query).flatMap { patientSearchDao.searchByIds(it) }!!

    override fun searchForPatientsWithNameLikeAndAgeWithin(query: String, dobUpperBound: String, dobLowerBound: String) =
        patientUuidsMatching(query).flatMap { patientSearchDao.searchByIds(it, dobUpperBound, dobLowerBound) }!!
  }
}
