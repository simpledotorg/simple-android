package org.simple.clinic.help

import androidx.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.simple.clinic.AuthenticationRule
import org.simple.clinic.TestClinicApp
import org.simple.clinic.util.None
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.unwrapJust
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class HelpSyncAndroidTest {

  @Inject
  lateinit var helpRepository: HelpRepository

  @Inject
  lateinit var helpSync: HelpSync

  @get:Rule
  val ruleChain = RuleChain
      .outerRule(AuthenticationRule())
      .around(RxErrorsRule())!!

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
  }

  @After
  fun tearDown() {
    val (helpFile) = helpRepository.helpFile().blockingFirst()
    helpFile?.delete()
  }

  @Test
  fun when_pulling_help_from_the_server_it_should_save_the_content_as_a_file() {
    val helpFileBeforeSync = helpRepository.helpFile().blockingFirst()

    helpSync
        .pull()
        .test()
        .assertNoErrors()

    val helpFile = helpRepository
        .helpFile()
        .unwrapJust()
        .blockingFirst()

    assertThat(helpFileBeforeSync).isSameAs(None)
    assertThat(helpFile.length()).isGreaterThan(0L)
  }
}
