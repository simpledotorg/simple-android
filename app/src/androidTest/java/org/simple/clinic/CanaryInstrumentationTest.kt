package org.simple.clinic

import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CanaryInstrumentationTest {

  @Test
  fun testEnvironmentWorks() {
    val context = InstrumentationRegistry.getTargetContext()

    assertThat(context).isNotNull()
  }
}
