package org.simple.clinic.editpatient

enum class EditPatientValidationError(val analyticsName: String) {
    FULL_NAME_EMPTY("Patient Edit:Name is empty"),
    PHONE_NUMBER_EMPTY("Patient Edit:Phone Number is empty"),
    PHONE_NUMBER_LENGTH_TOO_SHORT("Patient Edit:Phone Number is less than 6 digits"),
    PHONE_NUMBER_LENGTH_TOO_LONG("Patient Edit:Phone Number is more than 12 digits"),
    COLONY_OR_VILLAGE_EMPTY("Patient Edit:Colony or village empty"),
    DISTRICT_EMPTY("Patient Edit:District empty"),
    STATE_EMPTY("Patient Edit:State empty"),
    BOTH_DATEOFBIRTH_AND_AGE_ABSENT("Patient Edit:Both age and DOB are absent"),
    DATE_OF_BIRTH_PARSE_ERROR("Patient Edit:Invalid DOB"),
    DATE_OF_BIRTH_IN_FUTURE("Patient Edit:DOB in future"),
    AGE_EXCEEDS_MAX_LIMIT("Patient Edit: Age greater than 120 years is invalid"),
    DATE_OF_BIRTH_EXCEEDS_MAX_LIMIT("Patient Edit: Age greater than 120 years is invalid"),
    AGE_EXCEEDS_MIN_LIMIT("Patient Edit: Age equal to 0 years is invalid"),
    DATE_OF_BIRTH_EXCEEDS_MIN_LIMIT("Patient Edit: Age equal to 120 years is invalid")
}
