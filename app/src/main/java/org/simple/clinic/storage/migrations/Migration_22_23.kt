package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import javax.inject.Inject

@Suppress("ClassName")
class Migration_22_23 @Inject constructor() : Migration(22, 23) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL("""
      ALTER TABLE "ProtocolDrug" RENAME TO "ProtocolDrug_v22"
    """)
    database.execSQL("""
      CREATE TABLE IF NOT EXISTS "ProtocolDrug" (
    "uuid" TEXT NOT NULL,
    "protocolUuid" TEXT NOT NULL,
    "name" TEXT NOT NULL,
    "rxNormCode" TEXT,
    "dosage" TEXT NOT NULL,
    "createdAt" TEXT NOT NULL,
    "updatedAt" TEXT NOT NULL,
    PRIMARY KEY("uuid"),
    FOREIGN KEY("protocolUuid") REFERENCES "Protocol"("uuid") ON DELETE CASCADE ON UPDATE NO ACTION
      )
    """)

    database.execSQL("""INSERT INTO "ProtocolDrug"("uuid", "protocolUuid", "name", "rxNormCode", "dosage", "createdAt", "updatedAt")
    SELECT "uuid", "protocolUuid", "name", "rxNormCode", "dosage", "createdAt","updatedAt" FROM "ProtocolDrug_v22"
    """)

    database.execSQL("""DROP TABLE "ProtocolDrug_v22" """)
    database.execSQL("""CREATE INDEX "index_ProtocolDrug_protocolUuid" ON "ProtocolDrug" ("protocolUuid")""")
  }
}
