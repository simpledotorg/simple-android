package org.simple.clinic.medicalhistory.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ChipDefaults
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FilterChip
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import org.simple.clinic.R
import org.simple.clinic.common.ui.theme.SimpleTheme
import org.simple.clinic.medicalhistory.Answer

@Composable
fun MedicalHistoryQuestionItem(
  questionText: String,
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
      modifier = Modifier
        .padding(vertical = dimensionResource(R.dimen.spacing_8)),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_16))
    ) {
      Text(
        modifier = Modifier.weight(1f),
        text = questionText,
        style = MaterialTheme.typography.body1,
        color = MaterialTheme.colors.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )

      AnswerChipsGroup(
        selectedAnswer = selectedAnswer,
        onSelectionChange = onSelectionChange
      )
    }

    if (showDivider) {
      Divider(color = SimpleTheme.colors.onSurface11)
    }
  }
}

@Composable
private fun AnswerChipsGroup(
  selectedAnswer: Answer?,
  onSelectionChange: (Answer) -> Unit
) {
  Row(
    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_8)),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    MedicalHistoryAnswerChip(
      label = stringResource(R.string.newmedicalhistory_yes),
      selected = selectedAnswer == Answer.Yes,
      onSelectionChange = {
        onSelectionChange(Answer.Yes)
      }
    )

    MedicalHistoryAnswerChip(
      label = stringResource(R.string.newmedicalhistory_no),
      selected = selectedAnswer == Answer.No,
      onSelectionChange = {
        onSelectionChange(Answer.No)
      }
    )
  }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun MedicalHistoryAnswerChip(
  label: String,
  selected: Boolean,
  onSelectionChange: () -> Unit
) {
  val backgroundColor by animateColorAsState(
    if (selected) MaterialTheme.colors.primary else MaterialTheme.colors.primaryVariant
  )
  val textColor by animateColorAsState(
    if (selected) MaterialTheme.colors.onPrimary else MaterialTheme.colors.primary
  )

  FilterChip(
    selected = selected,
    colors = ChipDefaults.filterChipColors(
      backgroundColor = backgroundColor,
    ),
    onClick = onSelectionChange,
  ) {
    Text(
      modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.spacing_12)),
      text = label,
      style = MaterialTheme.typography.body2,
      color = textColor,
      textAlign = TextAlign.Center,
    )
  }
}

@Preview
@Composable
private fun MedicalHistoryQuestionItemPreview() {
  SimpleTheme {
    MedicalHistoryQuestionItem(
      questionText = stringResource(R.string.medicalhistory_question_heartattack),
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
      questionText = stringResource(R.string.medicalhistory_question_heartattack),
      selectedAnswer = Answer.Yes,
      showDivider = true,
      onSelectionChange = {
        // no-op
      }
    )
  }
}
