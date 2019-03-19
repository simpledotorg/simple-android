package org.simple.appium.utils;

import static java.lang.Runtime.getRuntime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class AdbCommand {

  public void executeCommand(String command) throws IOException, InterruptedException {
    Process process = getRuntime().exec(command);
    process.waitFor();
  }

  public String executeAdbCommandAndReadConsoleOutput(String command) throws IOException, InterruptedException {
    Process process = getRuntime().exec(command);
    process.waitFor();
    InputStream is = process.getInputStream();
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
    return bufferedReader.readLine();
  }

  public void executeCommandUsingArray(String[] command) throws IOException, InterruptedException {
    for (String arg : command) {
      Process process = getRuntime().exec(arg);
      process.waitFor();
    }
  }

  public void turnOffWifi() throws IOException, InterruptedException {
    Boolean status = checkWifiStatus();
    if (status.equals(true)) {
      String[] command = new String[] {
          "adb shell am start -a android.intent.action.MAIN -n com.android.settings/.wifi.WifiSettings",
          "sleep 5", "adb shell input keyevent 19", "adb shell input keyevent 19", "adb shell input keyevent 23", "adb shell input keyevent 3"
      };
      executeCommandUsingArray(command);
    }
  }

  public void turnOnWifi() throws IOException, InterruptedException {
    Boolean status = checkWifiStatus();
    if (status.equals(false)) {
      String[] command = new String[] {
          "adb shell am start -a android.intent.action.MAIN -n com.android.settings/.wifi.WifiSettings",
          "sleep 15", "adb shell input keyevent 19", "adb shell input keyevent 23", "adb shell input keyevent 3"
      };
      executeCommandUsingArray(command);
    }
  }

  public void turnOnAirplaneMode() throws IOException, InterruptedException {
    Boolean status = checkAirplaneModeStatus();

    if (status.equals(false)) {
      executeCommand("adb shell settings put global airplane_mode_on 1");
    }
  }

  public void turnOffAirplaneMode() throws IOException, InterruptedException {
    Boolean status = checkAirplaneModeStatus();

    if (status.equals(true)) {
      executeCommand("adb shell settings put global airplane_mode_on 0");
    }
  }

  public void minimizeApp() throws IOException, InterruptedException {
    executeCommand("adb shell input keyevent 3");
  }

  public Boolean checkWifiStatus() throws IOException, InterruptedException {
    Boolean flag = false;
    String wifiStatusCommand = "adb shell dumpsys wifi | grep \"mNetworkInfo\" ";
    String[] consoleOutput = executeAdbCommandAndReadConsoleOutput(wifiStatusCommand).split(",");

    for (String output : consoleOutput) {
      if (output.equals(" state: CONNECTED/CONNECTED")) {
        flag = true;
        break;
      }
    }
    return flag;
  }

  public Boolean checkAirplaneModeStatus() throws IOException, InterruptedException {
    Boolean flag = false;
    String airplaneModeStatus = "adb shell settings get global airplane_mode_on";
    String consoleOutput = executeAdbCommandAndReadConsoleOutput(airplaneModeStatus);

    if (consoleOutput.equals("1")) {
      flag = true;
    }
    return flag;
  }
}
