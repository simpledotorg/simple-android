package org.simple.clinic.patient.filter

class SortByTotalSumOfDistances : Comparator<PatientSearchContext> {

  override fun compare(first: PatientSearchContext, second: PatientSearchContext): Int =
      first.totalEditDistance.compareTo(second.totalEditDistance)
}
