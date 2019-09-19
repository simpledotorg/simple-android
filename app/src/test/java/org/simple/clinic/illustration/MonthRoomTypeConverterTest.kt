package org.simple.clinic.illustration

import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.Month

class MonthRoomTypeConverterTest {

  private val converter = DayOfMonth.MonthRoomTypeConverter()

  @Test
  fun `verify month enum is serialized as expected`() {
    val monthString = converter.fromEnum(Month.JANUARY)
    assertEquals("JANUARY", monthString)
  }

  @Test
  fun `verify month enum is parsed when the serialized form is not in uppercase`() {
    val monthEnum = converter.toEnum("January")
    assertEquals(Month.JANUARY, monthEnum)
  }
}
