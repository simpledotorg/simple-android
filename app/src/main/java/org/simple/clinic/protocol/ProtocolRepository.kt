package org.simple.clinic.protocol

import io.reactivex.Observable
import java.util.UUID
import javax.inject.Inject

class ProtocolRepository @Inject constructor() {

  fun currentProtocol(): Observable<Protocol> {
    val protocolUuid = UUID.fromString("3a4aadc1-3322-40be-95b4-a960e6cff9e3")
    return Observable.just(Protocol(
        protocolUuid,
        name = "Dummy protocol",
        drugs = listOf(
            ProtocolDrug(
                UUID.fromString("80976570-0747-4555-9f71-ccba053aab27"),
                name = "Amlodipine",
                rxNormCode = "rxnormcode-1",
                dosage = "5mg",
                protocolUUID = protocolUuid),
            ProtocolDrug(
                UUID.fromString("045b3a7e-ccaa-49ac-b274-e1c6845d621c"),
                name = "Amlodipine",
                rxNormCode = "rxnormcode-1",
                dosage = "10mg",
                protocolUUID = protocolUuid),
            ProtocolDrug(
                UUID.fromString("8a0d86b2-9cfa-49cf-8152-956d6ab0545e"),
                name = "Telmisartan",
                rxNormCode = "rxnormcode-2",
                dosage = "40mg",
                protocolUUID = protocolUuid),
            ProtocolDrug(
                UUID.fromString("25c12696-2994-440b-875f-be85b5bf39f0"),
                name = "Telmisartan",
                rxNormCode = "rxnormcode-2",
                dosage = "5mg",
                protocolUUID = protocolUuid),
            ProtocolDrug(
                UUID.fromString("ff32ca01-ba99-40b7-bc48-18b2aa87486d"),
                name = "Chlorthalidone",
                rxNormCode = "rxnormcode-3",
                dosage = "5mg",
                protocolUUID = protocolUuid),
            ProtocolDrug(
                UUID.fromString("cc891917-ae67-4d80-91f1-fcc1ac5a54f6"),
                name = "Chlorthalidone",
                rxNormCode = "rxnormcode-3",
                dosage = "5mg",
                protocolUUID = protocolUuid)
        )
    ))
  }
}
