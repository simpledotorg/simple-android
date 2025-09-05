package org.simple.clinic.medicalhistory.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ChipDefaults
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
import org.simple.clinic.R
import org.simple.clinic.medicalhistory.Answer

@Composable
fun AnswerChipsGroup(
    modifier: Modifier = Modifier,
    selectedAnswer: Answer?,
    onSelectionChange: (Answer) -> Unit
) {
  Row(
      modifier = modifier,
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
fun MedicalHistoryAnswerChip(
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
