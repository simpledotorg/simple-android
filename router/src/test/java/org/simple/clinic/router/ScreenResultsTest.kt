package org.simple.clinic.router

import com.google.common.truth.Truth.assertThat
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.router.screen.ScreenResults

@RunWith(JUnitParamsRunner::class)
class ScreenResultsTest {

  val screenResults = ScreenResults()

  @Test
  @Parameters(method = "params for saving results")
  fun `saving a result with a key must work as expected`(
      key: String,
      value: Any?
  ) {
    assertThat(screenResults.consume(key)).isNull()
    screenResults.put(key, value)

    val saved: Any? = screenResults.consume(key)

    assertThat(saved).isEqualTo(value)
  }

  @Suppress("Unsed")
  fun `params for saving results`(): List<List<Any?>> {
    return listOf(
        listOf("key1", null),
        listOf("key2", 2),
        listOf("key3", "asd"),
        listOf("key4", TestType1(-5, false)),
        listOf("key5", TestType2(
            listOf(1, 2, 3),
            mapOf(
                "one" to true,
                "two" to false,
                "three" to true
            )))
    )
  }

  @Test
  @Parameters(method = "params for saving results multiple times")
  fun `saving a result with the same key must replace it`(
      key: String,
      value1: Any?,
      value2: Any?
  ) {
    assertThat(screenResults.consume(key)).isNull()
    screenResults.put(key, value1)
    screenResults.put(key, value2)

    val saved: Any? = screenResults.consume(key)
    assertThat(saved).isEqualTo(value2)
  }

  @Suppress("Unused")
  fun `params for saving results multiple times`(): List<List<Any>> {
    return listOf(
        listOf("key1", -1, 5),
        listOf("key2", "one", "three"),
        listOf("key3", TestType1(5, false), TestType1(10, true))
    )
  }

  @Test
  @Parameters(method = "params for clearing results")
  fun `a result must be cleared after it is retrieved`(
      key: String,
      value: Any?
  ) {
    val oldKey1 = "old key 1"
    val oldKey2 = "old key 2"

    screenResults.put(oldKey1, "already present result 1")
    screenResults.put(oldKey2, "already present result 2")
    screenResults.put(key, value)
    assertThat(screenResults.keys()).isEqualTo(setOf(oldKey1, oldKey2, key))

    screenResults.consume(key)
    assertThat(screenResults.keys()).isEqualTo(setOf(oldKey1, oldKey2))
    screenResults.consume(oldKey2)
    assertThat(screenResults.keys()).isEqualTo(setOf(oldKey1))
  }

  @Suppress("Unsed")
  fun `params for clearing results`(): List<List<Any?>> {
    return listOf(
        listOf("key1", null),
        listOf("key2", 2),
        listOf("key3", "asd"),
        listOf("key4", TestType1(-5, false)),
        listOf("key5", TestType2(
            listOf(1, 2, 3),
            mapOf(
                "one" to true,
                "two" to false,
                "three" to true
            )))
    )
  }

  data class TestType1(val property1: Int, val property2: Boolean)
  data class TestType2(val property1: List<Int>, val property2: Map<String, Boolean>)
}
