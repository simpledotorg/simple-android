package org.simple.clinic.storage

import android.app.Application
import androidx.room.Room
import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Moshi
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.simple.clinic.AppDatabase
import org.simple.clinic.FakeMinimumMemoryChecker
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.questionnaire.component.BaseComponentData
import org.simple.clinic.storage.DatabaseEncryptor.State.DOES_NOT_EXIST
import org.simple.clinic.storage.DatabaseEncryptor.State.ENCRYPTED
import org.simple.clinic.storage.DatabaseEncryptor.State.SKIPPED
import org.simple.clinic.storage.DatabaseEncryptor.State.UNENCRYPTED
import org.simple.clinic.user.User
import org.simple.clinic.user.UserStatus
import org.simple.clinic.util.MinimumMemoryChecker
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class DatabaseEncryptorTest {

  @Inject
  lateinit var appContext: Application

  @Inject
  lateinit var databaseEncryptor: DatabaseEncryptor

  @Inject
  lateinit var moshi: Moshi

  @Inject
  lateinit var minimumMemoryChecker: MinimumMemoryChecker

  companion object {
    private const val DB_NAME = "simple_test.db"
  }

  @Before
  fun setup() {
    TestClinicApp.appComponent().inject(this)
    appContext.deleteDatabase(DB_NAME)
  }

  @After
  fun tearDown() {
    (minimumMemoryChecker as FakeMinimumMemoryChecker).hasMinMemory = true
  }

  @Test
  fun encrypting_existing_database_should_work_correctly() {
    // given
    val passphrase = databaseEncryptor.passphrase

    assertThat(databaseEncryptor.databaseState(DB_NAME)).isEqualTo(DOES_NOT_EXIST)

    val unencryptedDatabase = Room.databaseBuilder(appContext, AppDatabase::class.java, DB_NAME)
        .allowMainThreadQueries()
        .addTypeConverter(BaseComponentData.RoomTypeConverter(moshi))
        .build()

    val expectedUser = TestData.loggedInUser(
        uuid = UUID.fromString("7bf7e7bf-d105-45b1-977c-d678c4d0ce74"),
        name = "Ramesh Murthy",
        createdAt = Instant.parse("2018-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2018-01-01T00:00:00Z"),
        status = UserStatus.ApprovedForSyncing,
        loggedInStatus = User.LoggedInStatus.LOGGED_IN
    )

    unencryptedDatabase.userDao().createOrUpdate(expectedUser)

    val unencryptedUser = unencryptedDatabase.userDao().userImmediate()

    assertThat(unencryptedUser).isNotNull()
    assertThat(unencryptedUser).isEqualTo(expectedUser)

    unencryptedDatabase.close()

    assertThat(databaseEncryptor.databaseState(DB_NAME)).isEqualTo(UNENCRYPTED)

    // when
    databaseEncryptor.execute(databaseName = DB_NAME)

    // then
    assertThat(databaseEncryptor.databaseState(DB_NAME)).isEqualTo(ENCRYPTED)

    val encryptedDatabase = Room.databaseBuilder(appContext, AppDatabase::class.java, DB_NAME)
        .allowMainThreadQueries()
        .addTypeConverter(BaseComponentData.RoomTypeConverter(moshi))
        .openHelperFactory(SupportOpenHelperFactory(passphrase))
        .build()

    val encryptedUser = encryptedDatabase.userDao().userImmediate()

    assertThat(encryptedUser).isNotNull()
    assertThat(encryptedUser).isEqualTo(expectedUser)
  }

  @Test
  fun if_device_does_not_have_min_req_memory_then_skip_database_encryption() {
    // given
    (minimumMemoryChecker as FakeMinimumMemoryChecker).hasMinMemory = false

    // when
    databaseEncryptor.execute(databaseName = DB_NAME)

    // then
    assertThat(databaseEncryptor.databaseState(DB_NAME)).isEqualTo(SKIPPED)
  }

  @Test
  fun when_database_is_already_encrypted_it_should_do_nothing() {
    // given
    val passphrase = databaseEncryptor.passphrase
    val encryptedDatabase = Room.databaseBuilder(appContext, AppDatabase::class.java, DB_NAME)
        .allowMainThreadQueries()
        .addTypeConverter(BaseComponentData.RoomTypeConverter(moshi))
        .openHelperFactory(SupportOpenHelperFactory(passphrase))
        .build()

    val expectedUser = TestData.loggedInUser(uuid = UUID.randomUUID())
    encryptedDatabase.userDao().createOrUpdate(expectedUser)
    encryptedDatabase.close()

    assertThat(databaseEncryptor.databaseState(DB_NAME)).isEqualTo(ENCRYPTED)

    // when
    databaseEncryptor.execute(databaseName = DB_NAME)

    // then
    assertThat(databaseEncryptor.databaseState(DB_NAME)).isEqualTo(ENCRYPTED)

    val reOpenedEncryptedDb = Room.databaseBuilder(appContext, AppDatabase::class.java, DB_NAME)
        .allowMainThreadQueries()
        .addTypeConverter(BaseComponentData.RoomTypeConverter(moshi))
        .openHelperFactory(SupportOpenHelperFactory(passphrase))
        .build()

    val actualUser = reOpenedEncryptedDb.userDao().userImmediate()
    assertThat(actualUser).isEqualTo(expectedUser)
    reOpenedEncryptedDb.close()
  }

  @Test
  fun when_database_does_not_exist_it_should_do_nothing() {
    // given
    assertThat(databaseEncryptor.databaseState(DB_NAME)).isEqualTo(DOES_NOT_EXIST)

    // when
    databaseEncryptor.execute(databaseName = DB_NAME)

    // then
    assertThat(databaseEncryptor.databaseState(DB_NAME)).isEqualTo(DOES_NOT_EXIST)
  }
}
