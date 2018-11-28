package org.simple.clinic.patient.filter

class SortByTotalSumOfDistances : Comparator<PatientSearchContext> {

  override fun compare(first: PatientSearchContext, second: PatientSearchContext) = first.totalEditDistance.compareTo(second.totalEditDistance)
}
