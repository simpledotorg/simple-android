package com.simplelife.tests.builder;

import com.simplelife.AppiumServerUtils;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.remote.AndroidMobileCapabilityType;
import io.appium.java_client.remote.MobileCapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

public class AndroidDriverBuilder {

    public static AppiumDriver driver;

    public static AppiumDriver buildDriver() {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, "samsung");
        capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, "android");
        capabilities.setCapability("automationName", "UiAutomator");
        capabilities.setCapability("appPackage", "org.simple.clinic.sandbox");
        capabilities.setCapability("appActivity", "org.simple.clinic.activity.TheActivity");
        capabilities.setCapability(MobileCapabilityType.NO_RESET, "true");
        capabilities.setCapability(AndroidMobileCapabilityType.AUTO_GRANT_PERMISSIONS, true);
        capabilities.setCapability("autoAcceptAlerts", true);
        driver = new AppiumDriver(AppiumServerUtils.server.getUrl(), capabilities);
        //driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);

        return driver;

  //
    }
}
