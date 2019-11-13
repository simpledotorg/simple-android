package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import javax.inject.Inject

@Suppress("ClassName")
class Migration_21_22 @Inject constructor() : Migration(21, 22) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL("""
    CREATE TABLE IF NOT EXISTS "Protocol"(
    "uuid" TEXT NOT NULL,
    "name" TEXT NOT NULL,
    "followUpDays" INTEGER NOT NULL,
    "createdAt" TEXT NOT NULL,
    "updatedAt" TEXT NOT NULL,
    "syncStatus" TEXT NOT NULL,
    PRIMARY KEY("uuid")
    )
    """)

    database.execSQL("""
    CREATE TABLE IF NOT EXISTS "ProtocolDrug"(
    "uuid" TEXT NOT NULL,
    "protocolUuid" TEXT NOT NULL,
    "name" TEXT NOT NULL,
    "rxNormCode" TEXT,
    "dosage" TEXT NOT NULL,
    "createdAt" TEXT NOT NULL,
    "updatedAt" TEXT NOT NULL,
    "syncStatus" TEXT NOT NULL,
    PRIMARY KEY("uuid"),
    FOREIGN KEY("protocolUuid") REFERENCES "Protocol"("uuid") ON DELETE CASCADE ON UPDATE NO ACTION
    )
  """)

    database.execSQL("""CREATE INDEX "index_ProtocolDrug_protocolUuid" ON "ProtocolDrug" ("protocolUuid")""")
  }
}
