package org.simple.clinic.medicalhistory.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import org.simple.clinic.R
import org.simple.clinic.common.ui.theme.SimpleTheme
import org.simple.clinic.medicalhistory.Answer

enum class MedicalHistoryQuestionOptionOrientation { Horizontal, Vertical }

@Composable
fun MedicalHistoryQuestionOptions(
    modifier: Modifier = Modifier,
    options: List<Answer> = listOf(Answer.Yes, Answer.No),
    selectedAnswer: Answer?,
    onAnswerChange: (Answer) -> Unit,
    orientation: MedicalHistoryQuestionOptionOrientation = MedicalHistoryQuestionOptionOrientation.Horizontal,
) {
  val layoutModifier = modifier.selectableGroup()

  if (orientation == MedicalHistoryQuestionOptionOrientation.Horizontal) {
    Row(
        layoutModifier,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_12)),
        verticalAlignment = Alignment.CenterVertically
    ) {
      options.forEach { answer ->
        RadioOptionItem(
            answer = answer,
            selected = answer == selectedAnswer,
            onSelect = { onAnswerChange(answer) },
        )
      }
    }
  } else {
    Column(
        layoutModifier,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_12)),
        horizontalAlignment = Alignment.Start
    ) {
      options.forEach { answer ->
        RadioOptionItem(
            answer = answer,
            selected = answer == selectedAnswer,
            onSelect = { onAnswerChange(answer) },
        )
      }
    }
  }
}

@Composable
private fun RadioOptionItem(
    answer: Answer,
    selected: Boolean,
    onSelect: () -> Unit,
) {
  Row(
      Modifier
          .selectable(
              selected = selected,
              onClick = onSelect,
              role = Role.RadioButton,
              indication = null,
              interactionSource = remember { MutableInteractionSource() }
          ),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_8))
  ) {
    RadioButton(
        selected = selected,
        onClick = null,
        colors = RadioButtonDefaults.colors(
            selectedColor = MaterialTheme.colors.primary
        )
    )
    Text(
        text = answer.labelRes?.let { stringResource(it) }.orEmpty(),
        style = MaterialTheme.typography.body1,
        color = MaterialTheme.colors.onSurface
    )
  }
}

@Preview(showBackground = true, name = "Horizontal – Yes/No")
@Composable
private fun PreviewAnswerRadioGroupHorizontal() {
  SimpleTheme {
    MedicalHistoryQuestionOptions(
        options = listOf(Answer.Yes, Answer.No),
        selectedAnswer = Answer.Yes,
        onAnswerChange = {},
        orientation = MedicalHistoryQuestionOptionOrientation.Horizontal
    )
  }
}

@Preview(showBackground = true, name = "Vertical – Yes/No/Suspected")
@Composable
private fun PreviewAnswerRadioGroupVertical() {
  SimpleTheme {
    MedicalHistoryQuestionOptions(
        options = listOf(Answer.Yes, Answer.No, Answer.Suspected),
        selectedAnswer = Answer.Suspected,
        onAnswerChange = {},
        orientation = MedicalHistoryQuestionOptionOrientation.Vertical
    )
  }
}

@Preview(showBackground = true, name = "Horizontal – Three Options")
@Composable
private fun PreviewAnswerRadioGroupThreeOptions() {
  SimpleTheme {
    MedicalHistoryQuestionOptions(
        options = listOf(Answer.Yes, Answer.No, Answer.Suspected),
        selectedAnswer = null,
        onAnswerChange = {},
        orientation = MedicalHistoryQuestionOptionOrientation.Horizontal
    )
  }
}

@Preview(showBackground = true, name = "Vertical – No Selection")
@Composable
private fun PreviewAnswerRadioGroupNoSelection() {
  SimpleTheme {
    MedicalHistoryQuestionOptions(
        options = listOf(Answer.Yes, Answer.No),
        selectedAnswer = null,
        onAnswerChange = {},
        orientation = MedicalHistoryQuestionOptionOrientation.Vertical
    )
  }
}

