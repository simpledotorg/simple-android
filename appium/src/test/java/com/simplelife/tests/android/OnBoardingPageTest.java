package com.simplelife.tests.android;

import com.simplelife.pages.android.OnboardingPage;
import com.simplelife.tests.base.BaseTest;
import com.simplelife.utils.DataGenrate;
import org.testng.annotations.Test;

public class OnBoardingPageTest extends BaseTest {

    @Test(description = "Login to simple Application")
    public void login() {
        OnboardingPage onboardingPage = new OnboardingPage(appiumDriver);
        onboardingPage.loginToSimpleApplication("7879556515", "7879");
    }

    @Test(description = "signUp into simple Application")
    public void signUp() {
        OnboardingPage onboardingPage = new OnboardingPage(appiumDriver);
        DataGenrate dataGenrate = new DataGenrate();
        String phoneNumber = dataGenrate.setPhoneNumber();
        String name = dataGenrate.setName();
        String pin = "7879";
        onboardingPage.signUpToSimpleApplication(phoneNumber, name, pin);

    }

}