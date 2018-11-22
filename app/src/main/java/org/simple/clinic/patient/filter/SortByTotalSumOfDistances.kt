package org.simple.clinic.patient.filter

class SortByTotalSumOfDistances : Comparator<SearchContext> {

  override fun compare(first: SearchContext, second: SearchContext) = first.totalEditDistance.compareTo(second.totalEditDistance)
}
