package org.simple.clinic.questionnaire

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class QuestionnaireSections(
    val screeningQuestionnaire: Questionnaire?,
    val suppliesQuestionnaire: Questionnaire?,
    val drugStockReportsQuestionnaire: Questionnaire?,
) : Parcelable
