package org.simple.clinic.summary.bloodpressures.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import org.simple.clinic.R
import org.simple.clinic.common.ui.theme.SimpleTheme
import java.util.UUID

@Composable
fun BloodPressureSummaryItem(
    item: BloodPressureSummaryItem,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit,
) {
  val interactionSource = remember { MutableInteractionSource() }
  Row(
      modifier = Modifier
          .then(modifier)
          .fillMaxWidth()
          .clickable(
              enabled = item.canEdit,
              interactionSource = interactionSource,
              indication = LocalIndication.current
          ) { onEdit() }
          .padding(
              horizontal = dimensionResource(id = R.dimen.spacing_12),
              vertical = dimensionResource(id = R.dimen.spacing_4),
          ),
      verticalAlignment = Alignment.CenterVertically
  ) {
    val bpIcon = if (item.isHigh) {
      R.drawable.bp_reading_high
    } else {
      R.drawable.bp_reading_normal
    }
    Image(
        painter = painterResource(bpIcon),
        contentDescription = null,
        modifier = Modifier.size(dimensionResource(id = R.dimen.spacing_16))
    )

    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_12)))

    Text(
        text = "${item.systolic} / ${item.diastolic}",
        style = MaterialTheme.typography.body1,
        color = MaterialTheme.colors.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )

    if (item.isHigh) {
      Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_8)))

      Text(
          text = stringResource(R.string.bloodpressure_level_high),
          style = MaterialTheme.typography.body2,
          color = MaterialTheme.colors.error,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
      )
    }

    if (item.canEdit) {
      Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_8)))

      Text(
          modifier = Modifier.clickable(
              interactionSource = interactionSource,
              indication = null,
          ) { onEdit() },
          text = stringResource(R.string.patientsummary_edit).uppercase(),
          style = MaterialTheme.typography.button,
          color = MaterialTheme.colors.primary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
      )
    }

    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_8)))

    val dateTimeStyle = if (item.time.isNullOrBlank()) {
      MaterialTheme.typography.body2
    } else {
      MaterialTheme.typography.caption
    }
    val dateTimeString = if (item.time.isNullOrBlank()) {
      item.date
    } else {
      stringResource(R.string.patientsummary_newbp_date_time, item.date, item.time)
    }

    Text(
        modifier = Modifier.weight(1f),
        text = dateTimeString,
        style = dateTimeStyle,
        color = SimpleTheme.colors.onSurface67,
        textAlign = TextAlign.End,
    )
  }
}

data class BloodPressureSummaryItem(
    val id: UUID,
    val systolic: Int,
    val diastolic: Int,
    val date: String,
    val time: String?,
    val isHigh: Boolean,
    val canEdit: Boolean,
)

@Preview
@Composable
private fun BloodPressureSummaryItemPreview() {
  SimpleTheme {
    BloodPressureSummaryItem(
        item = BloodPressureSummaryItem(
            id = UUID.fromString("47a26ccd-f708-435c-8955-7061edaa0452"),
            systolic = 120,
            diastolic = 80,
            date = "12-Jun-2025",
            time = null,
            isHigh = false,
            canEdit = false,
        ),
        onEdit = {
          // no-op
        }
    )
  }
}

@Preview
@Composable
private fun BloodPressureSummaryItemMultilineDateTimePreview() {
  SimpleTheme {
    BloodPressureSummaryItem(
        item = BloodPressureSummaryItem(
            id = UUID.fromString("dbf8d3f5-87e2-4f90-ab53-1c250737af95"),
            systolic = 120,
            diastolic = 80,
            date = "12-Jun-2025",
            time = "08:00 am",
            isHigh = false,
            canEdit = false,
        ),
        onEdit = {
          // no-op
        }
    )
  }
}

@Preview
@Composable
private fun BloodPressureSummaryItemHighPreview() {
  SimpleTheme {
    BloodPressureSummaryItem(
        item = BloodPressureSummaryItem(
            id = UUID.fromString("5e29fbfb-760b-4bf2-bbf3-2ece69641ac9"),
            systolic = 140,
            diastolic = 90,
            date = "12-Jun-2025",
            time = null,
            isHigh = true,
            canEdit = false,
        ),
        onEdit = {
          // no-op
        }
    )
  }
}

@Preview
@Composable
private fun BloodPressureSummaryItemEditPreview() {
  SimpleTheme {
    BloodPressureSummaryItem(
        item = BloodPressureSummaryItem(
            id = UUID.fromString("0057f476-dd45-47c0-9797-21427aa86766"),
            systolic = 120,
            diastolic = 80,
            date = "12-Jun-2025",
            time = null,
            isHigh = false,
            canEdit = true,
        ),
        onEdit = {
          // no-op
        }
    )
  }
}

@Preview
@Composable
private fun BloodPressureSummaryItemHighAndEditPreview() {
  SimpleTheme {
    BloodPressureSummaryItem(
        item = BloodPressureSummaryItem(
            id = UUID.fromString("b309c90f-1391-4ab4-9991-09a4eae21631"),
            systolic = 140,
            diastolic = 90,
            date = "12-Jun-2025",
            time = null,
            isHigh = true,
            canEdit = true,
        ),
        onEdit = {
          // no-op
        }
    )
  }
}
