package org.simple.clinic.util.moshi

import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import org.simple.clinic.monthlyReports.questionnaire.component.BaseComponentData
import org.simple.clinic.monthlyReports.questionnaire.component.HeaderComponentData
import org.simple.clinic.monthlyReports.questionnaire.component.InputFieldComponentData
import org.simple.clinic.monthlyReports.questionnaire.component.InputViewGroupComponentData
import org.simple.clinic.monthlyReports.questionnaire.component.LineSeparatorComponentData
import org.simple.clinic.monthlyReports.questionnaire.component.SeparatorComponentData
import org.simple.clinic.monthlyReports.questionnaire.component.SubHeaderComponentData
import org.simple.clinic.monthlyReports.questionnaire.component.UnknownComponent
import org.simple.clinic.monthlyReports.questionnaire.component.ViewGroupComponentData

class QuestionnaireLayoutJsonAdapter {

  fun getFactory(): PolymorphicJsonAdapterFactory<BaseComponentData> {
    return PolymorphicJsonAdapterFactory.of(BaseComponentData::class.java, "view_type")
        .withSubtype(ViewGroupComponentData::class.java, "view_group")
        .withSubtype(SubHeaderComponentData::class.java, "sub_header")
        .withSubtype(InputViewGroupComponentData::class.java, "input_view_group")
        .withSubtype(SeparatorComponentData::class.java, "separator")
        .withSubtype(HeaderComponentData::class.java, "header")
        .withSubtype(InputFieldComponentData::class.java, "input_field")
        .withSubtype(LineSeparatorComponentData::class.java, "line_separator")
        .withDefaultValue(UnknownComponent())
  }
}
