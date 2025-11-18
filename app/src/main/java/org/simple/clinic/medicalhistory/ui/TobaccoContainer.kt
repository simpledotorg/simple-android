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
fun TobaccoContainer(
    isSmokingAnswer: Answer?,
    isUsingSmokelessTobaccoAnswer: Answer?,
    showSmokelessTobaccoQuestion: Boolean,
    onAnswerChange: (MedicalHistoryQuestion, Answer) -> Unit,
) {
  if (showSmokelessTobaccoQuestion) {
    TobaccoUseContainer(
        isSmokingAnswer = isSmokingAnswer,
        isUsingSmokelessTobaccoAnswer = isUsingSmokelessTobaccoAnswer,
        onAnswerChange = onAnswerChange
    )
  } else {
    SmokerContainer(
        smokerAnswer = isSmokingAnswer,
        onAnswerChange = onAnswerChange
    )
  }
}

@Composable
fun SmokerContainer(
    smokerAnswer: Answer?,
    modifier: Modifier = Modifier,
    onAnswerChange: (MedicalHistoryQuestion, Answer) -> Unit,
) {
  Card(modifier = modifier) {
    MedicalHistoryQuestionItem(
        modifier = Modifier
            .padding(vertical = dimensionResource(R.dimen.spacing_16))
            .padding(start = dimensionResource(R.dimen.spacing_16),
                end = dimensionResource(R.dimen.spacing_24)),
        question = MedicalHistoryQuestion.IsSmoking,
        selectedAnswer = smokerAnswer,
        onSelectionChange = {
          onAnswerChange(MedicalHistoryQuestion.IsSmoking, it)
        }
    )
  }
}

@Composable
fun TobaccoUseContainer(
    isSmokingAnswer: Answer?,
    isUsingSmokelessTobaccoAnswer: Answer?,
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
          text = stringResource(R.string.medicalhistorysummaryview_tobacco_use),
          style = SimpleTheme.typography.subtitle1Medium,
          color = MaterialTheme.colors.onSurface,
      )

      Spacer(Modifier.requiredHeight(dimensionResource(R.dimen.spacing_16)))

      MedicalHistoryQuestionItem(
          question = MedicalHistoryQuestion.IsSmoking,
          selectedAnswer = isSmokingAnswer,
      ) {
        onAnswerChange(MedicalHistoryQuestion.IsSmoking, it)
      }

      Spacer(Modifier.requiredHeight(dimensionResource(R.dimen.spacing_24)))

      MedicalHistoryQuestionItem(
          question = MedicalHistoryQuestion.IsUsingSmokelessTobacco,
          selectedAnswer = isUsingSmokelessTobaccoAnswer,
      ) {
        onAnswerChange(MedicalHistoryQuestion.IsUsingSmokelessTobacco, it)
      }
    }
  }
}

@Preview
@Composable
fun TobaccoQuestionPreview() {
  SimpleTheme {
    TobaccoContainer(
        isSmokingAnswer = Answer.Yes,
        isUsingSmokelessTobaccoAnswer = Answer.Unanswered,
        showSmokelessTobaccoQuestion = false
    ) { _, _ ->
      //no-op
    }
  }
}

@Preview
@Composable
fun TobaccoWithSmokelessQuestionPreview() {
  SimpleTheme {
    TobaccoContainer(
        isSmokingAnswer = Answer.Yes,
        isUsingSmokelessTobaccoAnswer = Answer.Yes,
        showSmokelessTobaccoQuestion = true
    ) { _, _ ->
      //no-op
    }
  }
}

