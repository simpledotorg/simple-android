package com.simplelife.tests.annotation;

public enum TesterName {
    AKASH("Gupta Akash");

    public String authorName;
    TesterName(String authorName)
    {
        this.authorName=authorName;
    }

    public String toString() {
        return this.authorName;
    }
}
