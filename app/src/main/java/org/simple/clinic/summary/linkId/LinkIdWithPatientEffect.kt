package org.simple.clinic.summary.linkId

import org.simple.clinic.patient.businessid.Identifier

sealed class LinkIdWithPatientEffect

data class RenderIdentifierText(val identifier: Identifier) : LinkIdWithPatientEffect()

object CloseSheetWithOutIdLinked : LinkIdWithPatientEffect()

object CloseSheetWithLinkedId : LinkIdWithPatientEffect()

object LoadCurrentUser : LinkIdWithPatientEffect()
