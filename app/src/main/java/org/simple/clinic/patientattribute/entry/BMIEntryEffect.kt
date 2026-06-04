package org.simple.clinic.patientattribute.entry

import org.simple.clinic.patientattribute.BMIReading

sealed class BMIEntryEffect

sealed class BMIEntryViewEffect : BMIEntryEffect()

data object CloseSheet : BMIEntryViewEffect()

data object ChangeFocusToHeight : BMIEntryViewEffect()

data object ChangeFocusToWeight : BMIEntryViewEffect()
