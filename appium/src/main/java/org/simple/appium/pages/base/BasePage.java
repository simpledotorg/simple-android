package org.simple.appium.pages.base;

import com.google.common.collect.ImmutableMap;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.touch.offset.PointOption;

public class BasePage {

  public AppiumDriver appiumDriver;
  AndroidDriver androidDriver;
  WebDriverWait wait;
  private int timeout = 20;
  private int pollingTime = 100;

  public BasePage(AppiumDriver appiumDriver) {
    this.appiumDriver = appiumDriver;
    wait = new WebDriverWait(appiumDriver, 20);
    init();
  }

  public void init() {
    PageFactory.initElements(this.appiumDriver, this);
  }

  public boolean waitForElement(WebElement element) {
    Wait wait = new FluentWait(appiumDriver)
        .withTimeout(Duration.ofSeconds(timeout))
        .pollingEvery(Duration.ofMillis(pollingTime))
        .ignoring(NoSuchElementException.class)
        .ignoring(StaleElementReferenceException.class);
    wait.until((Function<WebDriver, WebElement>) appiumDriver -> element);
    return element.isDisplayed();
  }

  public boolean isExists(WebElement element) {
    try {
      waitForElement(element);
      return element.isDisplayed();
    } catch (Throwable throwable) {
      throwable.printStackTrace();
    }
    return false;
  }

  public void waitVisibilityOf(WebElement element) {
    wait.until(ExpectedConditions.visibilityOf(element));
  }

  public void waitElementToBeClickable(WebElement element) {
    wait.until(ExpectedConditions.elementToBeClickable(element));
  }

  public void waitVisibilityOfAllElements(List<WebElement> elements) {
    wait.until(ExpectedConditions.visibilityOfAllElements(elements));
  }

  public void sendKeys(WebElement elem, String text) {
    waitElementToBeClickable(elem);
    elem.click();
    if (text != null) {
      if (!elem.getText().isEmpty()) {
        elem.clear();
      }
      elem.sendKeys(text);
    } else {
      Assert.assertNotNull(elem.getText());
    }
  }

  public void hideKeyboard() {
    try {
      appiumDriver.hideKeyboard();
    } catch (WebDriverException e) {
      e.printStackTrace();
    }
  }

  public void clickOnElement(WebElement element) {
    waitElementToBeClickable(element);
    try {
      element.click();
    } catch (Exception e) {
      JavascriptExecutor executer = (JavascriptExecutor) appiumDriver;
      executer.executeScript("arguments[0].click();", element);
    }
  }

  public void clickOnDoneButtonOnKeypad() {
    appiumDriver.executeScript("mobile:performEditorAction", ImmutableMap.of("action", "done"));

  }

  public void scrollDown() {
    int pressX = appiumDriver.manage().window().getSize().width / 2;
    int bottomY = appiumDriver.manage().window().getSize().height * 4 / 5;
    int topY = appiumDriver.manage().window().getSize().height / 8;
    scroll(pressX, bottomY, pressX, topY);
  }

  private void scroll(int fromX, int fromY, int toX, int toY) {
    TouchAction touchAction = new TouchAction(appiumDriver);
    PointOption pointOption = new PointOption();
    touchAction.longPress(pointOption.withCoordinates(fromX, fromY)).moveTo(pointOption.withCoordinates(toX, toY)).release().perform();
  }

  public void scrollDownTo(String text) {

    scrollDownTo(By.xpath("//*[@text=\"" + text + "\"]"));
  }

  public void scrollDownTo(By byOfElementToBeFound) {
    hideKeyboard();
    int counter = 0;
    while (counter < 12) {
      if (appiumDriver.findElements(byOfElementToBeFound).size() > 0) { return; }

      scrollDown1();

      counter++;
    }
    Assert.fail("Did not find : " + byOfElementToBeFound.toString());
  }

  public void scrollDown1() {
    int height = appiumDriver.manage().window().getSize().getHeight();
    scroll(5, height * 2 / 3, 5, height / 3);
  }

  public void scrollUp() {

    int height = appiumDriver.manage().window().getSize().getHeight();

    scroll(5, height / 3, 5, height * 2 / 3);
  }
}
