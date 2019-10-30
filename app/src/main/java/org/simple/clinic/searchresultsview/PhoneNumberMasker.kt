package org.simple.clinic.searchresultsview

interface PhoneNumberMasker {
  fun mask(number: String): String
}
