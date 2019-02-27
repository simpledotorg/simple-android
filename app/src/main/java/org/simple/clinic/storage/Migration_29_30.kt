package org.simple.clinic.storage

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.f2prateek.rx.preferences2.Preference
import org.simple.clinic.ClinicApp
import org.simple.clinic.util.Optional
import javax.inject.Inject
import javax.inject.Named

@Suppress("ClassName")
class Migration_29_30 : Migration(29, 30) {

  @field:[Inject Named("last_patient_pull_token")]
  lateinit var lastPatientPullToken: Preference<Optional<String>>

  override fun migrate(database: SupportSQLiteDatabase) {
    ClinicApp.appComponent.inject(this)
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
