package org.simple.clinic.util

import com.google.common.truth.Truth.assertThat
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.util.room.SafeEnumTypeAdapter

@RunWith(JUnitParamsRunner::class)
class SafeEnumTypeAdapterTest {

  sealed class TestType {
    object Value1 : TestType() {
      override fun toString() = "TestType:Value1"
    }

    object Value2 : TestType() {
      override fun toString() = "TestType:Value2"
    }

    data class Unknown(val actual: String) : TestType() {
      override fun toString() = "TestType:Unknown[$actual]"
    }
  }

  @Test
  @Parameters(method = "params for serializing types to strings")
  fun `enum values should be correctly serialized to their string values`(
      knownMappings: Map<TestType, String>,
      expectedStringValues: List<Pair<TestType?, String?>>
  ) {
    val adapter = SafeEnumTypeAdapter(
        knownMappings = knownMappings,
        unknownEnumToStringConverter = { throw RuntimeException() },
        unknownStringToEnumConverter = { throw RuntimeException() }
    )

    expectedStringValues.forEach { (type, expectedValue) ->
      assertThat(adapter.fromEnum(type)).isEqualTo(expectedValue)
    }
  }

  @Suppress("Unused")
  private fun `params for serializing types to strings`(): List<List<Any>> {
    fun testCase(
        mappings: Map<TestType, String>,
        cases: List<Pair<TestType?, String?>>
    ): List<Any> {
      return listOf(mappings, cases)
    }

    return listOf(
        testCase(
            mappings = mapOf(TestType.Value1 to "test1.value1", TestType.Value2 to "test1.value2"),
            cases = listOf(TestType.Value1 to "test1.value1", TestType.Value2 to "test1.value2", null to null)
        ),
        testCase(
            mappings = mapOf(TestType.Value1 to "test1_value1", TestType.Value2 to "test1_value2"),
            cases = listOf(TestType.Value1 to "test1_value1", TestType.Value2 to "test1_value2", null to null)
        )
    )
  }

  @Test
  @Parameters(method = "params for serializing unknown values")
  fun `unknown enum should be serialized using the fallback`(
      fallback: (TestType) -> String,
      testType: TestType?,
      expectedStringValue: String?
  ) {
    val knownMappings = mapOf(TestType.Value1 to "test1.value1", TestType.Value2 to "test1.value2")
    val adapter = SafeEnumTypeAdapter(
        knownMappings = knownMappings,
        unknownEnumToStringConverter = fallback,
        unknownStringToEnumConverter = { throw RuntimeException() }
    )

    assertThat(adapter.fromEnum(testType)).isEqualTo(expectedStringValue)
  }

  @Suppress("Unused")
  private fun `params for serializing unknown values`(): List<List<Any?>> {
    fun testCase(
        fallback: (TestType) -> String,
        testType: TestType?,
        expectedStringValue: String?
    ): List<Any?> {
      return listOf(fallback, testType, expectedStringValue)
    }

    return listOf(
        testCase(
            fallback = { (it as TestType.Unknown).actual },
            testType = TestType.Unknown("abducted_by_joker"),
            expectedStringValue = "abducted_by_joker"
        ),
        testCase(
            fallback = { (it as TestType.Unknown).actual },
            testType = null,
            expectedStringValue = null
        ),
        testCase(
            fallback = { "disapparated_in_hogwarts" },
            testType = TestType.Unknown("abducted_by_joker"),
            expectedStringValue = "disapparated_in_hogwarts"
        ),
        testCase(
            fallback = { (it as TestType.Unknown).actual },
            testType = TestType.Unknown("moved"),
            expectedStringValue = "moved"
        ),
        testCase(
            fallback = { (it as TestType.Unknown).actual },
            testType = TestType.Value1,
            expectedStringValue = "test1.value1"
        )
    )
  }

  @Test
  @Parameters(method = "params for deserializing strings to types")
  fun `enum values should be correctly deserialized from their string values`(
      knownMappings: Map<TestType, String>,
      expectedTypeValues: List<Pair<String?, TestType?>>
  ) {
    val adapter = SafeEnumTypeAdapter(
        knownMappings = knownMappings,
        unknownEnumToStringConverter = { throw RuntimeException() },
        unknownStringToEnumConverter = { throw RuntimeException() }
    )

    expectedTypeValues.forEach { (string, expectedType) ->
      assertThat(adapter.toEnum(string)).isEqualTo(expectedType)
    }
  }

  @Suppress("Unused")
  private fun `params for deserializing strings to types`(): List<List<Any>> {
    fun testCase(
        mappings: Map<TestType, String>,
        cases: List<Pair<String?, TestType?>>
    ): List<Any> {
      return listOf(mappings, cases)
    }

    return listOf(
        testCase(
            mappings = mapOf(TestType.Value1 to "test1.value1", TestType.Value2 to "test1.value2"),
            cases = listOf("test1.value1" to TestType.Value1, "test1.value2" to TestType.Value2, null to null)
        ),
        testCase(
            mappings = mapOf(TestType.Value1 to "test1_value1", TestType.Value2 to "test1_value2"),
            cases = listOf("test1_value1" to TestType.Value1, "test1_value2" to TestType.Value2, null to null)
        )
    )
  }

  @Test
  @Parameters(method = "params for deserializing unknown values")
  fun `unknown enum should be deserialized using the fallback`(
      fallback: (String) -> TestType,
      stringValue: String?,
      expectedEnum: TestType?
  ) {
    val knownMappings = mapOf(TestType.Value1 to "test1.value1", TestType.Value2 to "test1.value2")
    val adapter = SafeEnumTypeAdapter(
        knownMappings = knownMappings,
        unknownEnumToStringConverter = { throw RuntimeException() },
        unknownStringToEnumConverter = fallback
    )

    assertThat(adapter.toEnum(stringValue)).isEqualTo(expectedEnum)
  }

  @Suppress("Unused")
  private fun `params for deserializing unknown values`(): List<List<Any?>> {
    fun testCase(
        fallback: (String) -> TestType,
        stringValue: String?,
        expectedEnum: TestType?
    ): List<Any?> {
      return listOf(fallback, stringValue, expectedEnum)
    }

    return listOf(
        testCase(
            fallback = { TestType.Unknown(it) },
            stringValue = "abducted_by_joker",
            expectedEnum = TestType.Unknown("abducted_by_joker")
        ),
        testCase(
            fallback = { TestType.Unknown(it) },
            stringValue = null,
            expectedEnum = null
        ),
        testCase(
            fallback = { TestType.Value1 },
            stringValue = "abducted_by_joker",
            expectedEnum = TestType.Value1
        ),
        testCase(
            fallback = { TestType.Unknown(it) },
            stringValue = "disapparated_in_hogwarts",
            expectedEnum = TestType.Unknown("disapparated_in_hogwarts")
        ),
        testCase(
            fallback = { TestType.Unknown("moved") },
            stringValue = "disapparated_in_hogwarts",
            expectedEnum = TestType.Unknown("moved")
        )
    )
  }
}
