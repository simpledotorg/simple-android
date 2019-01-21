package org.simple.clinic.storage.files

import android.app.Application
import androidx.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.TestClinicApp
import java.io.File
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class AndroidFileStorageAndroidTest {

  @Inject
  lateinit var application: Application

  lateinit var testDirectory: File

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
    testDirectory = application.filesDir.resolve("test_dir")
    assertThat(testDirectory.mkdirs()).isTrue()
  }

  @After
  fun tearDown() {
    assertThat(testDirectory.deleteRecursively()).isTrue()
  }

  @Test
  fun test() {

  }
}
