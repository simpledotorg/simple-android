package org.simple.clinic.summary.bloodsugar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.simple.clinic.R
import org.simple.clinic.bloodsugar.BloodSugarMeasurementType
import org.simple.clinic.bloodsugar.BloodSugarReading
import org.simple.clinic.bloodsugar.BloodSugarUnitPreference
import org.simple.clinic.bloodsugar.Fasting
import org.simple.clinic.bloodsugar.HbA1c
import org.simple.clinic.bloodsugar.PostPrandial
import org.simple.clinic.common.ui.components.ButtonSize
import org.simple.clinic.common.ui.components.TextButton
import org.simple.clinic.common.ui.theme.SimpleTheme
import java.util.UUID

@Composable
fun BloodSugarSummary(
    summaryItems: List<BloodSugarSummaryItem>,
    canShowSeeAllButton: Boolean,
    onSeeAllClick: () -> Unit,
    onAddBPClick: () -> Unit,
    onEditBPClick: (UUID, BloodSugarMeasurementType) -> Unit,
    modifier: Modifier = Modifier
) {
  Column(
      modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colors.surface)
          .then(modifier)
  ) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      TextButton(
          leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_add_circle_blue1_24dp),
                contentDescription = null
            )
          },
          onClick = onAddBPClick,
          buttonSize = ButtonSize.Small,
      ) {
        val label = stringResource(id = R.string.bloodsugarsummaryview_add_blood_sugar_button).uppercase()
        Text(text = label)
      }

      if (canShowSeeAllButton) {
        TextButton(
            trailingIcon = {
              Icon(
                  painter = painterResource(id = R.drawable.ic_chevron_right_blue1_24dp),
                  contentDescription = null
              )
            },
            onClick = onSeeAllClick,
            buttonSize = ButtonSize.Small,
        ) {
          val label = stringResource(id = R.string.bloodpressuresummaryview_see_all_button).uppercase()
          Text(text = label)
        }
      }
    }

    Divider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(id = R.dimen.spacing_8)),
        color = SimpleTheme.colors.onSurface11,
    )

    if (summaryItems.isNotEmpty()) {
      Column(
          modifier = Modifier
              .fillMaxWidth()
              .padding(top = dimensionResource(id = R.dimen.spacing_4))
      ) {
        summaryItems.forEach { summaryItem ->
          BloodSugarSummaryItem(
              item = summaryItem,
              onEdit = {
                onEditBPClick(summaryItem.id, summaryItem.reading.type)
              },
          )
        }
      }
    } else {
      Text(
          text = stringResource(id = R.string.bloodsugarsummaryview_no_blood_sugars),
          modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = dimensionResource(id = R.dimen.spacing_16)),
          textAlign = TextAlign.Center,
          style = MaterialTheme.typography.body1,
          color = SimpleTheme.colors.onSurface67
      )
    }

    when {
      summaryItems.size > 1 -> {
        Spacer(Modifier.requiredHeight(dimensionResource(R.dimen.spacing_8)))
      }

      summaryItems.isNotEmpty() -> {
        Spacer(Modifier.requiredHeight(dimensionResource(R.dimen.spacing_24)))
      }

      else -> {
        // no-op
      }
    }
  }
}

@Preview
@Composable
private fun BloodSugarSummaryPreview() {
  SimpleTheme {
    BloodSugarSummary(
        summaryItems = listOf(
            BloodSugarSummaryItem(
                id = UUID.fromString("dbf8d3f5-87e2-4f90-ab53-1c250737af95"),
                reading = BloodSugarReading(
                    value = "80",
                    type = PostPrandial
                ),
                measurementUnit = BloodSugarUnitPreference.Mg,
                date = "12-Jun-2025",
                time = "08:10 am",
                canEdit = false,
            ),
            BloodSugarSummaryItem(
                id = UUID.fromString("dbf8d3f5-87e2-4f90-ab53-1c250737af95"),
                reading = BloodSugarReading(
                    value = "75",
                    type = PostPrandial
                ),
                measurementUnit = BloodSugarUnitPreference.Mg,
                date = "12-Jun-2025",
                time = "08:00 am",
                canEdit = false,
            ),
            BloodSugarSummaryItem(
                id = UUID.fromString("0057f476-dd45-47c0-9797-21427aa86766"),
                reading = BloodSugarReading(
                    value = "7.5",
                    type = HbA1c
                ),
                measurementUnit = BloodSugarUnitPreference.Mg,
                date = "12-Jun-2025",
                time = null,
                canEdit = true,
            ),
            BloodSugarSummaryItem(
                id = UUID.fromString("b309c90f-1391-4ab4-9991-09a4eae21631"),
                reading = BloodSugarReading(
                    value = "250",
                    type = Fasting
                ),
                measurementUnit = BloodSugarUnitPreference.Mg,
                date = "12-Jun-2025",
                time = null,
                canEdit = true,
            ),
        ),
        canShowSeeAllButton = true,
        onSeeAllClick = {
          // no-op
        },
        onAddBPClick = {
          // no-op
        },
        onEditBPClick = { _, _ ->
          // no-op
        }
    )
  }
}
