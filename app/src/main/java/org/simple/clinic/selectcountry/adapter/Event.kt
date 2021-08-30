package org.simple.clinic.selectcountry.adapter

import org.simple.clinic.appconfig.CountryV2

sealed class Event {
  data class CountryClicked(val country: CountryV2) : Event()
}
