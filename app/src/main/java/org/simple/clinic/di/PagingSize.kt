package org.simple.clinic.di

import javax.inject.Qualifier

@Qualifier
annotation class PagingSize(val value: Page) {

  enum class Page {
    AllRecentPatients,
    DrugsSearchResults
  }
}

