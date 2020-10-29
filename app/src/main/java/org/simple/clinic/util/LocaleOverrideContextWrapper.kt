package org.simple.clinic.util

import android.content.Context
import android.content.ContextWrapper
import java.util.Locale

class LocaleOverrideContextWrapper private constructor(base: Context) : ContextWrapper(base) {

  companion object {
    private fun wrap(base: Context, locale: Locale): Context {
      return overrideLocaleInContext(base, locale)
    }

    private fun overrideLocaleInContext(context: Context, overrideLocale: Locale): Context {
      Locale.setDefault(overrideLocale)

      val overrideContext = with(context.resources.configuration) {
        setLocale(overrideLocale)
        setLayoutDirection(overrideLocale)
        context.createConfigurationContext(this)
      }

      return LocaleOverrideContextWrapper(overrideContext)
    }
  }
}
