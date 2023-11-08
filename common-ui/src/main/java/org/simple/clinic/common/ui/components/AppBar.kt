package org.simple.clinic.common.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.simple.clinic.common.ui.theme.SimpleTheme
import androidx.compose.material.TopAppBar as MaterialTopAppBar

@Composable
fun TopAppBar(
    navigationIcon: @Composable () -> Unit,
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    actions: (@Composable RowScope.() -> Unit)? = null,
) {
  MaterialTopAppBar(
      modifier = modifier,
      backgroundColor = SimpleTheme.colors.toolbarPrimary,
      contentColor = SimpleTheme.colors.onToolbarPrimary
  ) {
    CompositionLocalProvider(LocalContentAlpha provides 1.0f) {
      Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
      ) {
        navigationIcon()
        Spacer(modifier = Modifier.requiredWidth(16.dp))
        Box(modifier = Modifier.weight(1f)) {
          ProvideTextStyle(value = SimpleTheme.typography.material.h6) {
            title()
          }
        }

        if (actions != null) {
          Row(
              Modifier.fillMaxHeight(),
              horizontalArrangement = Arrangement.End,
              verticalAlignment = Alignment.CenterVertically,
              content = actions
          )
        }
      }
    }
  }
}

@Preview
@Composable
private fun TopAppBarPreview() {
  SimpleTheme {
    TopAppBar(
        navigationIcon = {
          IconButton(onClick = { /*TODO*/ }) {
            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null)
          }
        },
        title = {
          Text(text = "Title")
        },
    )
  }
}

@Preview
@Composable
private fun TopAppBarWithActionsPreview() {
  SimpleTheme {
    TopAppBar(
        navigationIcon = {
          IconButton(onClick = { /*TODO*/ }) {
            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null)
          }
        },
        title = {
          Text(text = "Title")
        },
        actions = {
          IconButton(onClick = { /*TODO*/ }) {
            Icon(imageVector = Icons.Filled.Search, contentDescription = null)
          }
          IconButton(onClick = { /*TODO*/ }) {
            Icon(imageVector = Icons.Filled.MoreVert, contentDescription = null)
          }
        }
    )
  }
}
