package org.simple.clinic.patient.filter

import kotlin.math.max

/**
 * This is a comparator that is used to sort [PatientSearchContext] objects by prioritizing matches
 * which have the same number of words as the search term while still providing good
 * results when the search term and the name do not have the same number of words.
 *
 * It works by generating an overall score for a match based on a weighted function that
 * multiples the edit distance for a particular part with a multiplier which is decided
 * based on the position of the term in the word.
 *
 * Variations in words are penalized in a progressively decreasing manner from left to
 * right. Names with more matching words get a better score.
 **/
class SortByWeightedNameParts : Comparator<PatientSearchContext> {

  override fun compare(first: PatientSearchContext, second: PatientSearchContext): Int {
    val scoreOfFirst = computeScore(first)
    val scoreOfSecond = computeScore(second)

    return scoreOfFirst.compareTo(scoreOfSecond)
  }

  private fun computeScore(searchInfo: PatientSearchContext): Double {
    val maximumWordParts = max(searchInfo.nameParts.size, searchInfo.searchParts.size)

    // Generate decreasing multipliers based on the position of the
    // term. i.e, if the maximum number of parts is 3, the multipliers are
    // 3, 2, 1 for positions 0, 1, 2 respectively.
    val penaltiesForPositions: List<Int> = (1..maximumWordParts).map { it }.reversed()

    // Generate weighted scores for each position by assigning a default value,
    // one divided by the max parts in this case, and multiplying that value with
    // the position multipliers generated previously.
    val triedAndTestedArbitraryValue = 1 / maximumWordParts.toDouble()
    val positionBasedDefaultWeights = penaltiesForPositions.map { penalty -> penalty * triedAndTestedArbitraryValue }

    val editDistances = searchInfo.editDistances.map { it.editDistance }

    return positionBasedDefaultWeights
        .mapIndexed { index, weightedScore ->
          weightedScore * editDistances.getOrElse(index) { 1.0 }
        }
        .sum()
  }
}
