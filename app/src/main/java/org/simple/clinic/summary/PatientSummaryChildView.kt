package org.simple.clinic.summary

typealias PatientSummaryModelUpdateCallback = (PatientSummaryChildModel) -> Unit

interface PatientSummaryChildView {
  fun registerSummaryModelUpdateCallback(callback: PatientSummaryModelUpdateCallback?)
}
