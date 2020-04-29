package org.simple.clinic.widgets

/** Base class for Ui events in all screens. */
interface UiEvent {

  val analyticsName: String
    get() = ""
}
