package com.framework.pages;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.framework.utils.Report;

/**
 * The sign in screen. This class knows about the login form and the red error box.
 *
 * A page object holds the locators and the actions for ONE screen. The test then reads
 * like a list of steps instead of a pile of CSS selectors.
 *
 * It extends BasePage, so type(), click() and waitFor() come for free.
 */
public class LoginPage extends BasePage {

    // the red error box. This one is found the long way, because we need it in two places
    private static final String ERROR_BOX = "[data-test='error']";

    // how many times to try the login before deciding something is really wrong
    private static final int ATTEMPTS = 3;

    /*
     * @FindBy means "find this element using this locator".
     *
     * Selenium looks them up for us in the constructor below, so we can just say
     * "usernameInput" everywhere else instead of repeating the id.
     */
    @FindBy(id = "user-name")
    private WebElement usernameInput;

    @FindBy(id = "password")
    private WebElement passwordInput;

    @FindBy(id = "login-button")
    private WebElement loginBtn;

    public LoginPage(WebDriver driver) {
        super(driver);
        // this is the line that actually finds all the @FindBy elements
        PageFactory.initElements(driver, this);
    }

    /**
     * Fills the form and clicks login, then waits to see what happened.
     *
     * The password is logged nowhere on purpose. This message ends up in reports and in
     * bug tickets, so it only ever mentions the username.
     */
    public void login(String username, String password) {
        Report.info("Login as " + username);

        /*
         * Why try more than once?
         *
         * This page is built with JavaScript. If we click the button before the page has
         * finished loading, the click does an ordinary form submit, the page reloads, and
         * the boxes are empty again. The test then fails even though the site is fine.
         *
         * Trying a couple of times turns that timing problem into a non-event. It costs a
         * few seconds only when something is genuinely wrong, and then we throw an error
         * with a clear message.
         */
        for (int attempt = 1; attempt <= ATTEMPTS; attempt++) {

            waitFor().until(ExpectedConditions.visibilityOf(usernameInput));
            type(usernameInput, username);
            type(passwordInput, password);
            click(loginBtn);

            if (isOnHomePage()) {
                Report.info("Logged in on attempt " + attempt);
                return;
            }

            if (isErrorShown()) {
                Report.info("Login was refused on attempt " + attempt);
                return;
            }

            // neither happened, so the page most likely reloaded, try again
            Report.info("Attempt " + attempt + " went nowhere, trying again");
        }

        throw new RuntimeException("Login did not go through after " + ATTEMPTS + " attempts");
    }

    /** true if the address bar shows the inventory page, which means login worked */
    public boolean isOnHomePage() {
        try {
            // we do not want to wait the full 20 seconds just to find out it is "no",
            // so this one uses the short timeout
            waitFor(QUICK_TIMEOUT).until(ExpectedConditions.urlContains("inventory.html"));
            return true;
        } catch (TimeoutException e) {
            // a timeout here is a normal answer, not a problem, so we return false
            return false;
        }
    }

    /** waits for the red error box and returns the text inside it */
    public String getError() {
        WebElement box = waitFor().until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(ERROR_BOX)));
        return box.getText().trim();
    }

    /** true if a red error message is showing */
    public boolean isErrorShown() {
        List<WebElement> boxes = driver.findElements(By.cssSelector(ERROR_BOX));
        if (boxes.size() > 0) {
            return boxes.get(0).getText().trim().length() > 0;
        }
        return false;
    }
}
