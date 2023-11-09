package org.simple.clinic.settings

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.R

class SettingsScreenTest {

  @get:Rule
  val composeRule = createAndroidComposeRule<ComponentActivity>()

  private val defaultSettingsModel = SettingsModel.default(
      isChangeLanguageFeatureEnabled = true,
      isLogoutUserFeatureEnabled = true
  )

  @Test
  fun renderUserDetailsAfterTheyAreFetched() {
    // given
    var state by mutableStateOf(defaultSettingsModel)
    composeRule.setContent {
      SettingsScreen(
          model = state,
          navigationIconClick = { /*no-op*/ },
          changeLanguageButtonClick = { /*no-op*/ },
          updateButtonClick = { /*no-op*/ },
          logoutButtonClick = { /*no-op*/ }
      )
    }

    composeRule.onNodeWithTag("SETTINGS_USER_NAME").assertTextEquals("")
    composeRule.onNodeWithTag("SETTINGS_USER_PHONE_NUMBER").assertTextEquals("")

    // when
    state = defaultSettingsModel
        .userDetailsFetched(name = "Riya", phoneNumber = "1111111111")

    // then
    composeRule.onNodeWithTag("SETTINGS_USER_NAME").assertTextEquals("Riya")
    composeRule.onNodeWithTag("SETTINGS_USER_PHONE_NUMBER").assertTextEquals("1111111111")
  }

  @Test
  fun whenCurrentLanguageIsSystemLanguageShowLanguageHint() {
    // given
    val hintString = composeRule.activity.getString(R.string.settings_language_hint)

    val updatedModel = defaultSettingsModel.currentLanguageFetched(SystemDefaultLanguage)
    composeRule.setContent {
      SettingsScreen(
          model = updatedModel,
          navigationIconClick = { /*no-op*/ },
          changeLanguageButtonClick = { /*no-op*/ },
          updateButtonClick = { /*no-op*/ },
          logoutButtonClick = { /*no-op*/ }
      )
    }

    // then
    composeRule.onNodeWithTag("SETTINGS_LANGUAGE_CONTENT").assertExists()
    composeRule.onNodeWithTag("SETTINGS_LANGUAGE_CONTENT").assertTextEquals(hintString)
  }

  @Test
  fun whenCurrentLanguageIsManuallySelectedLanguageShowLanguageDisplayName() {
    // given
    val language = ProvidedLanguage(displayName = "English", languageCode = "en-IN")
    val updatedModel = defaultSettingsModel.currentLanguageFetched(language)
    composeRule.setContent {
      SettingsScreen(
          model = updatedModel,
          navigationIconClick = { /*no-op*/ },
          changeLanguageButtonClick = { /*no-op*/ },
          updateButtonClick = { /*no-op*/ },
          logoutButtonClick = { /*no-op*/ }
      )
    }

    // then
    composeRule.onNodeWithTag("SETTINGS_LANGUAGE_CONTENT").assertExists()
    composeRule.onNodeWithTag("SETTINGS_LANGUAGE_CONTENT").assertTextEquals("English")
  }

  @Test
  fun whenAppUpdateIsAvailableThenShowAppUpdateButton() {
    // given
    val updatedModel = defaultSettingsModel.checkedAppUpdate(isUpdateAvailable = true)
    composeRule.setContent {
      SettingsScreen(
          model = updatedModel,
          navigationIconClick = { /*no-op*/ },
          changeLanguageButtonClick = { /*no-op*/ },
          updateButtonClick = { /*no-op*/ },
          logoutButtonClick = { /*no-op*/ }
      )
    }

    // then
    composeRule.onNodeWithTag("SETTINGS_APP_UPDATE_BUTTON").assertExists()
  }

  @Test
  fun whenAppUpdateIsNotAvailableThenHideAppUpdateButton() {
    // given
    val updatedModel = defaultSettingsModel.checkedAppUpdate(isUpdateAvailable = false)
    composeRule.setContent {
      SettingsScreen(
          model = updatedModel,
          navigationIconClick = { /*no-op*/ },
          changeLanguageButtonClick = { /*no-op*/ },
          updateButtonClick = { /*no-op*/ },
          logoutButtonClick = { /*no-op*/ }
      )
    }

    // then
    composeRule.onNodeWithTag("SETTINGS_APP_UPDATE_BUTTON").assertDoesNotExist()
  }

  @Test
  fun whenUserIsLoggingOutThenShowProgressIndicator() {
    // given
    val updatedModel = defaultSettingsModel.userLoggingOut()
    composeRule.setContent {
      SettingsScreen(
          model = updatedModel,
          navigationIconClick = { /*no-op*/ },
          changeLanguageButtonClick = { /*no-op*/ },
          updateButtonClick = { /*no-op*/ },
          logoutButtonClick = { /*no-op*/ }
      )
    }

    // then
    composeRule.onNodeWithTag("SETTINGS_LOGGING_OUT_PROGRESS").assertExists()
  }

  @Test
  fun whenUserIsNotLoggingOutThenShowProgressIndicator() {
    // given
    composeRule.setContent {
      SettingsScreen(
          model = defaultSettingsModel,
          navigationIconClick = { /*no-op*/ },
          changeLanguageButtonClick = { /*no-op*/ },
          updateButtonClick = { /*no-op*/ },
          logoutButtonClick = { /*no-op*/ }
      )
    }

    // then
    composeRule.onNodeWithTag("SETTINGS_LOGGING_OUT_PROGRESS").assertDoesNotExist()
  }

  @Test
  fun whenDatabaseIsEncryptedThenShowAppSecureIcon() {
    // given
    val updatedModel = defaultSettingsModel.databaseEncryptionStatusLoaded(isDatabaseEncrypted = true)
    composeRule.setContent {
      SettingsScreen(
          model = updatedModel,
          navigationIconClick = { /*no-op*/ },
          changeLanguageButtonClick = { /*no-op*/ },
          updateButtonClick = { /*no-op*/ },
          logoutButtonClick = { /*no-op*/ }
      )
    }

    // then
    composeRule.onNodeWithTag("SETTINGS_APP_SECURE").assertExists()
  }

  @Test
  fun whenDatabaseIsNotEncryptedThenDoNotShowAppSecureIcon() {
    // given
    val updatedModel = defaultSettingsModel.databaseEncryptionStatusLoaded(isDatabaseEncrypted = false)
    composeRule.setContent {
      SettingsScreen(
          model = updatedModel,
          navigationIconClick = { /*no-op*/ },
          changeLanguageButtonClick = { /*no-op*/ },
          updateButtonClick = { /*no-op*/ },
          logoutButtonClick = { /*no-op*/ }
      )
    }

    // then
    composeRule.onNodeWithTag("SETTINGS_APP_SECURE").assertDoesNotExist()
  }

  @Test
  fun whenChangeLanguageFeatureIsNotEnabledThenDoNotShowChangeLanguageSetting() {
    val model = SettingsModel.default(
        isChangeLanguageFeatureEnabled = false,
        isLogoutUserFeatureEnabled = true
    )
    composeRule.setContent {
      SettingsScreen(
          model = model,
          navigationIconClick = { /*no-op*/ },
          changeLanguageButtonClick = { /*no-op*/ },
          updateButtonClick = { /*no-op*/ },
          logoutButtonClick = { /*no-op*/ }
      )
    }

    // then
    composeRule.onNodeWithTag("SETTINGS_ITEM_CHANGE_LANGUAGE").assertDoesNotExist()
  }

  @Test
  fun whenChangeLanguageFeatureIsEnabledThenShowChangeLanguageSetting() {
    val model = SettingsModel.default(
        isChangeLanguageFeatureEnabled = true,
        isLogoutUserFeatureEnabled = true
    )
    composeRule.setContent {
      SettingsScreen(
          model = model,
          navigationIconClick = { /*no-op*/ },
          changeLanguageButtonClick = { /*no-op*/ },
          updateButtonClick = { /*no-op*/ },
          logoutButtonClick = { /*no-op*/ }
      )
    }

    // then
    composeRule.onNodeWithTag("SETTINGS_ITEM_CHANGE_LANGUAGE").assertExists()
  }

  @Test
  fun whenLogoutFeatureIsNotEnabledThenDoNotShowLogoutButton() {
    val model = SettingsModel.default(
        isChangeLanguageFeatureEnabled = true,
        isLogoutUserFeatureEnabled = false
    )
    composeRule.setContent {
      SettingsScreen(
          model = model,
          navigationIconClick = { /*no-op*/ },
          changeLanguageButtonClick = { /*no-op*/ },
          updateButtonClick = { /*no-op*/ },
          logoutButtonClick = { /*no-op*/ }
      )
    }

    // then
    composeRule.onNodeWithTag("SETTINGS_LOGOUT_BUTTON").assertDoesNotExist()
  }

  @Test
  fun whenLogoutFeatureIsEnabledThenShowLogoutButton() {
    val model = SettingsModel.default(
        isChangeLanguageFeatureEnabled = true,
        isLogoutUserFeatureEnabled = true
    )
    composeRule.setContent {
      SettingsScreen(
          model = model,
          navigationIconClick = { /*no-op*/ },
          changeLanguageButtonClick = { /*no-op*/ },
          updateButtonClick = { /*no-op*/ },
          logoutButtonClick = { /*no-op*/ }
      )
    }

    // then
    composeRule.onNodeWithTag("SETTINGS_LOGOUT_BUTTON").assertExists()
  }
}
