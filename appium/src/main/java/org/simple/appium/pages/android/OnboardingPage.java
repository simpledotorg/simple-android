package org.simple.appium.pages.android;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.CacheLookup;
import org.openqa.selenium.support.FindBy;
import org.simple.appium.pages.base.BasePage;

import io.appium.java_client.AppiumDriver;

public class OnboardingPage extends BasePage {

  @CacheLookup
  @FindBy(id = "onboarding_get_started")
  private WebElement onboardingStarted;

  @CacheLookup
  @FindBy(id = "registrationphone_phone")
  private WebElement registrationPhone;

  @CacheLookup
  @FindBy(id = "registrationname_name")
  private WebElement registrationName;

  @CacheLookup
  @FindBy(id = "registrationpin_pin")
  private WebElement registrationPin;

  @CacheLookup
  @FindBy(id = "registrationconfirmpin_pin")
  private WebElement reConfirmPin;

  @CacheLookup
  @FindBy(id = "pinentry_pin")
  private WebElement password;

  @CacheLookup
  @FindBy(id = "onboarding_logo")
  private WebElement onboardingLogo;

  @CacheLookup
  @FindBy(xpath = "//android.widget.TextView[text='Your phone number']")
  private WebElement phoneCellText;


  public OnboardingPage(AppiumDriver appiumDriver) {
    super(appiumDriver);
  }

  public void getStarted() {

    clickOnElement(onboardingStarted);
  }

  public void setRegistrationPhone(String phoneNumber) {

    sendKeys(registrationPhone, phoneNumber);
    clickOnDoneButtonOnKeypad();
  }

  public void setRegistrationName(String name) {

    sendKeys(registrationName, name);
    clickOnDoneButtonOnKeypad();
  }

  public void setRegistrationPin(String pin) {

    sendKeys(registrationPin, pin);
    clickOnDoneButtonOnKeypad();
  }

  public Homepage setReconfirmPin(String pin) {
    sendKeys(reConfirmPin, pin);
    clickOnDoneButtonOnKeypad();
    return new Homepage(appiumDriver);
  }

  public Homepage setPassword(String password1) {
    sendKeys(password, password1);
    return new Homepage(appiumDriver);
  }

  public Homepage loginToSimpleApplication(String user, String password) {
    getStarted();
    setRegistrationPhone(user);
    setPassword(password);
    return new Homepage(appiumDriver);
  }

  public Homepage signUpToSimpleApplication(String user, String name, String pin) {
    getStarted();
    setRegistrationPhone(user);
    setRegistrationName(name);
    setRegistrationPin(pin);
    setReconfirmPin(pin);
    return new Homepage(appiumDriver);
  }
}
