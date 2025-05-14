package org.simple.clinic.common.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.simple.clinic.common.ui.theme.SimpleTheme

@Composable
fun SearchButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
  androidx.compose.material.OutlinedButton(
      modifier = modifier.defaultMinSize(
          minWidth = 64.dp,
          minHeight = 56.dp
      ),
      onClick = onClick,
      shape = RoundedCornerShape(size = 2.dp),
      enabled = enabled,
      border = if (enabled) {
        BorderStroke(width = 1.dp, SimpleTheme.colors.material.primary)
      } else {
        BorderStroke(width = 1.dp, SimpleTheme.colors.material.onSurface.copy(alpha = 0.16f))
      }
  ) {
    Icon(
        imageVector = Icons.Outlined.Search,
        contentDescription = "Search Overdue Patient"
    )
    Spacer(modifier = Modifier.requiredWidth(8.dp))
    ProvideTextStyle(value = SimpleTheme.typography.body0) {
      content()
    }
  }
}
