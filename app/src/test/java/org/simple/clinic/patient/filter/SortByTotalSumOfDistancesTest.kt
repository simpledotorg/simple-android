package org.simple.clinic.patient.filter

import com.google.common.truth.Truth.assertThat
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.patient.PatientSearchResult.PatientNameAndId
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class SortByTotalSumOfDistancesTest {

  @Test
  @Parameters(method = "params for sorting values")
  fun `it should sort the values based on the total sum of the distances`(
      originalList: List<SearchInfo>,
      expected: List<SearchInfo>
  ) {
    val sorted = originalList.sortedWith(SortByTotalSumOfDistances())

    assertThat(sorted).isEqualTo(expected)
  }

  @Suppress("Unused")
  private fun `params for sorting values`(): List<List<Any>> {
    fun generateTestData(distances: List<List<Double>>, results: List<Int>): List<Any> {
      val originalList = distances.map {
        SearchInfo(
            patient = PatientNameAndId(fullName = "Name", uuid = UUID.randomUUID()),
            nameParts = emptyList(),
            searchParts = emptyList(),
            distances = it
        )
      }
      val expected = results.map { originalList[it] }

      return listOf(originalList, expected)
    }

    return listOf(
        generateTestData(
            distances = listOf(
                listOf(1.0),
                listOf(2.0),
                listOf(3.0)
            ),
            results = listOf(0, 1, 2)
        ),
        generateTestData(
            distances = listOf(
                listOf(1.0),
                listOf(0.2, 0.3, 0.4),
                listOf(2.0),
                listOf(0.5)),
            results = listOf(3, 1, 0, 2)
        ),
        generateTestData(
            distances = listOf(
                listOf(-0.1, 0.1),
                listOf(-0.5),
                listOf(-0.5, 0.25, -0.3),
                listOf(2.0, -4.0, 3.0),
                listOf(4.0)),
            results = listOf(2, 1, 0, 3, 4)
        )
    )
  }
}
