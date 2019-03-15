package com.simplelife.utils;
import com.github.javafaker.Faker;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.Test;

import java.util.Locale;

public class DataGenrate {


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

    public static String setAge() {
        double random = Math.random();
        random = random * 80 + 1;
        int number = (int) random;
        String age = Integer.toString(number);

        return age;

    }

    @Test
    public void tes()
    {
       boolean b = PatientMedicalHistory.HASDIABETES.yes;
//        System.out.printf(String.valueOf(b));
        String aa = Gendar.MALE.toString();
        System.out.println(aa);


    }
}
