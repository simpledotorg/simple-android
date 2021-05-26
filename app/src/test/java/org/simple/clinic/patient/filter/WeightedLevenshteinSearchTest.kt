package org.simple.clinic.patient.filter

import com.google.common.truth.Truth.assertThat
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.patient.PatientSearchResult.PatientNameAndId
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class WeightedLevenshteinSearchTest {

  @Test
  @Parameters(method = "parameters for searching based on the weight")
  fun `it should search based on the weights provided`(
      charDeletionCost: Float,
      charInsertionCost: Float,
      charSubstitutionCost: Float,
      input: List<PatientNameAndId>,
      searchTerm: String,
      expectedResults: List<UUID>
  ) {
    val search = WeightedLevenshteinSearch(
        minimumSearchTermLength = 1,
        maximumAllowedEditDistance = 100F,
        characterSubstitutionCost = charSubstitutionCost,
        characterDeletionCost = charDeletionCost,
        characterInsertionCost = charInsertionCost,
        resultsComparator = DoNothingComparator())

    assertThat(search.search(searchTerm, input)).isEqualTo(expectedResults)
  }

  @Suppress("Unused")
  private fun `parameters for searching based on the weight`(): List<Any> {
    fun constructTestData(
        charDeletionCost: Float,
        charInsertionCost: Float,
        charSubstitutionCost: Float,
        names: List<String>,
        expectedResultIndices: List<Int>,
        searchTerm: String
    ): List<Any> {
      val input = names.map { PatientNameAndId(UUID.randomUUID(), it) }
      val expectedResults = expectedResultIndices.map { input[it].uuid }

      return listOf(charDeletionCost, charInsertionCost, charSubstitutionCost, input, searchTerm, expectedResults)
    }

    return listOf(
        constructTestData(
            charDeletionCost = 1F,
            charInsertionCost = 1F,
            charSubstitutionCost = 1F,
            names = listOf("Ashok", "Rahul", "John"),
            expectedResultIndices = listOf(0, 1, 2),
            searchTerm = "Ashok"),
        constructTestData(
            charDeletionCost = 10F,
            charInsertionCost = 500F,
            charSubstitutionCost = 10F,
            names = listOf("Ashok", "Rahul", "John"),
            searchTerm = "Joan",
            expectedResultIndices = listOf(2)),
        constructTestData(
            charDeletionCost = 500F,
            charInsertionCost = 10F,
            charSubstitutionCost = 10F,
            names = listOf("Ashok", "Rahul", "John"),
            searchTerm = "Ashok",
            expectedResultIndices = listOf(0, 1)),
        constructTestData(
            charDeletionCost = 500F,
            charInsertionCost = 500F,
            charSubstitutionCost = 90F,
            names = listOf("Ashok", "Rahul", "John"),
            searchTerm = "Athok",
            expectedResultIndices = listOf(0)),
        constructTestData(
            charDeletionCost = 500F,
            charInsertionCost = 90F,
            charSubstitutionCost = 500F,
            names = listOf("Ashok", "Rahul", "John"),
            searchTerm = "Jon",
            expectedResultIndices = listOf(2)),
        constructTestData(
            charDeletionCost = 90F,
            charInsertionCost = 500F,
            charSubstitutionCost = 500F,
            names = listOf("Ashok", "Rahul", "John"),
            searchTerm = "Ashoka",
            expectedResultIndices = listOf(0)),
        constructTestData(
            charDeletionCost = 500F,
            charInsertionCost = 500F,
            charSubstitutionCost = 500F,
            names = listOf("Ashok", "Rahul", "John"),
            searchTerm = "Asok",
            expectedResultIndices = emptyList()),
        constructTestData(
            charDeletionCost = 30F,
            charInsertionCost = 30F,
            charSubstitutionCost = 30F,
            names = listOf("Kamlesh Mistry", "Umesh Rao Goswami", "Bagwati Dora"),
            searchTerm = "Khamlesh Mistree",
            expectedResultIndices = listOf(0)),
        constructTestData(
            charDeletionCost = 30F,
            charInsertionCost = 30F,
            charSubstitutionCost = 30F,
            names = listOf("Kamlesh Mistry", "Kamles Mistry"),
            searchTerm = "Khamlesh Mistree",
            expectedResultIndices = listOf(0)),
        constructTestData(
            charDeletionCost = 30F,
            charInsertionCost = 30F,
            charSubstitutionCost = 30F,
            names = listOf("Kamlesh Mistry", "Kamles Mistry"),
            searchTerm = "Khamlesh Mistry",
            expectedResultIndices = listOf(0, 1)),
        constructTestData(
            charDeletionCost = 120F,
            charInsertionCost = 120F,
            charSubstitutionCost = 120F,
            names = listOf("Rahul Shenoy", "Rahul Sharma"),
            searchTerm = "Rahul",
            expectedResultIndices = listOf(0, 1)),
        constructTestData(
            charDeletionCost = 90F,
            charInsertionCost = 90F,
            charSubstitutionCost = 90F,
            names = listOf("Rahul", "Rahul Sharma"),
            searchTerm = "Rahul Sarma",
            expectedResultIndices = listOf(0, 1)),
        constructTestData(
            charDeletionCost = 1F,
            charInsertionCost = 1F,
            charSubstitutionCost = 1F,
            names = listOf("Rahul", "", "Rahul Sharma"),
            searchTerm = "Rahul Sarma",
            expectedResultIndices = listOf(0, 2)),
        constructTestData(
            charDeletionCost = 1F,
            charInsertionCost = 1F,
            charSubstitutionCost = 1F,
            names = listOf("Rahul", "Rahul Sharma"),
            searchTerm = "",
            expectedResultIndices = emptyList())
    )
  }

  @Test
  @Parameters(method = "params for ignoring white spaces")
  fun `it should ignore white spaces both in the search term and in the name`(
      input: List<PatientNameAndId>,
      searchTerm: String,
      expectedResults: List<UUID>
  ) {
    val search = WeightedLevenshteinSearch(
        minimumSearchTermLength = 1,
        maximumAllowedEditDistance = 100F,
        characterSubstitutionCost = 30F,
        characterDeletionCost = 30F,
        characterInsertionCost = 30F,
        resultsComparator = DoNothingComparator())

    assertThat(search.search(searchTerm, input)).isEqualTo(expectedResults)
  }

  @Suppress("Unused")
  private fun `params for ignoring white spaces`(): List<Any> {
    fun constructTestData(
        names: List<String>,
        expectedResultIndices: List<Int>,
        searchTerm: String
    ): List<Any> {
      val input = names.map { PatientNameAndId(UUID.randomUUID(), it) }
      val expectedResults = expectedResultIndices.map { input[it].uuid }

      return listOf(input, searchTerm, expectedResults)
    }

    return listOf(
        constructTestData(
            names = listOf("Ashok Sarma", "Ashok Sarma", "Ashok   Sarma", "Ashok\t \tSarma"),
            expectedResultIndices = listOf(0, 1, 2, 3),
            searchTerm = "Ashok Sarma"),
        constructTestData(
            names = listOf("Ashok Sarma", "Ashok Sarma", "Ashok   Sarma", "Ashok\t \tSarma"),
            expectedResultIndices = listOf(0, 1, 2, 3),
            searchTerm = "Ashok   Sarma"),
        constructTestData(
            names = listOf("Ashok Sarma", "Ashok Sarma", "Ashok   Sarma", "Ashok\t \tSarma"),
            expectedResultIndices = listOf(0, 1, 2, 3),
            searchTerm = "Ashok\tSarma"),
        constructTestData(
            names = listOf("Ashok Sarma", "Ashok Sarma", "Ashok   Sarma", "Ashok\t \tSarma"),
            expectedResultIndices = listOf(0, 1, 2, 3),
            searchTerm = "Ashok\t \tSarma")
    )
  }

  @Test
  @Parameters(method = "params for ignoring words lesser than cutoff")
  fun `it should ignore words in the name that are smaller than the minimum allowed length`(
      minimumSearchTermLength: Int,
      input: List<PatientNameAndId>,
      searchTerm: String,
      expectedResults: List<UUID>
  ) {
    val search = WeightedLevenshteinSearch(
        minimumSearchTermLength = minimumSearchTermLength,
        maximumAllowedEditDistance = 100F,
        characterSubstitutionCost = 30F,
        characterDeletionCost = 30F,
        characterInsertionCost = 30F,
        resultsComparator = DoNothingComparator())

    assertThat(search.search(searchTerm, input)).isEqualTo(expectedResults)
  }

  @Suppress("Unused")
  private fun `params for ignoring words lesser than cutoff`(): List<Any> {
    fun constructTestData(
        minimumSearchTermLength: Int,
        names: List<String>,
        expectedResultIndices: List<Int>,
        searchTerm: String
    ): List<Any> {
      val input = names.map { PatientNameAndId(UUID.randomUUID(), it) }
      val expectedResults = expectedResultIndices.map { input[it].uuid }

      return listOf(minimumSearchTermLength, input, searchTerm, expectedResults)
    }

    return listOf(
        constructTestData(
            minimumSearchTermLength = 3,
            names = listOf("Ashok A Sarma", "Ashok AA Sarma", "Mr Ashok Sarma", "Ashok Sarma S"),
            expectedResultIndices = listOf(0, 1, 2, 3),
            searchTerm = "Ashok Sarma"),
        constructTestData(
            minimumSearchTermLength = 2,
            names = listOf("Ashok A Sarma", "Ashok AA Sarma"),
            expectedResultIndices = listOf(0, 1),
            searchTerm = "Ashok Sarma"),
        constructTestData(
            minimumSearchTermLength = 3,
            names = listOf("Ashok AA Sharma", "Ashok AA Sarma", "Asok Rahul Sharma"),
            expectedResultIndices = listOf(0, 1),
            searchTerm = "Ashok AA Sarma"),
        constructTestData(
            minimumSearchTermLength = 2,
            names = listOf("Ashok AA Sharma", "Ashok AA Sarma"),
            expectedResultIndices = emptyList(),
            searchTerm = "Ashok Rahul Sarma")
    )
  }

  @Test
  @Parameters(method = "params for sorting results based on the comparator")
  fun `it should sort the results based on the comparator provided`(
      input: List<PatientNameAndId>,
      searchTerm: String,
      comparator: Comparator<PatientSearchContext>,
      expectedResults: List<UUID>
  ) {
    val search = WeightedLevenshteinSearch(
        minimumSearchTermLength = 1,
        maximumAllowedEditDistance = 100F,
        characterSubstitutionCost = 1F,
        characterDeletionCost = 1F,
        characterInsertionCost = 1F,
        resultsComparator = comparator)

    assertThat(search.search(searchTerm, input)).isEqualTo(expectedResults)
  }

  @Suppress("Unused")
  private fun `params for sorting results based on the comparator`(): List<Any> {
    fun constructTestData(
        names: List<String>,
        expectedResultIndices: List<Int>,
        comparator: Comparator<PatientSearchContext>,
        searchTerm: String
    ): List<Any> {
      val input = names.map { PatientNameAndId(UUID.randomUUID(), it) }
      val expectedResults = expectedResultIndices.map { input[it].uuid }

      return listOf(input, searchTerm, comparator, expectedResults)
    }

    return listOf(
        constructTestData(
            names = listOf("Ashok Sarma", "Ashok Sarma", "Ashok Sharma", "Asok Sharma", "Asok Sharma"),
            expectedResultIndices = listOf(0, 1, 2, 3, 4),
            searchTerm = "Ashok Sarma",
            comparator = DoNothingComparator()),
        constructTestData(
            names = listOf("Ashok Sarma", "Ashok Sharma", "Asok Sharma", "Asok Sharma", "Ashok Sarma"),
            expectedResultIndices = listOf(0, 1, 2, 3, 4),
            searchTerm = "Ashok Sarma",
            comparator = DoNothingComparator()),
        constructTestData(
            names = listOf("Ashok Sarma", "Ashok Sharma", "Asok Sharma", "Asoka Sharma", "Ashok Sarma"),
            expectedResultIndices = listOf(0, 4, 1, 2, 3),
            searchTerm = "Ashok Sarma",
            comparator = SortByTotalSumOfDistances()),
        constructTestData(
            names = listOf("Ashok Sarma", "Ashok Sharma", "Asok Sharma", "Asoka Sharma", "Ashok Sarma"),
            expectedResultIndices = listOf(3, 2, 1, 0, 4),
            searchTerm = "Ashok Sarma",
            comparator = SortByTotalSumOfDistances().reversed()),
        constructTestData(
            names = listOf("Parul", "Raul", "Rahul"),
            expectedResultIndices = listOf(2, 1, 0),
            searchTerm = "Rahul",
            comparator = SortByTotalSumOfDistances()),
        constructTestData(
            names = listOf("Parul", "Raul", "Rahul"),
            expectedResultIndices = listOf(0, 1, 2),
            searchTerm = "Rahul",
            comparator = SortByTotalSumOfDistances().reversed()))
  }

  @Test
  @Parameters(method = "params for ignoring non-letter characters")
  fun `it should ignore all non-letter characters when searching`(
      input: List<PatientNameAndId>,
      searchTerm: String,
      expectedResults: List<UUID>
  ) {
    val search = WeightedLevenshteinSearch(
        minimumSearchTermLength = 1,
        maximumAllowedEditDistance = 3F,
        characterInsertionCost = 1F,
        characterDeletionCost = 1F,
        characterSubstitutionCost = 1F,
        resultsComparator = DoNothingComparator()
    )

    assertThat(search.search(searchTerm, input)).isEqualTo(expectedResults)
  }

  @Suppress("Unused")
  private fun `params for ignoring non-letter characters`(): List<List<Any>> {
    fun testCase(
        names: List<String>,
        searchTerm: String,
        expectedResultIndices: List<Int>
    ): List<Any> {
      val input = names.map { PatientNameAndId(UUID.randomUUID(), it) }
      val expectedResults = expectedResultIndices.map { input[it].uuid }

      return listOf(input, searchTerm, expectedResults)
    }

    return listOf(
        testCase(
            names = listOf("Ashok Sharma", "Ashok Sharma123", "Ashok 123 Sharma", "Ashok Sharma(2345)", "Ashok(134) Sharma"),
            searchTerm = "Ashok Sharma",
            expectedResultIndices = listOf(0, 1, 2, 3, 4)
        ),
        testCase(
            names = listOf("Ashok Sharma", "Ashok Sharma123", "Ashok Sharma(2345)", "Ashokan(134) Sharman"),
            searchTerm = "Asok Sarma",
            expectedResultIndices = listOf(0, 1, 2)
        ),
        testCase(
            names = listOf("ವಿನಯ್ ಶೆನೊಯ್(0045)", "ವಿನಯ್ ಶೆನೊಯ್", "ವಿನಯ್ ಶೆನೊಯ್(೦೦೪೫)"),
            searchTerm = "ವಿನಯ್ ಶೆನೊಯ್",
            expectedResultIndices = listOf(0, 1, 2)
        ),
        testCase(
            names = listOf("சஞ்சித அகர்வால்(123)", "சஞ்சித அகர்வால்", "சஞ்சித அகர்வால்(௧௨௩)"),
            searchTerm = "சஞ்சித அகர்வால்",
            expectedResultIndices = listOf(0, 1, 2)
        ),
        testCase(
            names = listOf("रक्षक हेगड़े(123)", "रक्षक हेगड़े", "रक्षक हेगड़े(१२३)"),
            searchTerm = "रक्षक हेगड़े",
            expectedResultIndices = listOf(0, 1, 2)
        ),
        testCase(
            names = listOf("ਸਾਕੇਤ ਨਾਰਾਇਣ(123)", "ਸਾਕੇਤ ਨਾਰਾਇਣ", "ਸਾਕੇਤ ਨਾਰਾਇਣ(੧੨੩)"),
            searchTerm = "ਸਾਕੇਤ ਨਾਰਾਇਣ",
            expectedResultIndices = listOf(0, 1, 2)
        ),
        testCase(
            names = listOf("പ്രതുൽ കാലിലെ(123)", "പ്രതുൽ കാലിലെ", "പ്രതുൽ കാലിലെ(൧൨൩)"),
            searchTerm = "പ്രതുൽ കാലിലെ",
            expectedResultIndices = listOf(0, 1, 2)
        )
    )
  }

  private class DoNothingComparator : Comparator<PatientSearchContext> {
    override fun compare(o1: PatientSearchContext, o2: PatientSearchContext) = 0
  }
}
