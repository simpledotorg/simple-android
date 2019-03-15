package com.simplelife.tests.android;

import com.simplelife.pages.android.Homepage;
import com.simplelife.pages.android.OnboardingPage;
import com.simplelife.tests.annotation.Author;
import com.simplelife.tests.annotation.TestCaseNotes;
import com.simplelife.tests.annotation.TesterName;
import com.simplelife.tests.base.BaseTest;
import com.simplelife.utils.DataGenrate;
import com.simplelife.utils.Gendar;
import com.simplelife.utils.PatientMedicalHistory;
import org.testng.Assert;
import org.testng.annotations.Test;

public class HomePageTest extends BaseTest {

    @Author(name = TesterName.AKASH)
    @TestCaseNotes(Steps = "Login to Simple Application || Search Patient|| if patient is not present in our Data base then || click on Register Patient||Fill all the detail||Skip the Medical history||enter the BP||Skip the schedule visit", expecatedResult = "user successfully registered the Patient it should be display in Home Page")
    @Test
    public void addPatient() throws InterruptedException {
        OnboardingPage onboardingPage = new OnboardingPage(appiumDriver);
        DataGenrate data = new DataGenrate();
        String name = data.setName();
        String phoneNumber = data.setPhoneNumber();
        String address = data.setAddress();
        String age = data.setAge();
        Homepage homePage = onboardingPage.loginToSimpleApplication("7879556515", "7879");
        homePage.patientPersonalInformation(name, phoneNumber, age, address, String.valueOf(Gendar.MALE));
        homePage.clickOnNextButtonOnpatientMedicalHistoryInfoPage();
        homePage.setpatientBloodPressure("150", "140");
        homePage.dateOfVisitIsCurrentDate();
        homePage.savePatientDetaial();
        homePage.schaduleVisitNotNow();
    }

    @Test
    public void addPatientwithMedicalHistory() throws InterruptedException {
        OnboardingPage onboardingPage = new OnboardingPage(appiumDriver);
        DataGenrate data = new DataGenrate();
        Homepage homePage = onboardingPage.loginToSimpleApplication("7879556515", "7879");
        String name = data.setName();
        String phoneNumber = data.setPhoneNumber();
        String address = data.setAddress();
        String age = data.setAge();
        homePage.patientPersonalInformation(name, phoneNumber, age, address, "male");
        homePage.patientHaveAlreadyTakesHyperTensionDrugs(PatientMedicalHistory.ALREADYTAKESHYPERTENSIONDRUGS.no);
        homePage.patientHavediagnosedwithhypertension(PatientMedicalHistory.DIAGNOSEDWITHHYPERTENSION.yes);
        homePage.patientHaveHasdiabetes(PatientMedicalHistory.HASDIABETES.no);
        homePage.patientHaveHeartAttackinLast3Years(PatientMedicalHistory.HEARTATTACKINLAST3YEARS.yes);
        homePage.patientHavePastHistoryofStroke(PatientMedicalHistory.PASTHISTORYOFSTROKE.no);
        homePage.patientHavePastHistoryofkidneydisease(PatientMedicalHistory.PASTHISTORYOFKIDNEYDISEASE.yes);
        homePage.clickOnNextButtonOnpatientMedicalHistoryInfoPage();
        homePage.setpatientBloodPressure("150", "140");
        homePage.dateOfVisitIsCurrentDate();
        homePage.savePatientDetaial();
        homePage.schaduleVisitNotNow();
    }

    @Test
    public void addPatientwithMedicalHistoryAndchangeDateOfVisit() throws InterruptedException {
        OnboardingPage onboardingPage = new OnboardingPage(appiumDriver);
        DataGenrate data = new DataGenrate();
        Homepage homePage = onboardingPage.loginToSimpleApplication("7879556515", "7879");
        String name = data.setName();
        String phoneNumber = data.setPhoneNumber();
        String address = data.setAddress();
        String age = data.setAge();
        homePage.patientPersonalInformation(name, phoneNumber, age, address, "male");
        homePage.patientHaveAlreadyTakesHyperTensionDrugs(PatientMedicalHistory.ALREADYTAKESHYPERTENSIONDRUGS.no);
        homePage.patientHavediagnosedwithhypertension(PatientMedicalHistory.DIAGNOSEDWITHHYPERTENSION.yes);
        homePage.patientHaveHasdiabetes(PatientMedicalHistory.HASDIABETES.no);
        homePage.patientHaveHeartAttackinLast3Years(PatientMedicalHistory.HEARTATTACKINLAST3YEARS.yes);
        homePage.patientHavePastHistoryofStroke(PatientMedicalHistory.PASTHISTORYOFSTROKE.no);
        homePage.patientHavePastHistoryofkidneydisease(PatientMedicalHistory.PASTHISTORYOFKIDNEYDISEASE.yes);
        homePage.clickOnNextButtonOnpatientMedicalHistoryInfoPage();
        homePage.setpatientBloodPressure("150", "140");
        homePage.changeDateOfVisit("12", "04", "16");
        homePage.savePatientDetaial();
        homePage.schaduleVisitNotNow();
    }

    @Test
    public void addPatientwithMedicalHistoryAndchangeDayOfVisit() throws InterruptedException {
        OnboardingPage onboardingPage = new OnboardingPage(appiumDriver);
        DataGenrate data = new DataGenrate();
        Homepage homePage = onboardingPage.loginToSimpleApplication("7879556515", "7879");
        String name = data.setName();
        String phoneNumber = data.setPhoneNumber();
        String address = data.setAddress();
        String age = data.setAge();
        homePage.patientPersonalInformation(name, phoneNumber, age, address, "male");
        homePage.patientHaveAlreadyTakesHyperTensionDrugs(PatientMedicalHistory.ALREADYTAKESHYPERTENSIONDRUGS.no);
        homePage.patientHavediagnosedwithhypertension(PatientMedicalHistory.DIAGNOSEDWITHHYPERTENSION.yes);
        homePage.patientHaveHasdiabetes(PatientMedicalHistory.HASDIABETES.no);
        homePage.patientHaveHeartAttackinLast3Years(PatientMedicalHistory.HEARTATTACKINLAST3YEARS.yes);
        homePage.patientHavePastHistoryofStroke(PatientMedicalHistory.PASTHISTORYOFSTROKE.no);
        homePage.patientHavePastHistoryofkidneydisease(PatientMedicalHistory.PASTHISTORYOFKIDNEYDISEASE.yes);
        homePage.clickOnNextButtonOnpatientMedicalHistoryInfoPage();
        homePage.setpatientBloodPressure("150", "140");
        homePage.changeDayofVisit("12");
        homePage.savePatientDetaial();
        homePage.schaduleVisitNotNow();
    }

    @Test
    public void addPatientwithMedicalHistoryAndchangeMonthOfVisit() throws InterruptedException {
        OnboardingPage onboardingPage = new OnboardingPage(appiumDriver);
        DataGenrate data = new DataGenrate();
        Homepage homePage = onboardingPage.loginToSimpleApplication("7879556515", "7879");
        String name = data.setName();
        String phoneNumber = data.setPhoneNumber();
        String address = data.setAddress();
        String age = data.setAge();
        homePage.patientPersonalInformation(name, phoneNumber, age, address, "male");
        homePage.patientHaveAlreadyTakesHyperTensionDrugs(PatientMedicalHistory.ALREADYTAKESHYPERTENSIONDRUGS.no);
        homePage.patientHavediagnosedwithhypertension(PatientMedicalHistory.DIAGNOSEDWITHHYPERTENSION.yes);
        homePage.patientHaveHasdiabetes(PatientMedicalHistory.HASDIABETES.no);
        homePage.patientHaveHeartAttackinLast3Years(PatientMedicalHistory.HEARTATTACKINLAST3YEARS.yes);
        homePage.patientHavePastHistoryofStroke(PatientMedicalHistory.PASTHISTORYOFSTROKE.no);
        homePage.patientHavePastHistoryofkidneydisease(PatientMedicalHistory.PASTHISTORYOFKIDNEYDISEASE.yes);
        homePage.clickOnNextButtonOnpatientMedicalHistoryInfoPage();
        homePage.setpatientBloodPressure("150", "140");
        homePage.changeMonthOfVisit("12");
        homePage.savePatientDetaial();
        homePage.schaduleVisitNotNow();
    }

    @Test
    public void addPatientwithMedicalHistoryAndchangeYearOfVisit() throws InterruptedException {
        OnboardingPage onboardingPage = new OnboardingPage(appiumDriver);
        DataGenrate data = new DataGenrate();
        Homepage homePage = onboardingPage.loginToSimpleApplication("7879556515", "7879");
        String name = data.setName();
        String phoneNumber = data.setPhoneNumber();
        String address = data.setAddress();
        String age = data.setAge();
        homePage.patientPersonalInformation(name, phoneNumber, age, address, "male");
        homePage.patientHaveAlreadyTakesHyperTensionDrugs(PatientMedicalHistory.ALREADYTAKESHYPERTENSIONDRUGS.no);
        homePage.patientHavediagnosedwithhypertension(PatientMedicalHistory.DIAGNOSEDWITHHYPERTENSION.yes);
        homePage.patientHaveHasdiabetes(PatientMedicalHistory.HASDIABETES.no);
        homePage.patientHaveHeartAttackinLast3Years(PatientMedicalHistory.HEARTATTACKINLAST3YEARS.yes);
        homePage.patientHavePastHistoryofStroke(PatientMedicalHistory.PASTHISTORYOFSTROKE.no);
        homePage.patientHavePastHistoryofkidneydisease(PatientMedicalHistory.PASTHISTORYOFKIDNEYDISEASE.yes);
        homePage.clickOnNextButtonOnpatientMedicalHistoryInfoPage();
        homePage.setpatientBloodPressure("150", "140");
        homePage.changeYearOfVisit("12");
        homePage.savePatientDetaial();
        homePage.schaduleVisitNotNow();
    }

    @Test
    public void addPatientWithoutBloodPresser() throws InterruptedException {
        OnboardingPage onboardingPage = new OnboardingPage(appiumDriver);
        DataGenrate data = new DataGenrate();
        Homepage homePage = onboardingPage.loginToSimpleApplication("7879556515", "7879");
        String name = data.setName();
        String phoneNumber = data.setPhoneNumber();
        String address = data.setAddress();
        String age = data.setAge();
        homePage.patientPersonalInformation(name, phoneNumber, age, address, "male");
        homePage.clickOnNextButtonOnpatientMedicalHistoryInfoPage();
        homePage.setpatientBloodPressure("150", "140");
        homePage.dateOfVisitIsCurrentDate();
        homePage.deletePatientBloodPresser();
        homePage.savePatientDetaial();
        Assert.assertFalse(homePage.isrecentPatientLastBpDisplay(), "after deleting the patient Bp then also patient Last BP is display in Home Page");
    }


    @Test
    public void updateThePatientBloodPressure() throws InterruptedException {
        Homepage homePage = new Homepage(appiumDriver);
        homePage.clickOnPatientOnHomePage();
        boolean isDisplay = homePage.updateThePatientBloodPressure("160", "150");
        Assert.assertTrue(isDisplay, "No blood pressures added && blood pressure edit button is not display");
        homePage.savePatientDetaial();
        Assert.assertTrue(homePage.isrecentPatientLastBpDisplay());

    }

    @Test
    public void addNewBloodPressureOfPatient() throws InterruptedException {

        Homepage homePage = new Homepage(appiumDriver);
        homePage.addNewBloodPressureOfPatient("160", "150");
    }

}
