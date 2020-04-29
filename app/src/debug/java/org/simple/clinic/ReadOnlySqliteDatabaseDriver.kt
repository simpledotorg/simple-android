package org.simple.clinic

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.facebook.flipper.plugins.databases.impl.SqliteDatabaseConnectionProvider
import com.facebook.flipper.plugins.databases.impl.SqliteDatabaseDriver
import com.facebook.flipper.plugins.databases.impl.SqliteDatabaseProvider
import java.io.File

class ReadOnlySqliteDatabaseDriver(
    context: Context
) : SqliteDatabaseDriver(
    context,
    ContextBasedDatabaseProvider(context),
    ReadOnlySqliteDatabaseConnectionProvider()
)

private class ContextBasedDatabaseProvider(private val context: Context) : SqliteDatabaseProvider {

  override fun getDatabaseFiles(): MutableList<File> {
    return context
        .databaseList()
        .map(context::getDatabasePath)
        .toMutableList()
  }
}

private class ReadOnlySqliteDatabaseConnectionProvider : SqliteDatabaseConnectionProvider {

  override fun openDatabase(databaseFile: File): SQLiteDatabase {
    // We are setting this to READONLY for a couple of reasons:
    //
    // Room sets all databases to WAL by default, which means that they need to
    // share a connection to the underlying database via Room's SQLiteOpenHelper.
    //
    // However, the Flipper database plugin tries to open and manage its own
    // connection to the database. This causes weird problems like writes to the
    // database to fail silently.
    //
    // We could potentially write a custom connection provider for Flipper which
    // would let us use the open helper to create the connection, but the problem
    // is that the interface expects the `android.database.sqlite.SQLiteDatabase`
    // class, while Room uses the `android.arch.persistence.db.SupportSQLiteDatabase`
    // class.
    //
    // This is a temporary workaround until we can implement a new database driver
    // for Flipper which is aware of this alternate class.
    // TODO(vs): 2020-01-01 Implement RoomDatabaseDriver for Flipper

    val flags = SQLiteDatabase.OPEN_READONLY

    return SQLiteDatabase.openDatabase(databaseFile.absolutePath, null, flags)
  }
}
