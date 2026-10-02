package com.framework.models;

/**
 * Holds ONE row of credentials.csv.
 *
 * This is a very plain class on purpose: public fields and nothing else. Think of it as a
 * form with six boxes. CsvReader fills the boxes, the test reads the boxes.
 *
 * It sits in src/test/java because it only describes test data. Nothing in the framework
 * needs it, so it should not sit in the framework.
 */
public class TestData {

    public String username;
    public String password;
    public String error;
    public String firstname;
    public String lastname;
    public String zipcode;

    /**
     * Only used if a test fails and Java prints the object to explain the failure.
     * The password is deliberately left out, because a failure message is exactly the
     * kind of thing that gets pasted into a ticket or a chat.
     */
    @Override
    public String toString() {
        return "TestData[username=" + username + "]";
    }
}
