package com.simplelife.tests.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TestCaseNotes {

    String Title() default "";
    String Steps();
    String category() default "";

    String expecatedResult() default "";

    Priority priority()
            default Priority.MEDIUM;

}
