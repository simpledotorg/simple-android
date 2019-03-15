package com.simplelife;

import io.appium.java_client.service.local.AppiumDriverLocalService;

public class AppiumServerUtils {
    public static AppiumDriverLocalService server;

    public static void Startserver() {
        server = AppiumDriverLocalService.buildDefaultService();
        server.start();
    }

    public static void stopServer() {
        server.stop();
    }
}
