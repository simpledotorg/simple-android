package org.simple.clinic.patient

import android.arch.persistence.room.Embedded

data class PatientQueryModel(

    @Embedded(prefix = "patient_")
    val patient: Patient,

    @Embedded(prefix = "addr_")
    val address: PatientAddress,

    @Embedded(prefix = "phone_")
    val phoneNumber: PatientPhoneNumber?
)
