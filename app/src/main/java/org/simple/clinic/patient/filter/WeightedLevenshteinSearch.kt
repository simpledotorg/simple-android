package org.simple.clinic.patient.filter

import info.debatty.java.stringsimilarity.CharacterInsDelInterface
import info.debatty.java.stringsimilarity.CharacterSubstitutionInterface
import info.debatty.java.stringsimilarity.WeightedLevenshtein
import io.reactivex.Single
import org.simple.clinic.patient.PatientSearchResult
import java.util.UUID

class WeightedLevenshteinSearch(
    private val minimumSearchTermLength: Int,
    private val fuzzyStringDistanceCutoff: Float,
    private val resultsComparator: Comparator<SearchContext>,
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

  override fun search(searchTerm: String, names: List<PatientSearchResult.PatientNameAndId>): Single<List<UUID>> {
    val searchTermParts = nameToSearchableParts(searchTerm)
        .filter { it.length >= minimumSearchTermLength }

    return if (searchTermParts.isEmpty()) {
      Single.just(emptyList())
    } else {
      val searchContext = names
          .filter { it.fullName.isNotBlank() }
          .map {
            SearchContext(
                patient = it,
                nameParts = nameToSearchableParts(it.fullName),
                searchParts = searchTermParts)
          }
          .filter { it.nameParts.isNotEmpty() }
          .map {
            val distances = searchTermParts.zip(it.nameParts) { searchPart, namePart ->
              EditDistanceRecord(
                  namePart = namePart,
                  searchPart = searchPart,
                  editDistance = levenshtein.distance(searchPart, namePart))
            }

            it.copy(editDistanceRecords = distances)
          }
          .filter { it.totalEditDistance <= fuzzyStringDistanceCutoff }
          .sortedWith(resultsComparator)

      Single.just(searchContext.map { it.patient.uuid })
    }
  }

  private fun nameToSearchableParts(string: String) = string.split(whiteSpaceRegex)
      .filter { it.length >= minimumSearchTermLength }
      .map { it.toLowerCase() }
}
