package com.simplelife.tests.base;

import com.simplelife.tests.builder.AndroidDriverBuilder;
import com.simplelife.utils.AdbCommand;
import io.appium.java_client.AppiumDriver;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;

import java.io.IOException;

import static com.simplelife.AppiumServerUtils.Startserver;
import static com.simplelife.AppiumServerUtils.stopServer;

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
