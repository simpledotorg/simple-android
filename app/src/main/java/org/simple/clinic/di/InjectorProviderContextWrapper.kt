package org.simple.clinic.di

import android.content.Context
import android.content.ContextWrapper

class InjectorProviderContextWrapper(
    base: Context,
    private val injectors: Map<String, Any>
) : ContextWrapper(base) {

  companion object {
    fun wrap(
        base: Context,
        injectors: Map<String, Any>
    ): Context = InjectorProviderContextWrapper(base, injectors)
  }

  override fun getSystemService(name: String): Any? {
    return if (name !in injectors) super.getSystemService(name) else injectors.getValue(name)
  }
}
