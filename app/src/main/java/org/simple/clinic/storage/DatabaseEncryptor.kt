package org.simple.clinic.storage

import android.app.Application
import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import androidx.core.content.edit
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteStatement
import org.simple.clinic.di.AppScope
import org.simple.clinic.storage.DatabaseEncryptor.State.ENCRYPTED
import org.simple.clinic.storage.SharedPreferencesMode.Mode.Encrypted
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import javax.crypto.KeyGenerator
import javax.inject.Inject

@AppScope
class DatabaseEncryptor @Inject constructor(
    private val appContext: Application,
    @SharedPreferencesMode(Encrypted) private val sharedPreferences: SharedPreferences
) {

  companion object {
    private const val PASSPHRASE_PREF_KEY = "simple_passphrase"
  }

  enum class State {
    DOES_NOT_EXIST, UNENCRYPTED, ENCRYPTED
  }

  val passphrase: ByteArray get() = getSecurePassphrase() ?: generateSecurePassphrase()

  private val databaseEncryptionState = BehaviorSubject.create<State>()
  val isDatabaseEncrypted: Observable<Boolean> = databaseEncryptionState
      .map { it == ENCRYPTED }
      .distinctUntilChanged()

  init {
    SQLiteDatabase.loadLibs(appContext)
  }

  fun execute(databaseName: String) {
    val databaseState = databaseState(databaseName)
    databaseEncryptionState.onNext(databaseState)

    if (databaseState == State.UNENCRYPTED) {
      encrypt(databaseName)
      databaseEncryptionState.onNext(ENCRYPTED)
    }
  }

  /**
   * Replaces this database with a version encrypted with the supplied
   * passphrase, deleting the original. Do not call this while the database
   * is open, which includes during any Room migrations.
   *
   * The passphrase is untouched in this call. If you are going to turn around
   * and use it with SafeHelperFactory.fromUser(), fromUser() will clear the
   * passphrase. If not, please set all bytes of the passphrase to 0 or something
   * to clear out the passphrase.
   *
   * @param databasePath: a File pointing to the database
   * @param passphrase the passphrase from the user
   * @throws IOException
   *
   * copied from: https://github.com/commonsguy/cwac-saferoom/blob/07c6503e8bcff4d547575e4a0a846c67222c7caf/saferoom/src/main/java/com/commonsware/cwac/saferoom/SQLCipherUtils.java#L190
   */
  @Throws(IOException::class)
  private fun encrypt(databaseName: String) {
    val databasePath = appContext.getDatabasePath(databaseName)
    if (databasePath.exists()) {
      val newFile = File.createTempFile("simple_database_encryption", "tmp", appContext.cacheDir)
      var db = SQLiteDatabase.openDatabase(databasePath.absolutePath, "", null, SQLiteDatabase.OPEN_READWRITE)
      val version = db.version

      db.close()
      db = SQLiteDatabase.openDatabase(newFile.absolutePath, passphrase, null, SQLiteDatabase.OPEN_READWRITE, null, null)

      val st: SQLiteStatement = db.compileStatement("ATTACH DATABASE ? AS plaintext KEY ''")
      st.bindString(1, databasePath.absolutePath)
      st.execute()

      db.rawExecSQL("SELECT sqlcipher_export('main', 'plaintext')")
      db.rawExecSQL("DETACH DATABASE plaintext")
      db.version = version

      st.close()
      db.close()

      databasePath.delete()
      newFile.renameTo(databasePath)
    } else {
      throw FileNotFoundException(databasePath.absolutePath + " not found")
    }
  }

  /**
   * Determine whether or not this database appears to be encrypted, based
   * on whether we can open it without a passphrase.
   *
   * @param databasePath: a File pointing to the database
   * @return the detected state of the database
   *
   * copied from: https://github.com/commonsguy/cwac-saferoom/blob/07c6503e8bcff4d547575e4a0a846c67222c7caf/saferoom/src/main/java/com/commonsware/cwac/saferoom/SQLCipherUtils.java#L62
   *
   */
  @VisibleForTesting
  fun databaseState(databaseName: String): State {
    val databasePath = appContext.getDatabasePath(databaseName)
    if (databasePath.exists()) {
      var db: SQLiteDatabase? = null
      return try {
        db = SQLiteDatabase.openDatabase(databasePath.absolutePath, "",
            null, SQLiteDatabase.OPEN_READONLY)
        db.version
        State.UNENCRYPTED
      } catch (e: Exception) {
        ENCRYPTED
      } finally {
        db?.close()
      }
    }
    return State.DOES_NOT_EXIST
  }

  private fun getSecurePassphrase(): ByteArray? {
    return sharedPreferences.getString(PASSPHRASE_PREF_KEY, null)?.toByteArray(Charsets.ISO_8859_1)
  }

  private fun generateSecurePassphrase(): ByteArray {
    val keyGenerator = KeyGenerator.getInstance("AES").apply {
      init(256)
    }
    val passphrase = keyGenerator.generateKey().encoded

    sharedPreferences.edit(commit = true) {
      putString(PASSPHRASE_PREF_KEY, passphrase.toString(Charsets.ISO_8859_1))
    }

    return passphrase
  }
}
