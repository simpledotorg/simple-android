package org.simple.clinic.patient

/**
 * [Regex] for stripping patient names and search queries of white spaces and punctuation
 *
 * Currently matches the following characters
 * - Any whitespace
 * - Comma, Hyphen, SemiColon, Colon, Underscore, Apostrophe, Period
 * */
private val spacePunctuationRegex = Regex("[\\s;_\\-:,'\\\\.]")

fun convertNameToSearchableForm(string: String) = string.replace(spacePunctuationRegex, "")
