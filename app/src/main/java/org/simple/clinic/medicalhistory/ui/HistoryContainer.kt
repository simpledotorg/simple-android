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
import androidx.compose.ui.tooling.preview.Preview
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
            .padding(vertical = dimensionResource(R.dimen.spacing_16))
            .padding(start = dimensionResource(R.dimen.spacing_16),
                end = dimensionResource(R.dimen.spacing_24)),
    ) {
      Text(
          text = stringResource(R.string.medicalhistorysummaryview_history),
          style = SimpleTheme.typography.subtitle1Medium,
          color = MaterialTheme.colors.onSurface,
      )

      Spacer(Modifier.requiredHeight(dimensionResource(R.dimen.spacing_16)))

      MedicalHistoryQuestionItem(
          question = MedicalHistoryQuestion.HasHadAHeartAttack,
          selectedAnswer = heartAttackAnswer,
      ) {
        onAnswerChange(MedicalHistoryQuestion.HasHadAHeartAttack, it)
      }

      Spacer(Modifier.requiredHeight(dimensionResource(R.dimen.spacing_24)))

      MedicalHistoryQuestionItem(
          question = MedicalHistoryQuestion.HasHadAStroke,
          selectedAnswer = strokeAnswer,
      ) {
        onAnswerChange(MedicalHistoryQuestion.HasHadAStroke, it)
      }

      Spacer(Modifier.requiredHeight(dimensionResource(R.dimen.spacing_24)))

      MedicalHistoryQuestionItem(
          question = MedicalHistoryQuestion.HasHadAKidneyDisease,
          selectedAnswer = kidneyAnswer,
      ) {
        onAnswerChange(MedicalHistoryQuestion.HasHadAKidneyDisease, it)
      }

      if (showDiabetesQuestion) {

        Spacer(Modifier.requiredHeight(dimensionResource(R.dimen.spacing_24)))

        MedicalHistoryQuestionItem(
            question = MedicalHistoryQuestion.DiagnosedWithDiabetes,
            selectedAnswer = diabetesAnswer,
        ) {
          onAnswerChange(MedicalHistoryQuestion.DiagnosedWithDiabetes, it)
        }
      }
    }
  }
}

@Preview(showBackground = true, name = "History Container – With Diabetes Question")
@Composable
private fun PreviewHistoryContainer_WithDiabetes() {
  SimpleTheme {
    HistoryContainer(
        heartAttackAnswer = Answer.Yes,
        strokeAnswer = Answer.Unanswered,
        kidneyAnswer = Answer.No,
        diabetesAnswer = Answer.Unanswered,
        showDiabetesQuestion = true
    ) { _, _ ->
    }
  }
}

@Preview(showBackground = true, name = "History Container – Without Diabetes Question")
@Composable
private fun PreviewHistoryContainer_WithoutDiabetes() {
  SimpleTheme {
    HistoryContainer(
        heartAttackAnswer = Answer.Yes,
        strokeAnswer = Answer.Yes,
        kidneyAnswer = Answer.Unanswered,
        diabetesAnswer = Answer.Unanswered,
        showDiabetesQuestion = false
    ) { _, _ ->
    }
  }
}
