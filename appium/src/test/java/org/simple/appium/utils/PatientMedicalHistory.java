package org.simple.appium.utils;

public enum PatientMedicalHistory {
  DIAGNOSEDWITHHYPERTENSION(true, false), ALREADYTAKESHYPERTENSIONDRUGS(true, false), HEARTATTACKINLAST3YEARS(true, false),
  PASTHISTORYOFSTROKE(true, false), PASTHISTORYOFKIDNEYDISEASE(true, false), HASDIABETES(true, false);
  public boolean yes;
  public boolean no;

  PatientMedicalHistory(boolean yes, boolean no) {
    this.yes = yes;
    this.no = no;
  }

}
