package org.simple.clinic.home.overdue.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.simple.clinic.common.ui.theme.SimpleTheme
import org.simple.clinic.home.overdue.OverdueAppointmentSectionTitle
import java.util.Locale

@Composable
fun OverdueSectionHeader(
    modifier: Modifier = Modifier,
    headerText: String,
    count: Int,
    isExpanded: Boolean,
    overdueAppointmentSectionTitle: OverdueAppointmentSectionTitle,
    locale: Locale,
    onClick: (OverdueAppointmentSectionTitle) -> Unit
) {
  val chevronIcon = if (isExpanded) {
    Icons.Outlined.KeyboardArrowDown
  } else {
    Icons.Outlined.ChevronRight
  }

  Row(
      modifier = modifier
          .fillMaxWidth()
          .clickable { onClick(overdueAppointmentSectionTitle) }
          .padding(
              horizontal = 8.dp,
              vertical = 16.dp
          ),
      verticalAlignment = Alignment.CenterVertically
  ) {

    Text(
        text = headerText.uppercase(locale),
        modifier = Modifier.weight(1f),
        style = SimpleTheme.typography.tag.copy(color = SimpleTheme.colors.onSurface67)
    )

    Text(
        modifier = Modifier.padding(horizontal = 8.dp),
        text = String.format(locale, "%d", count),
        style = SimpleTheme.typography.tag.copy(color = SimpleTheme.colors.material.primary)
    )

    Icon(
        chevronIcon,
        tint = SimpleTheme.colors.material.primary,
        contentDescription = null,
    )
  }
}

@Preview
@Composable
private fun OverdueSectionHeaderPreview(modifier: Modifier = Modifier) {
  SimpleTheme {
    OverdueSectionHeader(
        headerText = "Pending to call",
        count = 40,
        isExpanded = false,
        overdueAppointmentSectionTitle = OverdueAppointmentSectionTitle.MORE_THAN_A_YEAR_OVERDUE,
        locale = Locale.US
    ) {
    }
  }
}

