package com.framework.utils;

import java.util.Base64;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

/**
 * Builds the Extent HTML report.
 *
 * Two things are static here, and it helps to know why:
 *
 *   extent - the whole report. Created once, written out at the very end.
 *   test   - the test currently running. Extent needs a test to be "started" before any
 *            step can be added to it, so we keep it here and swap it per test.
 *
 * Two version choices, both forced by this project using Java 8:
 *
 *   - ExtentReports 4.1.6, not 5.x. The 5.x jar files are built for Java 11 and will not
 *     load here, even though the 5.x notes claim Java 8 works.
 *   - ExtentSparkReporter, not ExtentHtmlReporter. Both exist in 4.1.6, but only the
 *     Spark version actually writes a screenshot into index.html. Tested it: with
 *     ExtentHtmlReporter the attached screenshot is silently thrown away.
 */
public class ExtentManager {

    private static Logger log = LogManager.getLogger(ExtentManager.class);

    /*
     * The report file. Note it is reports/extent/index.html at the project root, not
     * inside target/, because `mvn clean` deletes target/ and we do not want a finished
     * report thrown away.
     *
     * The folder is created for us when the report is written at the end.
     */
    private static final String REPORT_FILE = "reports/extent/index.html";

    private static ExtentReports extent;
    private static ExtentTest test;

    /** called once when the whole suite starts */
    public static void startReport(String suiteName) {

        // step 1: decide what the report looks like
        ExtentSparkReporter spark = new ExtentSparkReporter(REPORT_FILE);
        spark.config().setReportName(suiteName);
        spark.config().setTheme(Theme.STANDARD);

        // step 2: create the report and plug the design into it
        extent = new ExtentReports();
        extent.attachReporter(spark);
    }

    /** called at the start of each test, so steps go into the right place */
    public static void startTest(String name) {
        test = extent.createTest(name);
    }

    /** a normal step: adds a line AND a screenshot */
    public static void step(String message) {
        if (test == null) {
            return;
        }
        test.info(message);
        addScreenshot(ScreenshotStore.capture(message));
    }

    public static void pass(String message) {
        if (test != null) {
            test.pass(message);
        }
    }

    public static void fail(String message) {
        if (test != null) {
            test.fail(message);
        }
    }

    public static void skip(String message) {
        if (test != null) {
            test.skip(message);
        }
    }

    /**
     * Hands the picture to Extent as text instead of as a file path.
     *
     * Base64 turns the image bytes into a long text string that can sit inside the HTML
     * file. That is why one index.html can be emailed on its own and still show the
     * pictures. If we passed a file path instead, the pictures would break the moment
     * someone moved or zipped the report.
     */
    private static void addScreenshot(byte[] pictureBytes) {
        if (pictureBytes == null) {
            return;
        }
        try {
            String asText = Base64.getEncoder().encodeToString(pictureBytes);
            test.addScreenCaptureFromBase64String(asText);
        } catch (RuntimeException e) {
            // losing one picture is not worth failing the whole report over
            log.warn("Could not add a screenshot to the report", e);
        }
    }

    /** forget the finished test */
    public static void endTest() {
        test = null;
    }

    /** writes everything collected so far to disk. Must be the last call. */
    public static void flush() {
        if (extent != null) {
            extent.flush();
        }
    }
}
