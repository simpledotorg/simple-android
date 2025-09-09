package org.simple.clinic.medicalhistory.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import org.simple.clinic.R
import org.simple.clinic.common.ui.theme.SimpleTheme
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion

@Composable
fun MedicalHistoryQuestionItem(
    question: MedicalHistoryQuestion,
    selectedAnswer: Answer?,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true,
    onSelectionChange: (Answer) -> Unit,
) {
  Column(
      modifier = modifier
          .background(MaterialTheme.colors.surface)
  ) {
    Row(
        modifier = Modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_16))
    ) {
      Text(
          modifier = Modifier.weight(1f),
          text = stringResource(question.questionRes),
          style = MaterialTheme.typography.body1,
          color = MaterialTheme.colors.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
      )

      MedicalHistoryQuestionOptions(
          selectedAnswer = selectedAnswer,
          onSelectionChange = { newAnswer ->
            if (newAnswer == selectedAnswer) {
              onSelectionChange(Answer.Unanswered)
            } else {
              onSelectionChange(newAnswer)
            }
          }
      )
    }

    if (showDivider) {
      Divider(color = SimpleTheme.colors.onSurface11)
    }
  }
}


@Preview
@Composable
private fun MedicalHistoryQuestionItemPreview() {
  SimpleTheme {
    MedicalHistoryQuestionItem(
        question = MedicalHistoryQuestion.DiagnosedWithHypertension,
        selectedAnswer = null,
        showDivider = true,
        onSelectionChange = {
          // no-op
        }
    )
  }
}

@Preview
@Composable
private fun MedicalHistoryQuestionItemYesPreview() {
  SimpleTheme {
    MedicalHistoryQuestionItem(
        question = MedicalHistoryQuestion.DiagnosedWithDiabetes,
        selectedAnswer = Answer.Yes,
        showDivider = true,
        onSelectionChange = {
          // no-op
        }
    )
  }
}
