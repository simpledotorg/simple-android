package org.simple.clinic.patient.filter

import info.debatty.java.stringsimilarity.CharacterInsDelInterface
import info.debatty.java.stringsimilarity.CharacterSubstitutionInterface
import info.debatty.java.stringsimilarity.WeightedLevenshtein
import org.simple.clinic.patient.PatientSearchResult
import java.util.Locale
import java.util.UUID

class WeightedLevenshteinSearch(
    private val minimumSearchTermLength: Int,
    private val maximumAllowedEditDistance: Float,
    private val resultsComparator: Comparator<PatientSearchContext>,
    characterSubstitutionCost: Float,
    characterInsertionCost: Float,
    characterDeletionCost: Float
) : SearchPatientByName {

  init {
    if (minimumSearchTermLength < 1) {
      throw AssertionError("Minimum search term length cannot be less than 1")
    }
  }

  private val whiteSpaceRegex = Regex("[\\s]")

  private val levenshtein = WeightedLevenshtein(
      CharacterSubstitutionInterface { _, _ -> characterSubstitutionCost.toDouble() },
      object : CharacterInsDelInterface {
        override fun deletionCost(c: Char) = characterDeletionCost.toDouble()

        override fun insertionCost(c: Char) = characterInsertionCost.toDouble()
      }
  )

  override fun search(searchTerm: String, names: List<PatientSearchResult.PatientNameAndId>): List<UUID> {
    val searchTermParts = stringToSearchableParts(searchTerm)
        .filter { it.length >= minimumSearchTermLength }

    return if (searchTermParts.isEmpty()) {
      emptyList()
    } else {
      val searchContext = names
          .filter { it.fullName.isNotBlank() }
          .map {
            PatientSearchContext(
                patient = it,
                nameParts = stringToSearchableParts(it.fullName),
                searchParts = searchTermParts)
          }
          .filter { it.nameParts.isNotEmpty() }
          .map {
            val distances = searchTermParts.zip(it.nameParts) { searchPart, namePart ->
              PatientSearchContext.EditDistance(
                  namePart = namePart,
                  searchPart = searchPart,
                  editDistance = levenshtein.distance(searchPart, namePart))
            }

            it.copy(editDistances = distances)
          }
          .filter(this::discardLowQualityResults)
          .sortedWith(resultsComparator)

      searchContext.map { it.patient.uuid }
    }
  }

  private fun discardLowQualityResults(patientSearchContext: PatientSearchContext): Boolean {
    return patientSearchContext.totalEditDistance <= maximumAllowedEditDistance
  }

  private fun stringToSearchableParts(string: String) = string.split(whiteSpaceRegex)
      .filter { it.length >= minimumSearchTermLength }
      .map { namePart -> namePart.filter { it.isLetter() } }
      .filter { it.isNotBlank() }
      .map { it.toLowerCase(Locale.ROOT) }
}
