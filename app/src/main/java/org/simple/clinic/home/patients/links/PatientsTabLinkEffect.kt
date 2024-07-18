package org.simple.clinic.home.patients.links

sealed class PatientsTabLinkEffect

data object LoadCurrentFacility : PatientsTabLinkEffect()

data object LoadQuestionnaires : PatientsTabLinkEffect()

data object LoadQuestionnaireResponses : PatientsTabLinkEffect()

data object OpenMonthlyScreeningReportsListScreen : PatientsTabLinkEffect()

data object OpenMonthlySuppliesReportsListScreen : PatientsTabLinkEffect()

data object OpenPatientLineListDownloadDialog : PatientsTabLinkEffect()

data object OpenDrugStockReportsScreen : PatientsTabLinkEffect()
