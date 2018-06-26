package org.simple.clinic.protocol

import io.reactivex.Observable
import org.simple.clinic.di.AppScope
import java.util.UUID
import javax.inject.Inject

@AppScope
class ProtocolRepository @Inject constructor() {

  fun currentProtocol(): Observable<Protocol> {
    val protocolUuid = UUID.fromString("d486b15b-ac2a-4d2f-b87c-e1905d5ee793")
    return Observable.just(Protocol(
        protocolUuid,
        name = "Dummy protocol",
        drugs = listOf(
            ProtocolDrug(
                UUID.fromString("feab6950-86fe-4b70-95c9-f21620140068"),
                name = "Amlodipine",
                rxNormCode = "rxnormcode-1",
                dosages = listOf("5mg", "10mg"),
                protocolUUID = protocolUuid),
            ProtocolDrug(
                UUID.fromString("f951b82d-2198-4fed-a55c-e2be67894009"),
                name = "Telmisartan",
                rxNormCode = "rxnormcode-2",
                dosages = listOf("40mg", "80mg"),
                protocolUUID = protocolUuid),
            ProtocolDrug(
                UUID.fromString("43f7ea6d-4002-4aa8-b440-c5b4798fb78c"),
                name = "Chlorthalidone",
                rxNormCode = "rxnormcode-3",
                dosages = listOf("12.5mg", "25mg"),
                protocolUUID = protocolUuid)
        )
    ))
  }
}
