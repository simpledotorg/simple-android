package org.simple.clinic.summary.bloodpressures.ui

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
import org.simple.clinic.R
import org.simple.clinic.common.ui.components.ButtonSize
import org.simple.clinic.common.ui.components.TextButton
import org.simple.clinic.common.ui.theme.SimpleTheme
import java.util.UUID

@Composable
fun BloodPressureSummary(
    summaryItems: List<BloodPressureSummaryItem>,
    canShowSeeAllButton: Boolean,
    onSeeAllClick: () -> Unit,
    onAddBPClick: () -> Unit,
    onEditBPClick: (UUID) -> Unit,
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
        val label = stringResource(id = R.string.patientsummary_bp_add_new).uppercase()
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
          BloodPressureSummaryItem(
              item = summaryItem,
              onEdit = {
                onEditBPClick(summaryItem.id)
              },
          )
        }
      }
    } else {
      Text(
          text = stringResource(id = R.string.patientsummary_bp_none_added),
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
private fun BloodPressureSummaryPreview() {
  SimpleTheme {
    BloodPressureSummary(
        summaryItems = listOf(
            BloodPressureSummaryItem(
                id = UUID.fromString("dbf8d3f5-87e2-4f90-ab53-1c250737af95"),
                systolic = 120,
                diastolic = 80,
                date = "12-Jun-2025",
                time = "08:10 am",
                isHigh = false,
                canEdit = false,
            ),
            BloodPressureSummaryItem(
                id = UUID.fromString("dbf8d3f5-87e2-4f90-ab53-1c250737af95"),
                systolic = 120,
                diastolic = 80,
                date = "12-Jun-2025",
                time = "08:00 am",
                isHigh = false,
                canEdit = false,
            ),
            BloodPressureSummaryItem(
                id = UUID.fromString("5e29fbfb-760b-4bf2-bbf3-2ece69641ac9"),
                systolic = 140,
                diastolic = 90,
                date = "12-Jun-2025",
                time = null,
                isHigh = true,
                canEdit = false,
            ),
        ),
        canShowSeeAllButton = true,
        onSeeAllClick = {
          // no-op
        },
        onAddBPClick = {
          // no-op
        },
        onEditBPClick = {
          // no-op
        }
    )
  }
}
