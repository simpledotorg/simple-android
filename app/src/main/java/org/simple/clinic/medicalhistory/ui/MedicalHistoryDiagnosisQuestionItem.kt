package org.simple.clinic.medicalhistory.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import org.simple.clinic.R
import org.simple.clinic.common.ui.theme.SimpleTheme
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion

@Composable
fun MedicalHistoryDiagnosisQuestionItem(
    header: String,
    question: MedicalHistoryQuestion,
    selectedAnswer: Answer?,
    options: List<Answer> = listOf(Answer.Yes, Answer.No),
    orientation: MedicalHistoryQuestionOptionOrientation = MedicalHistoryQuestionOptionOrientation.Vertical,
    onAnswerChange: (MedicalHistoryQuestion, Answer) -> Unit,
) {
  Column {
    Text(
        text = buildAnnotatedString {
          append(header)
          withStyle(style = SpanStyle(color = MaterialTheme.colors.error)) {
            append(" *")
          }
        },
        style = SimpleTheme.typography.subtitle1Medium.copy(color = MaterialTheme.colors.onSurface),
    )

    Spacer(Modifier.requiredHeight(dimensionResource(R.dimen.spacing_12)))

    MedicalHistoryQuestionOptions(
        options = options,
        selectedAnswer = selectedAnswer,
        onAnswerChange = { answer -> onAnswerChange(question, answer) },
        orientation = orientation
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun MedicalHistoryDiagnosisQuestionItemPreview() {
  SimpleTheme {
    MedicalHistoryDiagnosisQuestionItem(
        header = stringResource(R.string.medicalhistorysummaryview_hypertension_diagnosis),
        question = MedicalHistoryQuestion.DiagnosedWithHypertension,
        selectedAnswer = Answer.Unanswered,
        onAnswerChange = { _, _ ->
          // no-op
        }
    )
  }
}
