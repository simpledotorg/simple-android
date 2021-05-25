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
      originalList: List<PatientSearchContext>,
      expected: List<PatientSearchContext>
  ) {
    val sorted = originalList.sortedWith(SortByTotalSumOfDistances())

    assertThat(sorted).isEqualTo(expected)
  }

  @Suppress("Unused")
  private fun `params for sorting values`(): List<List<Any>> {
    fun generateTestData(
        distances: List<List<PatientSearchContext.EditDistance>>,
        results: List<Int>
    ): List<Any> {
      val originalList = distances.map {
        PatientSearchContext(
            patient = PatientNameAndId(fullName = "Name", uuid = UUID.randomUUID()),
            nameParts = emptyList(),
            searchParts = emptyList(),
            editDistances = it
        )
      }
      val expected = results.map { originalList[it] }

      return listOf(originalList, expected)
    }

    val template = PatientSearchContext.EditDistance(namePart = "", searchPart = "", editDistance = 0.0)

    return listOf(
        generateTestData(
            distances = listOf(
                listOf(template.copy(editDistance = 1.0)),
                listOf(template.copy(editDistance = 2.0)),
                listOf(template.copy(editDistance = 3.0))),
            results = listOf(0, 1, 2)
        ),
        generateTestData(
            distances = listOf(
                listOf(template.copy(editDistance = 1.0)),
                listOf(
                    template.copy(editDistance = 0.2),
                    template.copy(editDistance = 0.3),
                    template.copy(editDistance = 0.4)),
                listOf(template.copy(editDistance = 2.0)),
                listOf(template.copy(editDistance = 0.5))),
            results = listOf(3, 1, 0, 2)
        ),
        generateTestData(
            distances = listOf(
                listOf(
                    template.copy(editDistance = -0.1),
                    template.copy(editDistance = 0.1)),
                listOf(template.copy(editDistance = -0.5)),
                listOf(
                    template.copy(editDistance = -0.5),
                    template.copy(editDistance = 0.25),
                    template.copy(editDistance = -0.3)),
                listOf(
                    template.copy(editDistance = 2.0),
                    template.copy(editDistance = -4.0),
                    template.copy(editDistance = 3.0)),
                listOf(template.copy(editDistance = 4.0))),
            results = listOf(2, 1, 0, 3, 4)
        )
    )
  }
}
