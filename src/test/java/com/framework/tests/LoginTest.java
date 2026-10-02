package com.framework.tests;

import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.framework.core.BaseTest;
import com.framework.core.DriverFactory;
import com.framework.models.TestData;
import com.framework.pages.CartPage;
import com.framework.pages.CheckoutPage;
import com.framework.pages.LoginPage;

/**
 * The three test cases.
 *
 * "extends BaseTest" is the inheritance bit. This class does not open a browser, does not
 * read the csv and does not close anything, because it inherits all of that from BaseTest.
 *
 * Every test does four things and nothing else:
 *    1. get the data it needs
 *    2. drive the page objects
 *    3. check the result with Assert
 *    4. log each step, which takes the screenshot as it goes
 */
public class LoginTest extends BaseTest {

    private LoginPage loginPage;
    private CartPage cartPage;
    private CheckoutPage checkoutPage;

    /**
     * Runs before every test method in this class.
     *
     * Page objects are cheap to create, so we make fresh ones each time rather than once
     * for the class. That way a leftover element from the previous test cannot be reused.
     *
     * Note the order: BaseTest's @BeforeMethod already sent us to the login page, so this
     * one runs second and the page objects find the right screen.
     */
    @BeforeMethod(alwaysRun = true)
    public void createPageObjects() {
        WebDriver browser = DriverFactory.get();

        loginPage = new LoginPage(browser);
        cartPage = new CartPage(browser);
        checkoutPage = new CheckoutPage(browser);
    }

    @Test(priority = 1, groups = "smoke",
          description = "Valid login lands on the home page")
    public void testValidLogin() {
        TestData user = data.get(VALID_USER);

        loginPage.login(user.username, user.password);

        Assert.assertTrue(loginPage.isOnHomePage(),
                "Valid login should land on the home page");
        step("Home page confirmed");
    }

    @Test(priority = 2, groups = "smoke",
          description = "Invalid login shows the error message")
    public void testInvalidLoginError() {
        TestData user = data.get(INVALID_USER);

        loginPage.login(user.username, user.password);

        // assertEquals(firstValue, secondValue, messageIfItFails)
        // the expected value comes from the csv, not from the code, so changing the
        // expected message means editing the csv and nothing else
        Assert.assertEquals(loginPage.getError(), user.error,
                "Invalid login should show the error message");
        step("Error message matched the csv");
    }

    @Test(priority = 3, groups = "regression",
          description = "Login, add to cart and place the order")
    public void testAddToCartAndCheckout() {
        TestData user = data.get(VALID_USER);

        // --- sign in ---
        loginPage.login(user.username, user.password);
        Assert.assertTrue(loginPage.isOnHomePage(),
                "Valid login should land on the home page");
        step("Home page confirmed");

        // --- add something to the cart and check it arrived ---
        cartPage.addProductToCart();
        cartPage.openCart();
        Assert.assertEquals(cartPage.getItemCount(), 1,
                "The cart should hold the one product that was added");
        step("Cart holds exactly 1 item");

        // --- check out ---
        checkoutPage.startCheckout();
        checkoutPage.fillInformation(user.firstname, user.lastname, user.zipcode);
        checkoutPage.continueToOverview();
        checkoutPage.placeOrder();

        Assert.assertEquals(checkoutPage.getCompleteText(), "Checkout: Complete!",
                "The order should be placed successfully");
        step("Order placed successfully");
    }
}
