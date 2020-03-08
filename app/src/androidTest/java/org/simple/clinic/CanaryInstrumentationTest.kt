package org.simple.clinic

import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.simple.clinic.util.Rules

@RunWith(AndroidJUnit4::class)
class CanaryInstrumentationTest {

  @get:Rule
  val ruleChain: RuleChain = Rules.global()

  @Test
  fun testEnvironmentWorks() {
    val context = InstrumentationRegistry.getTargetContext()

    assertThat(context).isNotNull()
  }
}
