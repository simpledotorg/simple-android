package org.simple.clinic.patient.filter

import com.google.common.truth.Truth.assertThat
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.patient.PatientSearchResult.PatientNameAndId
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class SortByWeightedNamePartsTest {

  @Test
  @Parameters(method = "params for sorting values")
  fun `it should sort the results based on the overall match of individual parts`(
      originalList: List<PatientSearchContext>,
      expected: List<PatientSearchContext>
  ) {
    val sorted = originalList.sortedWith(SortByWeightedNameParts())

    assertThat(sorted).isEqualTo(expected)
  }

  @Suppress("Unused")
  private fun `params for sorting values`(): List<List<Any>> {
    fun generateTestData(
        nameParts: List<List<String>>,
        searchParts: List<String>,
        distances: List<List<PatientSearchContext.EditDistance>>,
        resultIndices: List<Int>
    ): List<Any> {
      val originalList = distances.mapIndexed { index, distance ->
        PatientSearchContext(
            patient = PatientNameAndId(fullName = "Name", uuid = UUID.randomUUID()),
            nameParts = nameParts[index],
            searchParts = searchParts,
            editDistances = distance)
      }
      val expected = resultIndices.map { originalList[it] }

      return listOf(originalList, expected)
    }

    val template = PatientSearchContext.EditDistance(namePart = "", searchPart = "", editDistance = 0.0)

    return listOf(
        generateTestData(
            nameParts = listOf(
                listOf("Amar"),
                listOf("Amar", "Nair"),
                listOf("Amay", "Nayak")
            ),
            searchParts = listOf("Amar"),
            distances = listOf(
                listOf(template.copy(editDistance = 0.0)),
                listOf(template.copy(editDistance = 0.0)),
                listOf(template.copy(editDistance = 1.0))),
            resultIndices = listOf(0, 1, 2)),
        generateTestData(
            nameParts = listOf(
                listOf("Amar"),
                listOf("Amar", "Nair"),
                listOf("Amay", "Nayak"),
                listOf("Amay", "Nair")
            ),
            searchParts = listOf("Amay", "Nair"),
            distances = listOf(
                listOf(template.copy(editDistance = 1.0)),
                listOf(
                    template.copy(editDistance = 1.0),
                    template.copy(editDistance = 0.0)),
                listOf(
                    template.copy(editDistance = 0.0),
                    template.copy(editDistance = 3.0)),
                listOf(
                    template.copy(editDistance = 0.0),
                    template.copy(editDistance = 0.0))),
            resultIndices = listOf(3, 1, 0, 2)),
        generateTestData(
            nameParts = listOf(
                listOf("Amar"),
                listOf("Amar", "Nair"),
                listOf("Amay", "Nayak")
            ),
            searchParts = listOf("Amay"),
            distances = listOf(
                listOf(template.copy(editDistance = 1.0)),
                listOf(template.copy(editDistance = 1.0)),
                listOf(template.copy(editDistance = 0.0))),
            resultIndices = listOf(2, 0, 1)))
  }
}
