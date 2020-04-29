package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import javax.inject.Inject

/**
 * An earlier migration, [Migration_49_50], had a bug where where BPs which were not deleted would
 * end up generating encounters where the deletedAt property was set to the string "null" instead of
 * SQL NULL value.
 *
 * We fixed that migration, but some users did end up running this migration and would have
 * encounters which should not be deleted, but still will not show up in the UI because the
 * deletedAt field is non-null.
 *
 * This migration exists to change the deletedAt property for those encounters to be an SQL NULL.
 **/
@Suppress("ClassName")
class Migration_51_52 @Inject constructor() : Migration(51, 52) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL(""" UPDATE "Encounter" SET "deletedAt" = NULL WHERE "deletedAt" = 'null' """)
  }
}
