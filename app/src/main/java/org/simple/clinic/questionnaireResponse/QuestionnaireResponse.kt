package org.simple.clinic.questionnaireResponse

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import org.simple.clinic.monthlyReports.questionnaire.QuestionnaireType
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.storage.Timestamps
import java.util.UUID

@Entity(tableName = "QuestionnaireResponse")
@Parcelize
data class QuestionnaireResponse(
    @PrimaryKey
    val uuid: UUID,

    val questionnaireId: UUID,

    val questionnaireType: QuestionnaireType,

    val facilityId: UUID,

    val lastUpdatedByUserId: UUID,

    val content: Map<String, String>,

    @Embedded
    val timestamps: Timestamps,

    val syncStatus: SyncStatus
) : Parcelable
