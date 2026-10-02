package com.framework.core;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import com.framework.utils.ConfigReader;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Starts the browser, and shuts it down.
 *
 * Only one place in the whole project is allowed to create a WebDriver. If pages each
 * created their own you would get three browsers open at once, and it becomes impossible
 * to tell which one a test is using.
 *
 * The driver is kept in a static variable, so there is one shared browser for the run.
 */
public class DriverFactory {

    private static Logger log = LogManager.getLogger(DriverFactory.class);

    // static = one browser shared by the whole run
    private static WebDriver driver;

    /** returns the browser that is already open */
    public static WebDriver get() {
        return driver;
    }

    /** opens the browser and goes to the website */
    public static WebDriver create() {

        String browser = ConfigReader.browser();
        boolean headless = ConfigReader.isHeadless();

        if (browser.equalsIgnoreCase("edge")) {
            WebDriverManager.edgedriver().setup();
            EdgeOptions options = new EdgeOptions();
            turnOffPasswordWarning(options);
            if (headless) {
                options.addArguments("--headless=new");
            }
            driver = new EdgeDriver(options);

        } else if (browser.equalsIgnoreCase("firefox")) {
            WebDriverManager.firefoxdriver().setup();
            FirefoxOptions options = new FirefoxOptions();
            if (headless) {
                options.addArguments("-headless");
            }
            driver = new FirefoxDriver(options);

        } else {
            // anything unknown falls back to chrome
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
            turnOffPasswordWarning(options);
            if (headless) {
                options.addArguments("--headless=new");
            }
            driver = new ChromeDriver(options);
        }

        driver.manage().window().maximize();
        openSite();

        log.info("Started " + browser + ", headless=" + headless);
        return driver;
    }

    /** closes the browser */
    public static void quit() {
        if (driver == null) {
            return;
        }
        log.info("Closing browser");
        driver.quit();
        driver = null;
    }

    /**
     * Sends the browser back to the sign in page.
     *
     * Called before every test. The browser is opened once for the whole class, so
     * without this the second test would still be sitting on the page the first test
     * finished on, and would look for a login box that is not there.
     */
    public static void navigateToSite() {
        if (driver == null) {
            return;
        }
        openSite();
    }

    private static void openSite() {
        // open a blank page first, otherwise Chrome reopens with the old form values
        // still filled in, and they fight with the website's own inputs
        driver.get("about:blank");

        String url = ConfigReader.url();
        log.info("Opening " + url);
        driver.get(url);
    }

    /**
     * Turns off the "this password was found in a data breach, change it" bubble that
     * Chrome and Edge show after a login.
     *
     * That bubble is drawn by the BROWSER, not by the web page. Selenium cannot see or
     * click it, and it sits on top of the header, so it silently eats clicks meant for
     * the cart link. The test then fails for a reason that has nothing to do with the
     * test. Turning it off avoids a whole class of confusing failures.
     * Chrome and Edge both use the same engine, so both inherit from ChromiumOptions and
     * one method can set the settings for either one.
     */
    private static void turnOffPasswordWarning(ChromiumOptions options) {
        Map<String, Object> settings = new HashMap<String, Object>();
        settings.put("credentials_enable_leak_detection", false);
        settings.put("credentials_enable_service", false);
        settings.put("profile.password_manager_enabled", false);
        settings.put("profile.password_manager_leak_detection", false);

        options.setExperimentalOption("prefs", settings);
        options.addArguments("--disable-features=PasswordLeakDetection,PasswordManagerOnboarding,AutofillServerCommunication");
    }
}
