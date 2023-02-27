package org.simple.clinic.home.patients.links

sealed class PatientsTabLinkEffect

object LoadMonthlyScreeningReportsFormEffect : PatientsTabLinkEffect()

object LoadMonthlyScreeningReportsListEffect : PatientsTabLinkEffect()

object OpenMonthlyScreeningReportsListScreen : PatientsTabLinkEffect()

object OpenPatientLineListDownloadDialog : PatientsTabLinkEffect()

