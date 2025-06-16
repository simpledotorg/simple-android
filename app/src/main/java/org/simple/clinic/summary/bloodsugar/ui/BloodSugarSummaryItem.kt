package org.simple.clinic.summary.bloodsugar.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import org.simple.clinic.R
import org.simple.clinic.bloodsugar.BloodSugarReading
import org.simple.clinic.bloodsugar.BloodSugarUnitPreference
import org.simple.clinic.bloodsugar.Fasting
import org.simple.clinic.bloodsugar.HbA1c
import org.simple.clinic.bloodsugar.PostPrandial
import org.simple.clinic.common.ui.theme.SimpleTheme
import java.util.UUID

@Composable
fun BloodSugarSummaryItem(
    item: BloodSugarSummaryItem,
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
    val bloodSugarIcon = if (item.reading.isHigh || item.reading.isLow) {
      R.drawable.ic_blood_sugar_filled
    } else {
      R.drawable.ic_blood_sugar_outline
    }
    Image(
        painter = painterResource(bloodSugarIcon),
        contentDescription = null,
        modifier = Modifier.size(dimensionResource(id = R.dimen.spacing_16))
    )

    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_12)))

    BloodSugarReadingText(item = item)

    BloodSugarLevelIndicator(
        isHigh = item.reading.isHigh,
        isLow = item.reading.isLow,
    )

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

    BloodSugarDateTimeText(item = item)
  }
}

@Composable
private fun BloodSugarReadingText(item: BloodSugarSummaryItem) {
  val context = LocalContext.current
  val displayUnit = context.getString(item.reading.displayUnit(item.measurementUnit))
  val displayType = context.getString(item.reading.displayType)
  val readingPrefix = item.reading.displayValue(item.measurementUnit)
  val readingSuffix = "$displayUnit $displayType"

  Text(
      text = "$readingPrefix${item.reading.displayUnitSeparator}$readingSuffix",
      style = MaterialTheme.typography.body1,
      color = MaterialTheme.colors.onSurface,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
  )
}

@Composable
private fun RowScope.BloodSugarDateTimeText(item: BloodSugarSummaryItem) {
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
      modifier = Modifier.Companion.weight(1f),
      text = dateTimeString,
      style = dateTimeStyle,
      color = SimpleTheme.colors.onSurface67,
      textAlign = TextAlign.End,
  )
}

@Composable
private fun BloodSugarLevelIndicator(isHigh: Boolean, isLow: Boolean) {
  if (isHigh || isLow) {
    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_8)))

    @Suppress("KotlinConstantConditions")
    val bloodSugarLevelText = when {
      isLow -> stringResource(R.string.bloodsugar_level_low)
      isHigh -> stringResource(R.string.bloodsugar_level_high)
      else -> {
        throw IllegalStateException("Unknown blood sugar level")
      }
    }

    Text(
        text = bloodSugarLevelText,
        style = MaterialTheme.typography.body2,
        color = MaterialTheme.colors.error,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
  }
}

data class BloodSugarSummaryItem(
    val id: UUID,
    val reading: BloodSugarReading,
    val measurementUnit: BloodSugarUnitPreference,
    val date: String,
    val time: String?,
    val canEdit: Boolean,
)

@Preview
@Composable
private fun BloodPressureSummaryItemPreview() {
  SimpleTheme {
    BloodSugarSummaryItem(
        item = BloodSugarSummaryItem(
            id = UUID.fromString("47a26ccd-f708-435c-8955-7061edaa0452"),
            reading = BloodSugarReading(
                value = "75",
                type = PostPrandial
            ),
            measurementUnit = BloodSugarUnitPreference.Mg,
            date = "12-Jun-2025",
            time = null,
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
    BloodSugarSummaryItem(
        item = BloodSugarSummaryItem(
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
        onEdit = {
          // no-op
        }
    )
  }
}

@Preview
@Composable
private fun BloodPressureSummaryItemLowPreview() {
  SimpleTheme {
    BloodSugarSummaryItem(
        item = BloodSugarSummaryItem(
            id = UUID.fromString("5e29fbfb-760b-4bf2-bbf3-2ece69641ac9"),
            reading = BloodSugarReading(
                value = "30",
                type = PostPrandial
            ),
            measurementUnit = BloodSugarUnitPreference.Mg,
            date = "12-Jun-2025",
            time = null,
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
    BloodSugarSummaryItem(
        item = BloodSugarSummaryItem(
            id = UUID.fromString("5e29fbfb-760b-4bf2-bbf3-2ece69641ac9"),
            reading = BloodSugarReading(
                value = "250",
                type = PostPrandial
            ),
            measurementUnit = BloodSugarUnitPreference.Mg,
            date = "12-Jun-2025",
            time = null,
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
    BloodSugarSummaryItem(
        item = BloodSugarSummaryItem(
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
    BloodSugarSummaryItem(
        item = BloodSugarSummaryItem(
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
        onEdit = {
          // no-op
        }
    )
  }
}
