package org.simple.clinic.main

import javax.inject.Qualifier

@Qualifier
annotation class TypedMap(val type: Type) {

  enum class Type {
    UpdatePriorities
  }
}
