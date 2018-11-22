package org.simple.clinic.patient.filter

class SortByTotalSumOfDistances : Comparator<SearchInfo> {

  override fun compare(first: SearchInfo, second: SearchInfo): Int {
    val totalScoreOfFirst = first.distances.sum()
    val totalScoreOfSecond = second.distances.sum()

    return totalScoreOfFirst.compareTo(totalScoreOfSecond)
  }
}
