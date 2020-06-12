package org.simple.clinic.help

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.simple.clinic.TestClinicApp
import org.simple.clinic.help.HelpRepository.Companion.HELP_KEY
import org.simple.clinic.rules.LocalAuthenticationRule
import org.simple.clinic.storage.text.TextStore
import org.simple.clinic.util.Rules
import javax.inject.Inject


class HelpSyncAndroidTest {

  @Inject
  lateinit var helpRepository: HelpRepository

  @Inject
  lateinit var helpSync: HelpSync

  @Inject
  lateinit var textStore: TextStore

  @get:Rule
  val rules: RuleChain = Rules
      .global()
      .around(LocalAuthenticationRule())

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
  }

  @After
  fun tearDown() {
    textStore.delete(HELP_KEY)
  }

  @Test
  fun when_pulling_help_from_the_server_it_should_save_the_content_as_a_file() {
    textStore.delete(HELP_KEY)
    // Technically, it should be `null`. But due to an implementation
    // detail, `AndroidFileStorage` will end up creating an empty file
    // when `get()` is called, so it will never be `null`.
    // This will be fixed when we switch to a Room DAO.
    assertThat(textStore.get(HELP_KEY)).isEmpty()

    helpSync
        .pull()
        .test()
        .assertNoErrors()

    assertThat(textStore.get(HELP_KEY)).isNotEmpty()
  }
}
