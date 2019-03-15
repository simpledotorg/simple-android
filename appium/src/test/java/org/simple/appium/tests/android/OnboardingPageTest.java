package org.simple.appium.tests.android;

import org.simple.appium.pages.android.OnboardingPage;
import org.simple.appium.tests.base.BaseTest;
import org.simple.appium.utils.DataGenerate;
import org.testng.annotations.Test;

public class OnboardingPageTest extends BaseTest {

  @Test(description = "Login to simple Application")
  public void login() {
    OnboardingPage onboardingPage = new OnboardingPage(appiumDriver);
    onboardingPage.loginToSimpleApplication("7879556515", "7879");
  }

  @Test(description = "signUp into simple Application")
  public void signUp() {
    OnboardingPage onboardingPage = new OnboardingPage(appiumDriver);
    DataGenerate dataGenerate = new DataGenerate();
    String phoneNumber = dataGenerate.setPhoneNumber();
    String name = dataGenerate.setName();
    String pin = "7879";
    onboardingPage.signUpToSimpleApplication(phoneNumber, name, pin);

  }

}
