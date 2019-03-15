package org.simple.appium.tests.builder;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.simple.appium.AppiumServerUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.remote.AndroidMobileCapabilityType;
import io.appium.java_client.remote.MobileCapabilityType;

public class AndroidDriverBuilder {

  public static AppiumDriver driver;

  public static AppiumDriver buildDriver() throws FileNotFoundException {

    String appPath = FileUtils.getFile("app/build/outputs/apk/qa/debug/app-qa-debug.apk").getAbsolutePath().replaceAll("/appium","");
    DesiredCapabilities capabilities = new DesiredCapabilities();
    capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, "samsung");
    capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, "android");
    capabilities.setCapability("automationName", "UiAutomator");
    capabilities.setCapability("app", appPath);
    capabilities.setCapability("appPackage", "org.simple.clinic.qa.debug");
    capabilities.setCapability("appActivity", "org.simple.clinic.activity.TheActivity");
    capabilities.setCapability(MobileCapabilityType.NO_RESET, "true");
    capabilities.setCapability(AndroidMobileCapabilityType.AUTO_GRANT_PERMISSIONS, true);
    capabilities.setCapability("autoAcceptAlerts", true);
    driver = new AppiumDriver(AppiumServerUtils.server.getUrl(), capabilities);
    //driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);

    return driver;

    //androidCapabilities.setCapability(MobileCapabilityType.APP, FileUtils.getFile("app/build/outputs/apk/qa/debug/app-qa-debug.apk").getAbsolutePath());


  }
}
