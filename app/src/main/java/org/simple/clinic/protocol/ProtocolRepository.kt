package org.simple.clinic.protocol

import io.reactivex.Observable
import org.simple.clinic.di.AppScope
import java.util.UUID
import javax.inject.Inject

@AppScope
@Deprecated(message = "Use v2")
class ProtocolRepository @Inject constructor() {

  fun currentProtocol(): Observable<Protocol> {
    val protocolUuid = UUID.fromString("d486b15b-ac2a-4d2f-b87c-e1905d5ee793")
    return Observable.just(Protocol(
        protocolUuid,
        name = "Dummy protocol",
        drugs = listOf(
            ProtocolDrug(
                name = "Amlodipine",
                rxNormCode = "rxnormcode-1",
                dosages = listOf("5mg", "10mg")),
            ProtocolDrug(
                name = "Telmisartan",
                rxNormCode = "rxnormcode-2",
                dosages = listOf("40mg", "80mg")),
            ProtocolDrug(
                name = "Chlorthalidone",
                rxNormCode = "rxnormcode-3",
                dosages = listOf("12.5mg", "25mg")),
            ProtocolDrug(
                name = "Losartan",
                rxNormCode = "rxnormcode-4",
                dosages = listOf("50mg", "100mg")),
            ProtocolDrug(
                name = "Atenolol",
                rxNormCode = "rxnormcode-5",
                dosages = listOf("25mg", "50mg")),
            ProtocolDrug(
                name = "Hydrochlorothiazide",
                rxNormCode = "rxnormcode-6",
                dosages = listOf("12.5mg", "25mg")),
            ProtocolDrug(
                name = "Aspirin",
                rxNormCode = "rxnormcode-7",
                dosages = listOf("75mg", "81mg")),
            ProtocolDrug(
                name = "Enalapril",
                rxNormCode = "rxnormcode-8",
                dosages = listOf("20mg", "40mg"))
        )
    ))
  }
}
