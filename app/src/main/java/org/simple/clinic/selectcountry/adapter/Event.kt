package org.simple.clinic.selectcountry.adapter

import org.simple.clinic.appconfig.Country

sealed class Event {
  data class CountryClicked(val country: Country) : Event()
}
