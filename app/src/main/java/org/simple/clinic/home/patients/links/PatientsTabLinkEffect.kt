package org.simple.clinic.home.patients.links

sealed class PatientsTabLinkEffect

object LoadCurrentFacility : PatientsTabLinkEffect()

object OpenMonthlyScreeningReportsListScreen : PatientsTabLinkEffect()

object OpenPatientLineListDownloadDialog : PatientsTabLinkEffect()

