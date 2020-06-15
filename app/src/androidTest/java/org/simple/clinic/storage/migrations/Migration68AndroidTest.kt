package org.simple.clinic.storage.migrations

import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.simple.clinic.assertTableDoesNotExist
import org.simple.clinic.assertValues
import java.io.File

class Migration68AndroidTest : BaseDatabaseMigrationTest(67, 68) {

  private lateinit var reportsFile: File

  private lateinit var helpFile: File

  @Before
  override fun setUp() {
    super.setUp()
    InstrumentationRegistry.getInstrumentation().targetContext.filesDir.let { files ->
      reportsFile = files.resolve("report.html")
      helpFile = files.resolve("help.html")
    }
  }

  @After
  fun tearDown() {
    reportsFile.delete()
    helpFile.delete()
  }

  @Test
  fun it_should_save_the_reports_and_help_file_content_into_the_table() {
    // given
    val reportsText = """
      Day 1: 23 patients
      Day 2: 45 patients
    """
    val helpText = """
      Help me Obi-Wan Kenobi,
      You're my only hope!
    """

    reportsFile.ensureExists()
    helpFile.ensureExists()

    reportsFile.writeText(reportsText)
    helpFile.writeText(helpText)

    assertThat(reportsFile.readText()).isEqualTo(reportsText)
    assertThat(helpFile.readText()).isEqualTo(helpText)

    before.assertTableDoesNotExist("TextRecords")

    // then
    after
        .query(""" SELECT * FROM "TextRecords" """)
        .use { cursor ->
          assertThat(cursor.count).isEqualTo(2)

          cursor.moveToNext()
          cursor.assertValues(mapOf(
              "id" to "reports",
              "text" to reportsText
          ))

          cursor.moveToNext()
          cursor.assertValues(mapOf(
              "id" to "help",
              "text" to helpText
          ))
        }

    assertThat(reportsFile.exists()).isFalse()
    assertThat(helpFile.exists()).isFalse()
  }

  @Test
  fun it_should_not_fail_if_the_files_do_not_exist() {
    // given
    assertThat(reportsFile.exists()).isFalse()
    assertThat(helpFile.exists()).isFalse()

    before.assertTableDoesNotExist("TextRecords")

    // then
    after
        .query(""" SELECT * FROM "TextRecords" """)
        .use { cursor ->
          assertThat(cursor.count).isEqualTo(0)
        }

    assertThat(reportsFile.exists()).isFalse()
    assertThat(helpFile.exists()).isFalse()
  }

}

private fun File.ensureExists() {
  if (exists() && !isFile) throw RuntimeException("Cannot create the file!")

  if (!exists()) {
    parentFile!!.mkdirs()
    createNewFile()
  }
}
