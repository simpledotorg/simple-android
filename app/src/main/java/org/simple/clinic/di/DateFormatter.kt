package org.simple.clinic.di

import javax.inject.Qualifier

@Qualifier
annotation class DateFormatter(val value: Type) {

  enum class Type {
    FileDateTime,
    Day,
    Month,
    FullYear
  }
}
