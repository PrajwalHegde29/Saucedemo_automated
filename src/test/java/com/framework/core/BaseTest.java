package com.framework.core;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import com.framework.models.TestData;
import com.framework.utils.CsvReader;
import com.framework.utils.Report;

/**
 * The parent of every test class.
 *
 * INHERITANCE: LoginTest says "extends BaseTest". That means LoginTest gets the browser
 * and the test data for free, without writing any of that setup itself.
 *
 * This is the most useful thing in the whole framework. Without it, all three tests would
 * repeat the same six setup lines, and if the setup changed you would have to change it in
 * three places and hope you did not miss one.
 *
 * The @Before... and @After... annotations are TestNG instructions. TestNG reads them and
 * runs the method at the right time, so the test methods themselves stay clean.
 */
public class BaseTest {

    private static Logger log = LogManager.getLogger(BaseTest.class);

    protected static final String CREDENTIALS = "testdata/credentials.csv";

    /*
     * Which row of the csv to use.
     *
     * Row 0 is the valid user, row 1 is the user the site refuses.
     * We count from 0, so the first row is 0 and not 1.
     */
    protected static final int VALID_USER = 0;
    protected static final int INVALID_USER = 1;

    // "protected" means the test classes can use this, but the outside world cannot
    protected List<TestData> data;

    /**
     * Runs ONCE before all the tests in the class.
     *
     * Opening a browser is slow, so we do not want to do it three times. Once is enough.
     */
    @BeforeClass(alwaysRun = true)
    public void openBrowser() {
        data = CsvReader.read(CREDENTIALS);
        log.info("Loaded " + data.size() + " rows from the csv");
        DriverFactory.create();
    }

    /**
     * Runs BEFORE EVERY test method.
     *
     * This sends the browser back to the login page each time. Without it, the second test
     * would still be on the page the first test finished on, and would look for a login
     * box that is not there any more.
     *
     * It also means the tests do not depend on each other, so you can run any one of them
     * on its own and it will still work.
     *
     * TestNG always runs a parent's @BeforeMethod before the child's, so LoginTest's own
     * @BeforeMethod still finds the browser on the login page and ready to use.
     */
    @BeforeMethod(alwaysRun = true)
    public void goToSite() {
        DriverFactory.navigateToSite();
    }

    /** Runs ONCE after all the tests in the class, to tidy up */
    @AfterClass(alwaysRun = true)
    public void closeBrowser() {
        DriverFactory.quit();
    }

    /**
     * A shortcut so the tests read as a list of steps.
     * "step("added to cart")" instead of "Report.info("added to cart")".
     */
    protected static void step(String message) {
        Report.info(message);
    }
}
