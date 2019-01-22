package org.simple.clinic.storage.files

import android.app.Application
import androidx.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
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

  @Inject
  lateinit var fileStorage: FileStorage

  lateinit var testDirectory: File

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
    testDirectory = application.filesDir.resolve("test_dir")
    testDirectory.deleteRecursively()
    assertThat(testDirectory.mkdirs()).isTrue()
  }

  @Test
  fun getting_a_file_at_non_existent_path_must_create_it() {
    val path = testDirectory.resolve("foo/bar/file_1.txt").path

    val result = fileStorage.getFile(path) as GetFileResult.Success

    result.file.let { file ->
      assertThat(file.exists()).isTrue()
      assertThat(file.isFile).isTrue()
      assertThat(file.parentFile.name).isEqualTo("bar")
      assertThat(file.parentFile.parentFile.name).isEqualTo("foo")
      assertThat(file.parentFile.parentFile.parentFile).isEqualTo(testDirectory)
    }
  }

  @Test
  fun getting_an_already_existing_file_must_not_overwrite_it() {
    val text = "Some text that is already present"

    val alreadyExistingFile = testDirectory.resolve("file.txt")
    alreadyExistingFile.writeText(text)
    assertThat(alreadyExistingFile.readText()).isEqualTo(text)

    val result = fileStorage.getFile(alreadyExistingFile.path) as GetFileResult.Success
    assertThat(result.file.readText()).isEqualTo(text)
  }

  @Test
  fun getting_a_non_existing_file_at_an_existing_path_must_create_it() {
    val path = testDirectory.resolve("file_1.txt").path

    val result = fileStorage.getFile(path) as GetFileResult.Success

    result.file.let { file ->
      assertThat(file.exists()).isTrue()
      assertThat(file.isFile).isTrue()
      assertThat(file.parentFile.name).isEqualTo(testDirectory.name)
    }
  }

  @Test
  fun getting_a_writable_file_that_points_to_a_directory_should_return_not_a_file_result() {
    val path = testDirectory.name
    val result = fileStorage.getFile(path)

    assertThat(result).isEqualTo(GetFileResult.NotAFile(path))
  }

  @Test
  fun writing_to_a_file_must_work_as_expected() {
    val file = testDirectory.resolve("file.txt")
    file.createNewFile()

    val text = "Some text data"
    val result = fileStorage.writeToFile(file, text)

    assertThat(result).isEqualTo(WriteFileResult.Success(file))
    assertThat(file.readText()).isEqualTo(text)
  }

  @Test
  fun reading_from_a_file_must_work_as_expected() {
    val file = testDirectory.resolve("file.txt")
    file.createNewFile()
    val text = "Draco Dormiens Nunquam Titillandus"
    file.writeText(text)

    val result = fileStorage.readFromFile(file)

    assertThat(result).isEqualTo(ReadFileResult.Success(content = text))
  }
}
