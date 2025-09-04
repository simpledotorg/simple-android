package org.simple.clinic.medicalhistory.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import org.simple.clinic.R
import org.simple.clinic.common.ui.theme.SimpleTheme
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion

@Composable
fun HistoryContainer(
    heartAttackAnswer: Answer?,
    strokeAnswer: Answer?,
    kidneyAnswer: Answer?,
    diabetesAnswer: Answer?,
    showDiabetesQuestion: Boolean,
    modifier: Modifier = Modifier,
    onAnswerChange: (MedicalHistoryQuestion, Answer) -> Unit,
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .padding(horizontal = dimensionResource(R.dimen.spacing_16))
                .padding(
                    top = dimensionResource(R.dimen.spacing_16),
                    bottom = dimensionResource(R.dimen.spacing_4)
                )
        ) {
            Text(
                text = stringResource(R.string.medicalhistorysummaryview_history),
                style = SimpleTheme.typography.subtitle1Medium,
                color = MaterialTheme.colors.onSurface,
            )

            Spacer(Modifier.requiredHeight(dimensionResource(R.dimen.spacing_4)))

            MedicalHistoryQuestionItem(
                question = MedicalHistoryQuestion.HasHadAHeartAttack,
                selectedAnswer = heartAttackAnswer,
                showDivider = true,
            ) {
                onAnswerChange(MedicalHistoryQuestion.HasHadAHeartAttack, it)
            }

            MedicalHistoryQuestionItem(
                question = MedicalHistoryQuestion.HasHadAStroke,
                selectedAnswer = strokeAnswer,
                showDivider = true,
            ) {
                onAnswerChange(MedicalHistoryQuestion.HasHadAStroke, it)
            }

            MedicalHistoryQuestionItem(
                question = MedicalHistoryQuestion.HasHadAKidneyDisease,
                selectedAnswer = kidneyAnswer,
                showDivider = showDiabetesQuestion,
            ) {
                onAnswerChange(MedicalHistoryQuestion.HasHadAKidneyDisease, it)
            }

            if (showDiabetesQuestion) {
                MedicalHistoryQuestionItem(
                    question = MedicalHistoryQuestion.DiagnosedWithDiabetes,
                    selectedAnswer = diabetesAnswer,
                    showDivider = false,
                ) {
                    onAnswerChange(MedicalHistoryQuestion.DiagnosedWithDiabetes, it)
                }
            }
        }
    }
}

