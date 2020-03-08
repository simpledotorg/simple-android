package org.simple.clinic

import android.app.Application
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.simple.clinic.util.Rules
import java.util.Locale
import javax.inject.Inject


class DateOfBirthHintUnfocusedAndroidTest {

  @get:Rule
  val ruleChain: RuleChain = Rules.global()

  @Inject
  lateinit var application: Application

  @Test
  fun verify_unfocused_hint_on_date_of_birth_edittext_is_longer_than_mask_in_all_locales() {
    TestClinicApp.appComponent().inject(this)

    val stringResId = R.string.patiententry_date_of_birth_unfocused
    val maskLength = 8 // ##/##/####

    val locales = application
        .assets
        .locales
        .map { Locale.forLanguageTag(it) }

    locales.forEach { locale ->
      val stringRes = with(application.resources.configuration) {
        setLocale(locale)
        val contextWithOverridenLocale = application.createConfigurationContext(this)
        contextWithOverridenLocale.getString(stringResId)
      }

      assertWithMessage("Locale [$locale] has string ($stringRes) length less than mask length")
          .that(stringRes.length)
          .isAtLeast(maskLength)
    }
  }
}
