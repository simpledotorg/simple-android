package org.simple.clinic.patient.filter

import com.google.common.truth.Truth
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.patient.PatientSearchResult
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class SortByWeightedNamePartsTest {

  @Test
  @Parameters(method = "params for sorting values")
  fun `it should sort the results based on the overall match of individual parts`(
      originalList: List<SearchInfo>,
      expected: List<SearchInfo>
  ) {
    val sorted = originalList.sortedWith(SortByWeightedNameParts())

    Truth.assertThat(sorted).isEqualTo(expected)
  }

  @Suppress("Unused")
  private fun `params for sorting values`(): List<List<Any>> {
    fun generateTestData(
        nameParts: List<List<String>>,
        searchParts: List<String>,
        distances: List<List<Double>>,
        results: List<Int>
    ): List<Any> {
      val originalList = distances.mapIndexed { index, distance ->
        SearchInfo(
            patient = PatientSearchResult.PatientNameAndId(fullName = "Name", uuid = UUID.randomUUID()),
            nameParts = nameParts[index],
            searchParts = searchParts,
            distances = distance)
      }
      val expected = results.map { originalList[it] }

      return listOf(originalList, expected)
    }

    return listOf(
        generateTestData(
            nameParts = listOf(
                listOf("Amar"),
                listOf("Amar", "Nair"),
                listOf("Amay", "Nayak")
            ),
            searchParts = listOf("Amar"),
            distances = listOf(
                listOf(0.0),
                listOf(0.0),
                listOf(1.0)
            ),
            results = listOf(0, 1, 2)
        ),
        generateTestData(
            nameParts = listOf(
                listOf("Amar"),
                listOf("Amar", "Nair"),
                listOf("Amay", "Nayak"),
                listOf("Amay", "Nair")
            ),
            searchParts = listOf("Amay", "Nair"),
            distances = listOf(
                listOf(1.0),
                listOf(1.0, 0.0),
                listOf(0.0, 3.0),
                listOf(0.0, 0.0)
            ),
            results = listOf(3, 2, 1, 0)
        ),
        generateTestData(
            nameParts = listOf(
                listOf("Amar"),
                listOf("Amar", "Nair"),
                listOf("Amay", "Nayak")
            ),
            searchParts = listOf("Amay"),
            distances = listOf(
                listOf(1.0),
                listOf(1.0),
                listOf(0.0)
            ),
            results = listOf(2, 0, 1)
        )
    )
  }
}
