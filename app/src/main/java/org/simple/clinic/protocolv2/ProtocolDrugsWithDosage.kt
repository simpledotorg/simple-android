package org.simple.clinic.protocolv2

data class ProtocolDrugsWithDosage(val protocol: Protocol, val drugs: List<ProtocolDrug>)
