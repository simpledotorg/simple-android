package org.simple.clinic.monthlyReports

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Entity(tableName = "Questionnaire")
@Parcelize
data class Questionnaire(
    @PrimaryKey
    val uuid: UUID,

    val questionnaire_type: QuestionnaireType,

    val layout: String,
) : Parcelable
