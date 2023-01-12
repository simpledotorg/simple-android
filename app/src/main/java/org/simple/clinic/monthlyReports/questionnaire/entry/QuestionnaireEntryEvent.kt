package org.simple.clinic.monthlyReports.questionnaire.entry

import org.simple.clinic.facility.Facility
import org.simple.clinic.monthlyReports.questionnaire.Questionnaire

sealed class QuestionnaireEntryEvent

data class CurrentFacilityLoaded(val facility: Facility) : QuestionnaireEntryEvent()

data class QuestionnaireFormFetched(val questionnaire: Questionnaire) : QuestionnaireEntryEvent()
