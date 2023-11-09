package org.simple.clinic.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.simple.clinic.R
import org.simple.clinic.common.ui.components.OutlinedButton
import org.simple.clinic.common.ui.components.TopAppBar
import org.simple.clinic.common.ui.theme.SimpleRedTheme
import org.simple.clinic.common.ui.theme.SimpleTheme

@Composable
fun SettingsScreen(
    model: SettingsModel,
    navigationIconClick: () -> Unit,
    changeLanguageButtonClick: () -> Unit,
    updateButtonClick: () -> Unit,
    logoutButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
  SimpleTheme {
    Scaffold(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = SimpleTheme.colors.material.surface,
        topBar = {
          TopAppBar(
              navigationIcon = {
                IconButton(onClick = navigationIconClick) {
                  Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null)
                }
              },
              title = {
                Text(
                    text = stringResource(id = R.string.settings_title),
                )
              },
          )
        }
    ) { paddingValues ->
      SettingsList(
          model = model,
          paddingValues = paddingValues,
          changeLanguageButtonClick = changeLanguageButtonClick,
          updateButtonClick = updateButtonClick,
          logoutButtonClick = logoutButtonClick
      )
    }

    if (model.isUserLoggingOut == true) {
      // Scrim
      Box(
          modifier = Modifier
              .testTag("SETTINGS_LOGGING_OUT_PROGRESS")
              .fillMaxSize()
              .background(Color.Black.copy(alpha = 0.32f))
              .pointerInput(Unit) { },
          contentAlignment = Alignment.Center
      ) {
        CircularProgressIndicator(color = Color.White)
      }
    }
  }
}

@Composable
private fun SettingsList(
    model: SettingsModel,
    paddingValues: PaddingValues,
    changeLanguageButtonClick: () -> Unit,
    updateButtonClick: () -> Unit,
    logoutButtonClick: () -> Unit
) {
  LazyColumn(
      modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues),
  ) {
    // Name
    item {
      SettingItem(
          title = stringResource(id = R.string.settings_name),
          canShowDivider = true,
          leading = {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = null
            )
          }
      ) {
        Text(
            modifier = Modifier.testTag("SETTINGS_USER_NAME"),
            text = model.name.orEmpty(),
            style = SimpleTheme.typography.material.body1,
            color = SimpleTheme.colors.material.onBackground
        )
      }
    }

    // Phone Number
    item {
      SettingItem(
          title = stringResource(id = R.string.settings_number),
          canShowDivider = true,
          leading = {
            Icon(
                imageVector = Icons.Filled.Phone,
                contentDescription = null
            )
          }
      ) {
        Text(
            modifier = Modifier.testTag("SETTINGS_USER_PHONE_NUMBER"),
            text = model.phoneNumber.orEmpty(),
            style = SimpleTheme.typography.material.body1,
            color = SimpleTheme.colors.material.onBackground
        )
      }
    }

    // Language
    if (model.isChangeLanguageFeatureEnabled) {
      item {
        val languageContent = when (model.currentLanguage) {
          is ProvidedLanguage -> model.currentLanguage.displayName
          SystemDefaultLanguage, null -> stringResource(id = R.string.settings_language_hint)
        }

        SettingItem(
            modifier = Modifier.testTag("SETTINGS_ITEM_CHANGE_LANGUAGE"),
            title = stringResource(id = R.string.settings_language),
            contentPadding = PaddingValues(start = 16.dp),
            canShowDivider = true,
            leading = {
              Icon(
                  imageVector = Icons.Filled.Language,
                  contentDescription = null
              )
            },
            action = {
              TextButton(
                  modifier = Modifier.padding(end = 8.dp),
                  onClick = changeLanguageButtonClick
              ) {
                Text(text = stringResource(id = R.string.settings_change).uppercase())
              }
            }
        ) {
          Text(
              modifier = Modifier.testTag("SETTINGS_LANGUAGE_CONTENT"),
              text = languageContent,
              style = SimpleTheme.typography.material.body1,
              color = SimpleTheme.colors.material.onBackground
          )
        }
      }
    }

    // App Version
    item {
      val updateActionButton: (@Composable () -> Unit)? = if (model.isUpdateAvailable == true) {
        {
          TextButton(
              modifier = Modifier
                  .testTag("SETTINGS_APP_UPDATE_BUTTON")
                  .padding(end = 8.dp),
              onClick = updateButtonClick
          ) {
            Text(text = stringResource(id = R.string.settings_update).uppercase())
          }
        }
      } else {
        null
      }

      SettingItem(
          title = stringResource(id = R.string.settings_software),
          contentPadding = PaddingValues(start = 16.dp),
          canShowDivider = false,
          leading = {
            Icon(
                imageVector = Icons.Filled.SystemUpdate,
                contentDescription = null
            )
          },
          action = updateActionButton
      ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
              text = stringResource(
                  id = R.string.settings_software_version,
                  model.appVersion.orEmpty()
              ),
              style = SimpleTheme.typography.material.body1,
              color = SimpleTheme.colors.material.onBackground
          )

          if (model.isDatabaseEncrypted == true) {
            Spacer(modifier = Modifier.requiredWidth(8.dp))

            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                modifier = Modifier
                    .requiredSize(16.dp)
                    .testTag("SETTINGS_APP_SECURE")
            )
          }
        }
      }
    }

    // Logout
    if (model.isLogoutUserFeatureEnabled) {
      item {
        LogoutButton(
            modifier = Modifier
                .padding(top = 16.dp)
                .testTag("SETTINGS_LOGOUT_BUTTON"),
            logout = logoutButtonClick
        )
      }
    }
  }
}

@Composable
private fun SettingItem(
    title: String,
    leading: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    canShowDivider: Boolean = true,
    action: (@Composable () -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
    content: @Composable () -> Unit,
) {
  Box(
      modifier = modifier
          .fillMaxWidth()
          .requiredHeightIn(min = 80.dp),
      contentAlignment = Alignment.Center
  ) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
      CompositionLocalProvider(
          LocalContentColor provides SimpleTheme.colors.material.onSurface.copy(alpha = 0.67f),
          LocalContentAlpha provides 1.0f
      ) { leading() }

      Spacer(modifier = Modifier.requiredWidth(16.dp))

      Column(
          modifier = Modifier
              .padding(horizontal = 12.dp)
              .weight(1f),
          verticalArrangement = Arrangement.spacedBy(2.dp)
      ) {
        Text(
            text = title, style = SimpleTheme.typography.material.caption,
            color = SimpleTheme.colors.material.onSurface.copy(alpha = 0.67f)
        )

        content()
      }

      action?.invoke()
    }

    if (canShowDivider) {
      Divider(
          modifier = Modifier
              .align(Alignment.BottomStart)
              .padding(
                  start = 56.dp,
                  end = 16.dp
              ))
    }
  }
}

@Composable
private fun LogoutButton(
    modifier: Modifier = Modifier,
    logout: () -> Unit
) {
  Box(modifier) {
    SimpleRedTheme {
      OutlinedButton(
          onClick = logout,
          modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
      ) {
        Text(text = stringResource(id = R.string.settings_logout).uppercase())
      }
    }
  }
}

private val previewSettingsModel = SettingsModel(
    name = "Riya Murthy",
    phoneNumber = "1111111111",
    currentLanguage = SystemDefaultLanguage,
    appVersion = "1.0.0",
    isUpdateAvailable = true,
    isUserLoggingOut = null,
    isDatabaseEncrypted = true,
    isChangeLanguageFeatureEnabled = true,
    isLogoutUserFeatureEnabled = true
)

@Preview
@Composable
private fun SettingItemPreview() {
  SimpleTheme {
    SettingItem(
        title = stringResource(id = R.string.settings_name),
        canShowDivider = true,
        leading = {
          Icon(imageVector = Icons.Filled.AccountCircle, contentDescription = null)
        }
    ) {
      Text(
          text = previewSettingsModel.name.orEmpty(),
          style = SimpleTheme.typography.material.body1,
          color = SimpleTheme.colors.material.onBackground
      )
    }
  }
}

@Preview
@Composable
private fun SettingsScreenContentPreview() {
  SettingsScreen(
      model = previewSettingsModel,
      navigationIconClick = {
        // no-op
      },
      changeLanguageButtonClick = {
        // no-op
      },
      updateButtonClick = {
        // no-op
      },
      logoutButtonClick = {
        // no-op
      }
  )
}
