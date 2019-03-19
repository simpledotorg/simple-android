package org.simple.appium.pages.android;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.CacheLookup;
import org.openqa.selenium.support.FindBy;
import org.simple.appium.pages.base.BasePage;

import java.util.List;

import io.appium.java_client.AppiumDriver;

public class Homepage extends BasePage {

  @CacheLookup
  @FindBy(className = "android.widget.RadioButton")
  List<WebElement> patientGender;

  @CacheLookup
  @FindBy(id = "patients_search_patients")
  private WebElement patientsSearch;

  @CacheLookup
  @FindBy(id = "patientsearch_fullname")
  private WebElement patientFullname;

  @CacheLookup
  @FindBy(id = "patientsearch_search")
  private WebElement nextBtn;

  @CacheLookup
  @FindBy(id = "patientsearchresults_new_patient_rationale")
  private WebElement patientNotPresent;

  @CacheLookup
  @FindBy(id = "patientsearchresults_new_patient")
  private WebElement addNewPatient;

  @CacheLookup
  @FindBy(id = "patiententry_phone_number")
  private WebElement patientPhoneNumber;

  @CacheLookup
  @FindBy(id = "patiententry_age")
  private WebElement patientAge;

  @CacheLookup
  @FindBy(id = "patiententry_colony_or_village")
  private WebElement patientAddress;

  @CacheLookup
  @FindBy(xpath = "//android.widget.Button[@text='NEXT']")
  private WebElement complateRagistrationDetail;

  @CacheLookup
  @FindBy(id = "newmedicalhistory_item_label")
  private List<WebElement> patientMedicalHistory;

  @CacheLookup
  @FindBy(id = "newmedicalhistory_item_yes")
  private List<WebElement> patientHaveDiseases;

  @CacheLookup
  @FindBy(id = "newmedicalhistory_item_no")
  private List<WebElement> patientIsNotHaveDiseases;

  @CacheLookup
  @FindBy(id = "newmedicalhistory_save")
  private WebElement patientMedicalHistoryNextBtn;

  @FindBy(id = "bloodpressureentry_systolic")
  private WebElement bloodPressureSystolic;

  @CacheLookup
  @FindBy(id = "bloodpressureentry_diastolic")
  private WebElement bloodPressureDiastolic;

  @CacheLookup
  @FindBy(id = "bloodpressureentry_next_arrow")
  private WebElement bloodPressureNextArrow;

  @CacheLookup
  @FindBy(id = "bloodpressureentry_enter_date")
  private WebElement bloodpressureEnterDate;

  @CacheLookup
  @FindBy(id = "bloodpressureentry_day")
  private WebElement bloodpressureEnterDay;

  @CacheLookup
  @FindBy(id = "bloodpressureentry_month")
  private WebElement bloodpressureEnterMonth;

  @CacheLookup
  @FindBy(id = "bloodpressureentry_year")
  private WebElement bloodpressureEnterYear;

  @CacheLookup
  @FindBy(xpath = "//android.widget.Button[@text='SAVE']")
  private WebElement complateRagistration;

  @CacheLookup
  @FindBy(xpath = "//android.widget.TextView[@text='Schedule next visit in']")
  private WebElement schaduleVisitTab;

  @CacheLookup
  @FindBy(id = "scheduleappointment_done")
  private WebElement sceduleAppointmentDoneBtn;

  @CacheLookup
  @FindBy(id = "scheduleappointment_not_now")
  private WebElement sceduleAppointmentNotNowBtn;

  @CacheLookup
  @FindBy(id = "patientsummary_item_bp_days_ago")
  private WebElement changePatientBloodPressure;

  @CacheLookup
  @FindBy(id = "bloodpressureentry_remove")
  private WebElement removePatientBloodPressure;

  @CacheLookup
  @FindBy(id = "android:id/button1")
  private WebElement removePatientBloodPressureConfirmPopUp;

  @FindBy(id = "recentpatient_item_last_bp")
  private WebElement recentPatientLastBp;

  @FindBy(id = "recentpatient_item_title")
  private List<WebElement> patientListOnHomePage;

  @FindBy(id = "patientsummary_item_bp_placeholde")
  private WebElement patientBloodPressureSummary;

  @FindBy(id = "patientsummary_item_newbp")
  private WebElement patientAddNewBloodPressureButton;

  @CacheLookup
  @FindBy(id = "scheduleappointment_increment_date")
  private WebElement scheduleappointmentIncrementDate;

  @CacheLookup
  @FindBy(id = "scheduleappointment_decrement_date")
  private WebElement scheduleappointmentDecrementDate;

  @CacheLookup
  @FindBy(id = "patients_enter_code")
  private WebElement otpVerificationAfterLogin;

  @CacheLookup
  @FindBy(id = "enterotp_otp")
  private WebElement enterOtp;

  @CacheLookup
  @FindBy(id = "button1")
  private WebElement otpGotUpPopUp;

  public void setComplateRagistrationDetail(WebElement complateRagistrationDetail) {

  }

  public Homepage(AppiumDriver appiumDriver) {
    super(appiumDriver);
  }

  public void clickOnsearchPatientBtn() {
    clickOnElement(patientsSearch);
  }

  public void searchPatient(String patientName) {
    sendKeys(patientFullname, patientName);
  }

  public void addNewPatient(String patientName) {
    sendKeys(patientFullname, patientName);
  }

  public void clickOnNextBtn() {
    clickOnElement(nextBtn);
  }

  public void patientPersonalInformation(String patientName, String phone, String age, String address, String gendar) {
    clickOnsearchPatientBtn();
    addNewPatient(patientName);
    clickOnNextBtn();
    clickOnElement(addNewPatient);
    sendKeys(patientPhoneNumber, phone);
    sendKeys(patientAge, age);
    for (WebElement genderElement : patientGender) {
      if (genderElement.getText().trim().equalsIgnoreCase(gendar.toLowerCase())) {
        genderElement.click();
      }
    }
    sendKeys(patientAddress, address);
    clickOnElement(complateRagistrationDetail);
  }

  public boolean isPatientPresentInDataBase() {
    return waitForElement(patientNotPresent);
  }

  public void clickOnNextButtonOnpatientMedicalHistoryInfoPage() {
    clickOnElement(patientMedicalHistoryNextBtn);
  }

  public void patientHavediagnosedwithhypertension(boolean diagnosedwithhypertension) {
    if (diagnosedwithhypertension && patientMedicalHistory.get(0).getText().replaceAll(" ", "").equalsIgnoreCase("diagnosedwithhypertension")) {
      clickOnElement(patientHaveDiseases.get(0));

    } else {
      clickOnElement(patientIsNotHaveDiseases.get(0));

    }
  }

  public void patientHaveAlreadyTakesHyperTensionDrugs(boolean alreadytakeshypertensiondrugs) {
    if (alreadytakeshypertensiondrugs && patientMedicalHistory.get(1).getText().replaceAll(" ", "").equalsIgnoreCase("alreadytakeshypertensiondrugs")) {
      clickOnElement(patientHaveDiseases.get(1));

    } else {
      clickOnElement(patientIsNotHaveDiseases.get(1));
    }
  }

  public void patientHaveHeartAttackinLast3Years(boolean heartattackinlast3years) {
    if (heartattackinlast3years && patientMedicalHistory.get(2).getText().replaceAll(" ", "").equalsIgnoreCase("heartattackinlast3years")) {
      clickOnElement(patientHaveDiseases.get(2));

    } else {
      clickOnElement(patientIsNotHaveDiseases.get(2));

    }
  }

  public void patientHavePastHistoryofStroke(boolean pasthistoryofstroke) {
    if (pasthistoryofstroke && patientMedicalHistory.get(3).getText().replaceAll(" ", "").equalsIgnoreCase("pasthistoryofstroke")) {
      clickOnElement(patientHaveDiseases.get(3));

    } else {
      clickOnElement(patientIsNotHaveDiseases.get(3));

    }
  }

  public void patientHavePastHistoryofkidneydisease(boolean pasthistoryofkidneydisease) {
    if (pasthistoryofkidneydisease && patientMedicalHistory.get(4).getText().replaceAll(" ", "").equalsIgnoreCase("pasthistoryofkidneydisease")) {
      clickOnElement(patientHaveDiseases.get(4));

    } else {
      clickOnElement(patientIsNotHaveDiseases.get(4));

    }
  }

  public void patientHaveHasdiabetes(boolean hasdiabetes) {
    if (hasdiabetes && patientMedicalHistory.get(5).getText().replaceAll(" ", "").equalsIgnoreCase("hasdiabetes")) {
      clickOnElement(patientHaveDiseases.get(5));

    } else {
      clickOnElement(patientIsNotHaveDiseases.get(5));

    }
  }

  public void setpatientBloodPressure(String systolic, String diastolic) {
    String bolloPresser = systolic.concat(diastolic);
    bloodPressureDiastolic.sendKeys(bolloPresser);
    clickOnElement(bloodPressureNextArrow);

  }

  public void changeDateOfVisit(String day, String month, String year) {
    boolean dateIsPresent = waitForElement(bloodpressureEnterDate);
    if (dateIsPresent) {
      sendKeys(bloodpressureEnterDay, day);
      sendKeys(bloodpressureEnterMonth, month);
      sendKeys(bloodpressureEnterYear, year);
      clickOnDoneButtonOnKeypad();
    } else {
      System.out.println("date is not display");
    }
  }

  public void dateOfVisitIsCurrentDate() {
    clickOnDoneButtonOnKeypad();
  }

  public void changeDayofVisit(String day) {
    boolean dataTabIsPresent = waitForElement(bloodpressureEnterDate);
    if (dataTabIsPresent) {
      sendKeys(bloodpressureEnterDay, day);
      clickOnDoneButtonOnKeypad();
    } else {
      System.out.println("date is not display");
    }
  }

  public void changeMonthOfVisit(String month) {
    boolean dataTabIsPresent = waitForElement(bloodpressureEnterDate);
    if (dataTabIsPresent) {
      sendKeys(bloodpressureEnterMonth, month);
      clickOnDoneButtonOnKeypad();
    } else {
      System.out.println("data is not display");
    }
  }

  public void changeYearOfVisit(String year) {
    boolean dataTabIsPresent = waitForElement(bloodpressureEnterDate);
    if (dataTabIsPresent) {
      sendKeys(bloodpressureEnterYear, year);
      clickOnDoneButtonOnKeypad();
    } else {
      System.out.println("data is not display");
    }
  }

  public void savePatientDetaial() throws InterruptedException {
    clickOnElement(complateRagistration);
    Thread.sleep(2000);
  }

  public void schaduleVisitDoneBtn() {
    boolean isDisplay = waitForElement(schaduleVisitTab);
    if (isDisplay) {
      clickOnElement(sceduleAppointmentDoneBtn);
    }
  }

  public void schaduleVisitNotNow() {
    boolean isDisplay = waitForElement(schaduleVisitTab);
    if (isDisplay) {
      clickOnElement(sceduleAppointmentNotNowBtn);
    } else {
      System.out.println("User Unable to click on schadule Visit Not Now Button");
    }
  }

  public void deletePatientBloodPresser() {
    clickOnElement(changePatientBloodPressure);
    clickOnElement(removePatientBloodPressure);
    clickOnElement(removePatientBloodPressureConfirmPopUp);
  }

  public boolean updateThePatientBloodPressure(String systolic, String diastolic) {
    boolean is = isExists(recentPatientLastBp);
    if (is) {
      clickOnElement(changePatientBloodPressure);
      setpatientBloodPressure(systolic, diastolic);
      return true;
    } else {
      return false;
    }
  }

  public boolean isRecentPatientLastBpDisplay() {
    return waitForElement(recentPatientLastBp);
  }

  public void clickOnPatientOnHomePage() {
    clickOnElement(patientListOnHomePage.get(0));
  }

  public void addNewBloodPressureOfPatient(String systolic, String diastolic) throws InterruptedException {
    clickOnPatientOnHomePage();
    if (isExists(patientAddNewBloodPressureButton)) {
      patientAddNewBloodPressureButton.click();
    } else {
      scrollDownTo(By.id("patientsummary_item_newbp"));
      patientAddNewBloodPressureButton.click();
    }
    setpatientBloodPressure(systolic, diastolic);
    dateOfVisitIsCurrentDate();
    savePatientDetaial();
  }

  public void scheduleAppointmentAtDecrementDate() {
    clickOnElement(scheduleappointmentDecrementDate);
    schaduleVisitDoneBtn();
  }

  public void scheduleAppointmentAfterTwoMonth() {
    clickOnElement(scheduleappointmentIncrementDate);
    schaduleVisitDoneBtn();
  }

  public void otpVerification() {
    clickOnElement(otpVerificationAfterLogin);
    sendKeys(enterOtp,"000000");
    clickOnElement(otpGotUpPopUp);
  }
}
