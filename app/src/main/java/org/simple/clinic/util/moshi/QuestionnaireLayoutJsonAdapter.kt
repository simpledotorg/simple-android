package org.simple.clinic.util.moshi

import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import org.simple.clinic.questionnaire.component.BaseComponentData
import org.simple.clinic.questionnaire.component.HeaderComponentData
import org.simple.clinic.questionnaire.component.InputViewGroupComponentData
import org.simple.clinic.questionnaire.component.LineSeparatorComponentData
import org.simple.clinic.questionnaire.component.ParagraphComponentData
import org.simple.clinic.questionnaire.component.RadioViewGroupComponentData
import org.simple.clinic.questionnaire.component.SeparatorComponentData
import org.simple.clinic.questionnaire.component.SubHeaderComponentData
import org.simple.clinic.questionnaire.component.UnknownComponent
import org.simple.clinic.questionnaire.component.UnorderedListViewGroupComponentData
import org.simple.clinic.questionnaire.component.ViewGroupComponentData

class QuestionnaireLayoutJsonAdapter {

  fun getFactory(): PolymorphicJsonAdapterFactory<BaseComponentData> {
    return PolymorphicJsonAdapterFactory.of(BaseComponentData::class.java, "view_type")
        .withSubtype(ViewGroupComponentData::class.java, "view_group")
        .withSubtype(SubHeaderComponentData::class.java, "sub_header")
        .withSubtype(InputViewGroupComponentData::class.java, "input_view_group")
        .withSubtype(SeparatorComponentData::class.java, "separator")
        .withSubtype(HeaderComponentData::class.java, "header")
        .withSubtype(LineSeparatorComponentData::class.java, "line_separator")
        .withSubtype(ParagraphComponentData::class.java, "paragraph")
        .withSubtype(UnorderedListViewGroupComponentData::class.java, "unordered_list_view_group")
        .withSubtype(RadioViewGroupComponentData::class.java, "radio_view_group")
        .withDefaultValue(UnknownComponent())
  }
}
