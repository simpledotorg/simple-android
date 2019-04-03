package org.simple.clinic.patient.businessid

import androidx.room.ColumnInfo

data class Identifier(

    @ColumnInfo(name = "identifier")
    val value: String,

    @ColumnInfo(name = "identifierType")
    val type: BusinessId.IdentifierType
)
