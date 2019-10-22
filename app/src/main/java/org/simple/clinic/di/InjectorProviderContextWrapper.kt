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
        injectors: Map<Class<*>, Any>
    ): Context = InjectorProviderContextWrapper(base, injectors.mapKeys { it.key.name })
  }

  override fun getSystemService(name: String): Any? {
    return if (name in injectors) injectors.getValue(name) else super.getSystemService(name)
  }
}


@Suppress("UNCHECKED_CAST")
inline fun <reified T> Context.injector() = getSystemService(T::class.java.name) as T
