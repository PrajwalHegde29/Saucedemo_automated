package com.framework.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The ONE place tests are allowed to log a step.
 *
 * Why not just call Log4j and Extent directly from the page objects? Because then every
 * page would need to know about both tools, and changing the reporting later would mean
 * editing every page.
 *
 * So a page object calls Report.info("something happened") and this class quietly does
 * three things:
 *    1. writes a line to automation.log
 *    2. adds a line to the Extent report
 *    3. takes a screenshot for that step
 *
 * The page object does not know any of that is happening. That is the whole point.
 */
public class Report {

    // one shared log file writer for this class
    private static Logger log = LogManager.getLogger(Report.class);

    /**
     * Use this for a normal step. One call = one log line + one report line + one screenshot.
     */
    public static void info(String message) {
        log.info(message);
        ExtentManager.step(message);
    }

    /** the test worked */
    public static void pass(String message) {
        log.info(message);
        ExtentManager.pass(message);
    }

    /** the test failed. The message should never contain a password */
    public static void fail(String message) {
        log.error(message);
        ExtentManager.fail(message);
    }

    /** the test did not run */
    public static void skip(String message) {
        log.info(message);
        ExtentManager.skip(message);
    }
}
