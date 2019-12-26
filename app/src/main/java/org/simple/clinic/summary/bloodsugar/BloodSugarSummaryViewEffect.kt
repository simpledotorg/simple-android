package org.simple.clinic.summary.bloodsugar

sealed class BloodSugarSummaryViewEffect

object OpenBloodSugarTypeSelector : BloodSugarSummaryViewEffect()

object FetchBloodSugarSummary : BloodSugarSummaryViewEffect()
