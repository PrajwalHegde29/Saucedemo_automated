package com.framework.pages;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.framework.utils.Report;

/**
 * The home page product grid, and the cart page.
 *
 * These two screens are grouped together because they are small and both are about
 * "what is in the cart".
 */
public class CartPage extends BasePage {

    @FindBy(id = "add-to-cart-sauce-labs-backpack")
    private WebElement addToCartBtn;

    @FindBy(css = "a.shopping_cart_link")
    private WebElement cartLink;

    @FindBy(css = ".cart_item")
    private List<WebElement> itemsInCart;

    public CartPage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    public void addProductToCart() {
        click(addToCartBtn);
        Report.info("Added Sauce Labs Backpack to the cart");
    }

    public void openCart() {
        click(cartLink);
        waitForUrl("cart.html");
        Report.info("Opened the cart");
    }

    /** how many different items are in the cart */
    public int getItemCount() {
        // a List is a collection that keeps order, so it has a .size() method
        waitFor().until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".cart_item")));
        return itemsInCart.size();
    }
}
