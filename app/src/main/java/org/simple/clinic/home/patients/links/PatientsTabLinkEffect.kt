package org.simple.clinic.home.patients.links

sealed class PatientsTabLinkEffect

object LoadCurrentFacility : PatientsTabLinkEffect()

object LoadQuestionnaires : PatientsTabLinkEffect()

object LoadQuestionnaireResponses : PatientsTabLinkEffect()

object OpenMonthlyScreeningReportsListScreen : PatientsTabLinkEffect()

object OpenMonthlySuppliesReportsListScreen : PatientsTabLinkEffect()

object OpenPatientLineListDownloadDialog : PatientsTabLinkEffect()

