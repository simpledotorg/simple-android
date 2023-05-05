package org.simple.clinic.questionnaire

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.questionnaireresponse.QuestionnaireResponse

@Parcelize
data class QuestionnaireResponseSections(
    val screeningQuestionnaireResponseList: List<QuestionnaireResponse>,
    val suppliesQuestionnaireResponseList: List<QuestionnaireResponse>,
) : Parcelable
