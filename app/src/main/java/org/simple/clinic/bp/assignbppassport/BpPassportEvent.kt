package org.simple.clinic.bp.assignbppassport

sealed class BpPassportEvent

object NewOngoingPatientEntrySaved : BpPassportEvent()
