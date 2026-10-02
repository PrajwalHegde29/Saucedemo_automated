package com.framework.pages;

import java.time.Duration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Shared code for every page object.
 *
 * This is INHERITANCE. CartPage, CheckoutPage and LoginPage all write
 * "extends BasePage", which means they automatically get the three helper methods below
 * without writing them again.
 *
 * Why bother? Because without this, every page would invent its own wait time and its
 * own way of typing into a box. That is how a suite ends up with a test that passes in
 * one place and randomly fails in another.
 */
public class BasePage {

    // how long to wait before giving up, in seconds
    protected static final int DEFAULT_TIMEOUT = 20;

    // a shorter wait, for checks where we do not mind a quick "no"
    protected static final int QUICK_TIMEOUT = 5;

    // the browser, shared with whoever created this page object
    protected WebDriver driver;

    /** every page object needs the browser, so it is passed in when the page is created */
    public BasePage(WebDriver driver) {
        this.driver = driver;
    }

    /**
     * Creates a "waiter". You then ask it to wait for something.
     *
     * Why wait at all? Because a web page arrives in pieces. You ask for a button, the
     * button is not there YET, and Selenium throws NoSuchElementException straight away.
     * A waiter asks again every half second until the thing shows up or the time runs out.
     * This one line is the difference between a reliable test and a flaky test.
     *
     * Duration.ofSeconds(20) just turns the number 20 into the "20 seconds" object that
     * Selenium 4 asks for. Nothing clever, just the newer way of writing it.
     */
    protected WebDriverWait waitFor(int seconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(seconds));
    }

    protected WebDriverWait waitFor() {
        return new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT));
    }

    /**
     * Clears the box then types into it.
     *
     * The clear() matters. This website is built with React, and if you type into a box
     * that already has text, React sometimes ends up with both the old and new value.
     */
    protected void type(WebElement box, String text) {
        box.clear();
        box.sendKeys(text);
    }

    /** waits until the element can actually be clicked, then clicks it */
    protected void click(WebElement element) {
        waitFor().until(ExpectedConditions.elementToBeClickable(element)).click();
    }

    /** waits until the address bar contains the given text */
    protected void waitForUrl(String partOfUrl) {
        waitFor().until(ExpectedConditions.urlContains(partOfUrl));
    }
}
