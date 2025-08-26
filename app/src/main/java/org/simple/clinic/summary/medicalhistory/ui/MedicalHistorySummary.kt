package org.simple.clinic.summary.medicalhistory.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import org.simple.clinic.medicalhistory.ui.MedicalHistoryQuestionItem
import org.simple.clinic.medicalhistory.ui.TobaccoQuestion

@Composable
fun MedicalHistorySummary(
    hypertensionAnswer: Answer?,
    diabetesAnswer: Answer?,
    heartAttackAnswer: Answer?,
    strokeAnswer: Answer?,
    kidneyAnswer: Answer?,
    isSmokingAnswer: Answer?,
    isUsingSmokelessTobaccoAnswer: Answer?,
    diabetesManagementEnabled: Boolean,
    showSmokerQuestion: Boolean,
    showSmokelessTobaccoQuestion: Boolean,
    modifier: Modifier = Modifier,
    onAnswerChange: (MedicalHistoryQuestion, Answer) -> Unit,
) {
  Column(
      modifier = modifier
          .fillMaxWidth()
          .padding(dimensionResource(R.dimen.spacing_8)),
      verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_8))
  ) {
    if (diabetesManagementEnabled) {
      DiagnosisContainer(
          modifier = Modifier.fillMaxWidth(),
          hypertensionAnswer = hypertensionAnswer,
          diabetesAnswer = diabetesAnswer,
          onAnswerChange = onAnswerChange,
      )
    }

    HistoryContainer(
        heartAttackAnswer = heartAttackAnswer,
        strokeAnswer = strokeAnswer,
        kidneyAnswer = kidneyAnswer,
        diabetesAnswer = diabetesAnswer,
        diabetesManagementEnabled = diabetesManagementEnabled,
        onAnswerChange = onAnswerChange
    )

    if (showSmokerQuestion) {
      TobaccoQuestion(
          isSmokingAnswer = isSmokingAnswer,
          isUsingSmokelessTobaccoAnswer = isUsingSmokelessTobaccoAnswer,
          showSmokelessTobaccoQuestion = showSmokelessTobaccoQuestion,
          onAnswerChange = onAnswerChange
      )
    }
  }
}

@Composable
fun HistoryContainer(
    heartAttackAnswer: Answer?,
    strokeAnswer: Answer?,
    kidneyAnswer: Answer?,
    diabetesAnswer: Answer?,
    diabetesManagementEnabled: Boolean,
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
          showDivider = !diabetesManagementEnabled,
      ) {
        onAnswerChange(MedicalHistoryQuestion.HasHadAKidneyDisease, it)
      }

      if (!diabetesManagementEnabled) {
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

@Composable
private fun DiagnosisContainer(
    hypertensionAnswer: Answer?,
    diabetesAnswer: Answer?,
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
          text = stringResource(R.string.medicalhistorysummaryview_diagnosis),
          style = SimpleTheme.typography.subtitle1Medium,
          color = MaterialTheme.colors.onSurface,
      )

      Spacer(Modifier.requiredHeight(dimensionResource(R.dimen.spacing_4)))

      MedicalHistoryQuestionItem(
          question = MedicalHistoryQuestion.DiagnosedWithHypertension,
          selectedAnswer = hypertensionAnswer,
          showDivider = true,
      ) {
        onAnswerChange(MedicalHistoryQuestion.DiagnosedWithHypertension, it)
      }

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

@Preview
@Composable
private fun MedicalHistorySummaryPreview() {
  SimpleTheme {
    MedicalHistorySummary(
        hypertensionAnswer = Answer.Yes,
        diabetesAnswer = null,
        heartAttackAnswer = Answer.Yes,
        strokeAnswer = Answer.No,
        kidneyAnswer = null,
        isSmokingAnswer = null,
        isUsingSmokelessTobaccoAnswer = null,
        diabetesManagementEnabled = true,
        showSmokerQuestion = false,
        showSmokelessTobaccoQuestion = false,
        onAnswerChange = { _, _ ->
          // no-op
        }
    )
  }
}

@Preview
@Composable
private fun MedicalHistorySummaryNoDiabetesManagementPreview() {
  SimpleTheme {
    MedicalHistorySummary(
        hypertensionAnswer = Answer.Yes,
        diabetesAnswer = null,
        heartAttackAnswer = Answer.Yes,
        strokeAnswer = Answer.No,
        kidneyAnswer = null,
        isSmokingAnswer = null,
        isUsingSmokelessTobaccoAnswer = null,
        diabetesManagementEnabled = false,
        showSmokerQuestion = false,
        showSmokelessTobaccoQuestion = false,
        onAnswerChange = { _, _ ->
          // no-op
        }
    )
  }
}

@Preview
@Composable
private fun MedicalHistorySummarySmokerPreview() {
  SimpleTheme {
    MedicalHistorySummary(
        hypertensionAnswer = Answer.Yes,
        diabetesAnswer = null,
        heartAttackAnswer = Answer.Yes,
        strokeAnswer = Answer.No,
        kidneyAnswer = null,
        isSmokingAnswer = Answer.Yes,
        isUsingSmokelessTobaccoAnswer = Answer.Yes,
        diabetesManagementEnabled = true,
        showSmokerQuestion = true,
        showSmokelessTobaccoQuestion = false,
        onAnswerChange = { _, _ ->
          // no-op
        }
    )
  }
}

@Preview
@Composable
private fun MedicalHistorySummaryTobaccoUsePreview() {
  SimpleTheme {
    MedicalHistorySummary(
        hypertensionAnswer = Answer.Yes,
        diabetesAnswer = null,
        heartAttackAnswer = Answer.Yes,
        strokeAnswer = Answer.No,
        kidneyAnswer = null,
        isSmokingAnswer = Answer.Yes,
        isUsingSmokelessTobaccoAnswer = Answer.Yes,
        diabetesManagementEnabled = true,
        showSmokerQuestion = true,
        showSmokelessTobaccoQuestion = true,
        onAnswerChange = { _, _ ->
          // no-op
        }
    )
  }
}
