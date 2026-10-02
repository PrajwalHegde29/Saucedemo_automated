package com.framework.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.framework.utils.Report;

/**
 * The checkout steps and the final "thank you" screen.
 */
public class CheckoutPage extends BasePage {

    @FindBy(id = "checkout")
    private WebElement checkoutBtn;

    @FindBy(id = "first-name")
    private WebElement firstNameInput;

    @FindBy(id = "last-name")
    private WebElement lastNameInput;

    @FindBy(id = "postal-code")
    private WebElement zipInput;

    @FindBy(id = "continue")
    private WebElement continueBtn;

    @FindBy(id = "finish")
    private WebElement finishBtn;

    @FindBy(css = ".header_secondary_container .title")
    private WebElement pageTitle;

    public CheckoutPage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    public void startCheckout() {
        click(checkoutBtn);
        waitForUrl("checkout-step-one");
        Report.info("Started checkout");
    }

    public void fillInformation(String firstName, String lastName, String zip) {
        Report.info("Filling checkout details: " + firstName + " " + lastName + " " + zip);

        waitFor().until(ExpectedConditions.visibilityOf(firstNameInput));
        type(firstNameInput, firstName);
        type(lastNameInput, lastName);
        type(zipInput, zip);
    }

    public void continueToOverview() {
        click(continueBtn);
        waitForUrl("checkout-step-two");
        Report.info("Reviewed the order details");
    }

    public void placeOrder() {
        click(finishBtn);
        waitForUrl("checkout-complete");
        Report.info("Placed the order");
    }

    /** the big heading on the confirmation screen, for example "Checkout: Complete!" */
    public String getCompleteText() {
        waitFor().until(ExpectedConditions.visibilityOf(pageTitle));
        return pageTitle.getText().trim();
    }
}
