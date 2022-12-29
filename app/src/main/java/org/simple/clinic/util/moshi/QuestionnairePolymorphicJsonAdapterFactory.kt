package org.simple.clinic.util.moshi

import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import org.simple.clinic.monthlyReports.questionnaire.component.BaseComponent
import org.simple.clinic.monthlyReports.questionnaire.component.UnknownComponent
import org.simple.clinic.monthlyReports.questionnaire.component.ViewGroup

class QuestionnairePolymorphicJsonAdapterFactoryProvider() {
  fun getFactory(): PolymorphicJsonAdapterFactory<BaseComponent> {
    return PolymorphicJsonAdapterFactory.of(BaseComponent::class.java, "type")
        .withSubtype(ViewGroup::class.java, "group")
        .withDefaultValue(UnknownComponent())
  }
}
