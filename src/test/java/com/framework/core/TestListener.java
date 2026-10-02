package com.framework.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.framework.utils.ExtentManager;
import com.framework.utils.Report;
import com.framework.utils.ScreenshotStore;

/**
 * TestNG calls the methods in this class every time something happens in the suite.
 *
 * This is how the reporting works without the test code having to do any of it. When a
 * test starts, TestNG calls onTestStart. When it ends, TestNG calls onTestSuccess,
 * onTestFailure or onTestSkipped. We never have to remember to call them.
 *
 * It is registered in testng.xml, not as a parent of the test classes.
 *
 * One important TestNG detail: TestNG builds a SEPARATE instance of this class, so this
 * class cannot see the browser that BaseTest created. That is why it asks for the browser
 * with DriverFactory.get() instead of holding one of its own.
 */
public class TestListener implements ITestListener {

    private static Logger log = LogManager.getLogger(TestListener.class);

    /** once at the very start of the suite */
    @Override
    public void onStart(ITestContext context) {
        log.info("Suite starting: " + context.getName());
        ScreenshotStore.beginSuite();
        ExtentManager.startReport(context.getName());
    }

    /** once at the very end of the suite */
    @Override
    public void onFinish(ITestContext context) {
        log.info("Suite finished, writing the reports");
        ExtentManager.flush();
    }

    /** called just before each test method */
    @Override
    public void onTestStart(ITestResult result) {
        String name = getName(result);
        log.info("--- test start: " + name);

        ExtentManager.startTest(name);
        ScreenshotStore.beginTest(name);
    }

    /** called when a test passes */
    @Override
    public void onTestSuccess(ITestResult result) {
        log.info("--- test passed: " + getName(result));
        Report.pass("Test completed without errors");
        finishTest();
    }

    /** called when a test fails */
    @Override
    public void onTestFailure(ITestResult result) {
        String name = getName(result);
        Throwable error = result.getThrowable();

        /*
         * The second argument is the real error. Passing it to the log is what puts the
         * full stack trace into automation.log, which is the whole reason for having
         * Log4j alongside the Extent report.
         */
        log.error("--- test FAILED: " + name, error);

        if (error == null) {
            Report.fail("Test failed");
        } else {
            Report.fail(error.toString());
        }
        finishTest();
    }

    /** called when a test is skipped */
    @Override
    public void onTestSkipped(ITestResult result) {
        log.info("--- test skipped: " + getName(result));
        Report.skip("Test was skipped");
        finishTest();
    }

    /** saves the Word document, then closes the test in the Extent report */
    private void finishTest() {
        ScreenshotStore.finishTest();
        ExtentManager.endTest();
    }

    /**
     * Works out what to call this test in the report.
     *
     * We prefer the description from @Test(description = "...") because it reads better
     * than a method name: "Invalid login shows the error message" instead of
     * "testInvalidLoginError". If there is no description we use the method name.
     */
    private String getName(ITestResult result) {
        String description = result.getMethod().getDescription();

        if (description != null && description.trim().length() > 0) {
            return description;
        }
        return result.getMethod().getMethodName();
    }
}
