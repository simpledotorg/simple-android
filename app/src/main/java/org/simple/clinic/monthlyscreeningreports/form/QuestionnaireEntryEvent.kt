package org.simple.clinic.monthlyscreeningreports.form

import org.simple.clinic.facility.Facility
import org.simple.clinic.questionnaire.Questionnaire

sealed class QuestionnaireEntryEvent

data class CurrentFacilityLoaded(val facility: Facility) : QuestionnaireEntryEvent()

data class QuestionnaireFormFetched(val questionnaire: Questionnaire) : QuestionnaireEntryEvent()
