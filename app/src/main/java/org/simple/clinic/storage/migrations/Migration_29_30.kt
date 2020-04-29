package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.f2prateek.rx.preferences2.Preference
import org.simple.clinic.storage.inTransaction
import org.simple.clinic.util.Optional
import javax.inject.Inject
import javax.inject.Named

@Suppress("ClassName")
class Migration_29_30 @Inject constructor(
    @Named("last_patient_pull_token")
    private val lastPatientPullToken: Preference<Optional<String>>
) : Migration(29, 30) {

  override fun migrate(database: SupportSQLiteDatabase) {
    lastPatientPullToken.delete()

    database.inTransaction {
      val tableName = "BusinessId"

      database.execSQL("""
        CREATE TABLE IF NOT EXISTS "$tableName" (
          "uuid" TEXT NOT NULL,
          "patientUuid" TEXT NOT NULL,
          "identifier" TEXT NOT NULL,
          "identifierType" TEXT NOT NULL,
          "metaVersion" TEXT NOT NULL,
          "meta" TEXT NOT NULL,
          "createdAt" TEXT NOT NULL,
          "updatedAt" TEXT NOT NULL,
          "deletedAt" TEXT,
          PRIMARY KEY("uuid"),
          FOREIGN KEY("patientUuid") REFERENCES "Patient"("uuid") ON UPDATE NO ACTION ON DELETE CASCADE)
        """)

      database.execSQL("""
        CREATE INDEX "index_BusinessId_patientUuid" ON "$tableName" ("patientUuid")
      """)

      database.execSQL("""
        CREATE INDEX "index_BusinessId_identifier" ON "$tableName" ("identifier")
      """)
    }
  }
}
