package org.simple.clinic.settings

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import org.junit.Test

class SettingsUiRendererTest {

  private val ui = mock<SettingsUi>()
  private val renderer = SettingsUiRenderer(ui)
  private val defaultModel = SettingsModel.FETCHING_USER_DETAILS

  @Test
  fun `when the user details are being fetched, do nothing`() {
    // when
    renderer.render(defaultModel)

    // then
    verifyZeroInteractions(ui)
  }

  @Test
  fun `when the user details are fetched, render them on the UI`() {
    // given
    val name = "Anish Acharya"
    val phoneNumber = "1234567890"
    val modelWithUserDetails = defaultModel.userDetailsFetched(name, phoneNumber)

    // when
    renderer.render(modelWithUserDetails)

    // then
    verify(ui).displayUserDetails(name, phoneNumber)
    verifyNoMoreInteractions(ui)
  }
}
