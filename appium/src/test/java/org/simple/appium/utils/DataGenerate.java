package org.simple.appium.utils;

import com.github.javafaker.Faker;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.Locale;

public class DataGenerate {


  public static String setAge() {
    double random = Math.random();
    random = random * 80 + 1;
    int number = (int) random;
    String age = Integer.toString(number);

    return age;

  }

  public String setName() {
    Faker faker = new Faker(new Locale("en-IND"));
    String fullName = faker.name().fullName();
    System.out.println(fullName);
    return fullName;
  }

  public String setAddress() {
    Faker faker = new Faker(new Locale("en-IND"));
    String address = faker.address().fullAddress();
    return address;
  }

  public String setPhoneNumber() {
    String phoneNumber = "1" + RandomStringUtils.randomNumeric(9);
    return phoneNumber;
  }

}
