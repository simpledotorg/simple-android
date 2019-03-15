package org.simple.appium.tests.base;

import static org.simple.appium.AppiumServerUtils.Startserver;
import static org.simple.appium.AppiumServerUtils.stopServer;

import org.simple.appium.tests.builder.AndroidDriverBuilder;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;

import java.io.IOException;

import io.appium.java_client.AppiumDriver;

public class BaseTest {

  public static AppiumDriver appiumDriver;

  @BeforeTest(description = "Start appium server")
  public void start() {
    Startserver();
  }

  @AfterTest(description = "Stop Appium server")
  public void teraDown() {
    stopServer();
  }

  @BeforeMethod(description = "Build the Appium driver")
  public void buildDriver() throws IOException, InterruptedException {
//        AdbCommand adbCommand=new AdbCommand();
//        adbCommand.turnOffWifi();
    appiumDriver = AndroidDriverBuilder.buildDriver();

  }
}
