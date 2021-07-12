package org.simple.clinic.drugs.search

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.simple.clinic.storage.Timestamps
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequency
import java.util.UUID

@Entity
data class Drug(
    @PrimaryKey
    val id: UUID,

    val name: String,

    val category: DrugCategory?,

    val frequency: MedicineFrequency?,

    val composition: String?,

    val dosage: String?,

    val rxNormCode: String?,

    val protocol: Answer,

    val common: Answer,

    @Embedded
    val timestamps: Timestamps
)
