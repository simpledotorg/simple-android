package org.simple.clinic.patient.filter

import kotlin.math.max


/**
 * This is a comparator that is used to sort [SearchInfo] objects by prioritizing matches
 * which have the same number of words as the search term while still providing good
 * results when the search term and the name do not have the same number of words.
 *
 * It works by generating an overall score for a match based on a weighted function that
 * multiples the edit distance for a particular part with a multiplier which is decided
 * based on the position of the term in the word.
 *
 * Variations in the earlier parts of the name are penalised more than the variations in the
 * later parts of the name and names with more matching words get a better score.
 **/
class SortByWeightedNameParts : Comparator<SearchInfo> {

  override fun compare(first: SearchInfo, second: SearchInfo): Int {
    val scoreOfFirst = computeScore(first)
    val scoreOfSecond = computeScore(second)

    return scoreOfFirst.compareTo(scoreOfSecond)
  }

  private fun computeScore(searchInfo: SearchInfo): Double {
    val maximumWordParts = max(searchInfo.nameParts.size, searchInfo.searchParts.size)

    // Generates decreasing multipliers based on the position of the
    // term. i.e, if the maximum number of parts is 3, the multipliers are
    // 3, 2, 1 for positions 0, 1, 2 respectively.
    val positionMultipliers = (1..maximumWordParts).map { it }.reversed()

    // Generate weighted scores for each position by assigning an arbitrary value,
    // one divided by the max parts in this case, and multiplying that value with
    // the position multipliers generated previously.
    val defaultScores = (1..maximumWordParts).map { (1 / maximumWordParts.toDouble()) * positionMultipliers[it - 1] }

    // Generate the final score by multiplying the distance (normalized by dividing it by the
    // total sum of the distances) with the weighted scores computed earlier and adding all the
    // individual scores.
    val sumOfDistances = searchInfo.distances.sum()
    val scores = defaultScores.toMutableList()

    searchInfo.distances.forEachIndexed { index, distance ->
      scores[index] *= when (sumOfDistances) {
        0.0 -> 0.0
        else -> distance / sumOfDistances
      }
    }

    return scores.sum()
  }
}
