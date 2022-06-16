package org.simple.clinic.home.overdue.search

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.home.overdue.search.OverdueSearchQueryValidator.Result.Empty
import org.simple.clinic.home.overdue.search.OverdueSearchQueryValidator.Result.LengthTooShort
import org.simple.clinic.home.overdue.search.OverdueSearchQueryValidator.Result.Valid

class OverdueSearchValidatorTest {

  private val validator = OverdueSearchQueryValidator(
      overdueSearchConfig = OverdueSearchConfig(minLengthOfSearchQuery = 3)
  )

  @Test
  fun `when search query is empty, then result should be empty`() {
    // given
    val searchQuery = ""

    // when
    val result = validator.validate(searchQuery = searchQuery)

    // then
    assertThat(result).isEqualTo(Empty)
  }

  @Test
  fun `when search query doesn't meet minimum length, then result be length too short`() {
    // given
    val searchQuery = "ba"

    // when
    val result = validator.validate(searchQuery = searchQuery)

    // then
    assertThat(result).isEqualTo(LengthTooShort)
  }

  @Test
  fun `when search query is not empty and meets minimum length, then result should be valid`() {
    // given
    val searchQuery = "bab"

    // when
    val result = validator.validate(searchQuery = searchQuery)

    // then
    assertThat(result).isEqualTo(Valid(searchQuery))
  }
}
