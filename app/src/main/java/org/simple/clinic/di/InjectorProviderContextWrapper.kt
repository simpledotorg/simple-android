package org.simple.clinic.di

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper

const val INJECTOR_SERVICE_KEY = "org.simple.clinic.injector_provider"

class InjectorProviderContextWrapper(
    base: Context,
    private val injectors: List<Any>
) : ContextWrapper(base) {

  companion object {
    fun wrap(
        base: Context,
        vararg injectors: Any
    ): Context = InjectorProviderContextWrapper(base, injectors.toList())
  }

  fun find(clazz: Class<*>): Any {
    val injector = injectors.find { clazz.isAssignableFrom(it.javaClass) }

    requireNotNull(injector) {
      "Could not find an appropriate injector for ${clazz.name}! Did you forget to implement it on the component or to provide it to the context wrapper?"
    }

    return injector
  }

  override fun getSystemService(name: String): Any? {
    return if (name == INJECTOR_SERVICE_KEY) this else super.getSystemService(name)
  }
}

@SuppressLint("WrongConstant")
@Suppress("UNCHECKED_CAST")
inline fun <reified T> Context.injector(): T {
  val injectorProvider = getSystemService(INJECTOR_SERVICE_KEY) as InjectorProviderContextWrapper

  return injectorProvider.find(T::class.java) as T
}
