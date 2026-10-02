package com.framework.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.common.usermodel.PictureType;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import com.framework.core.DriverFactory;

/**
 * Takes a picture at every step, and saves one Word document per test.
 *
 * The only files written to disk are the documents:
 *
 *   screenshots/
 *     TC01_Valid_login_lands_on_the_home_page.docx
 *     TC02_Invalid_login_shows_the_error_message.docx
 *     TC03_Login__add_to_cart_and_place_the_order.docx
 *
 * The pictures are held in memory as bytes and put straight into the Word file. Nothing
 * is written to a temporary folder, so a run that crashes half way does not leave loose
 * .png files lying around.
 *
 * HONEST NOTE: the Word writing part uses Apache POI. Those calls cannot be simplified,
 * because that is simply the API POI gives us. The idea behind them is only:
 *     new XWPFDocument()          -> a blank Word document
 *     createParagraph()           -> add a line
 *     run.setText("...")          -> put text on that line
 *     run.addPicture(...)         -> put a picture on that line
 *     document.write(out)         -> save the file
 */
public class ScreenshotStore {

    private static Logger log = LogManager.getLogger(ScreenshotStore.class);

    // where the documents are saved
    private static final String FOLDER = "screenshots";

    /*
     * A screenshot is as wide as the browser window, which is wider than a page of
     * A4 paper. Left at full size, Word pushes the picture off the side of the page, so
     * we shrink it to fit. 600 pixels wide is a good readable size on A4.
     */
    private static final int MAX_WIDTH_PX = 600;

    /* An inner class: a small class defined inside another class. One picture plus the
     * step it belongs to. We keep these in a List until the test finishes. */
    private static class Shot {
        byte[] image;
        String caption;

        Shot(byte[] image, String caption) {
            this.image = image;
            this.caption = caption;
        }
    }

    private static List<Shot> pictures = new ArrayList<Shot>();

    // the test we are working on right now
    private static String testName = "";

    // TC01, TC02, ... counts up as tests run
    private static int testNumber;

    /** called once when the whole suite starts, so numbering restarts at TC01 every run */
    public static void beginSuite() {
        testNumber = 0;
    }

    /**
     * Called at the start of each test. Forgets the previous test's pictures, so this
     * document starts empty, and bumps the number.
     */
    public static void beginTest(String name) {
        testName = name;
        testNumber = testNumber + 1;
        pictures.clear();
    }

    /** returns TC01, TC02, TC03 ... used in the filename and as the heading */
    public static String id() {
        if (testNumber < 10) {
            return "TC0" + testNumber;
        }
        return "TC" + testNumber;
    }

    /**
     * Takes ONE picture of the current screen and keeps it for this test's document.
     *
     * getScreenshotAs(OutputType.BYTES) is the important line. Selenium can hand back a
     * picture as a file, as base64 text, or as raw bytes. We want bytes, because bytes
     * go straight into the Word file with nothing written to disk in between.
     *
     * @return the picture, or null if there was no browser or the shot failed
     */
    public static byte[] capture(String caption) {
        WebDriver browser = DriverFactory.get();

        if (browser == null) {
            log.info("No browser open, skipping the screenshot for [" + caption + "]");
            return null;
        }

        try {
            byte[] image = ((TakesScreenshot) browser).getScreenshotAs(OutputType.BYTES);
            Shot shot = new Shot(image, caption);
            pictures.add(shot);
            return image;

        } catch (RuntimeException e) {
            /*
             * A missing picture must never turn a passing test into a failing one.
             * Catch it, write it in the log, carry on.
             */
            log.warn("Could not take a screenshot for [" + caption + "]", e);
            return null;
        }
    }

    /**
     * Writes this test's Word document. Called when the test finishes.
     *
     * If the document cannot be written, that is written in the log and nothing else
     * happens, because a broken document is not a test failure.
     */
    public static void finishTest() {

        if (pictures.size() == 0) {
            log.info("No screenshots for [" + testName + "], no document written");
            return;
        }

        File folder = new File(FOLDER);
        if (!folder.exists()) {
            folder.mkdirs();       // creates screenshots/ if it is not there yet
        }

        String fileName = id() + "_" + cleanFileName(testName) + ".docx";
        File document = new File(folder, fileName);

        XWPFDocument word = new XWPFDocument();

        try {
            // first line of the document: TC01 - Valid login lands on the home page
            word.createParagraph().createRun().setText(id() + " - " + testName);

            // then every step, with its picture underneath
            for (int i = 0; i < pictures.size(); i++) {
                Shot shot = pictures.get(i);
                word.createParagraph().createRun().setText(shot.caption);
                addPicture(word, shot);
            }

            // write() saves the document, and the file stream is what it saves into
            OutputStream out = new FileOutputStream(document);
            word.write(out);
            out.close();
            word.close();

            log.info("Document written: " + document.getPath()
                    + " (" + pictures.size() + " screenshots)");

        } catch (IOException e) {
            log.error("Could not write " + document.getPath(), e);

        } catch (RuntimeException e) {
            log.error("Could not write " + document.getPath(), e);
        }

        pictures.clear();
    }

    /**
     * Puts one picture into the Word document.
     */
    private static void addPicture(XWPFDocument word, Shot shot) {

        // ImageIO reads the image so we can find out how wide and tall it is.
        // NOTE: it uses up the stream it is given, so we give it its own copy.
        BufferedImage size;
        try {
            size = ImageIO.read(new ByteArrayInputStream(shot.image));
        } catch (IOException e) {
            log.error("Could not read the image for [" + shot.caption + "]", e);
            return;
        }

        if (size == null) {
            log.warn("The image was not readable, skipped the step [" + shot.caption + "]");
            return;
        }

        // work out the new size, keeping the width/height proportion the same
        double scale = 1.0;
        if (size.getWidth() > MAX_WIDTH_PX) {
            scale = (double) MAX_WIDTH_PX / size.getWidth();
        }
        int newWidth = (int) (size.getWidth() * scale);
        int newHeight = (int) (size.getHeight() * scale);

        try {
            // a paragraph is a line in the document, a run is the text or picture on it
            XWPFParagraph paragraph = word.createParagraph();
            XWPFRun run = paragraph.createRun();

            // Units.EMU_PER_PIXEL is 9525. Word measures in EMU, not pixels, so this
            // converts our pixel sizes into what Word expects.
            InputStream in = new ByteArrayInputStream(shot.image);
            run.addPicture(in, PictureType.PNG, "screenshot",
                    newWidth * Units.EMU_PER_PIXEL,
                    newHeight * Units.EMU_PER_PIXEL);
            in.close();

        } catch (InvalidFormatException e) {
            log.error("Word rejected the image for [" + shot.caption + "]", e);

        } catch (IOException e) {
            log.error("Could not add the image for [" + shot.caption + "]", e);
        }
    }

    /**
     * Makes the test name safe to use in a filename.
     *
     * Two reasons this is needed:
     *   1. Windows does not allow  \ / : * ? " < > |  in a filename, and our test names
     *      contain a colon, for example "Checkout: Complete!".
     *   2. Spaces in a filename are a nuisance, because every command that touches the
     *      file then needs quotes around it.
     *
     * So we walk through the name one character at a time and keep only the safe ones.
     * Letters, numbers, dots, underscores and dashes stay, everything else becomes "_".
     *
     * "Login, add to cart and place the order" becomes "Login__add_to_cart_and_place_the_order"
     */
    private static String cleanFileName(String name) {
        String result = "";

        for (int i = 0; i < name.length(); i++) {
            char one = name.charAt(i);

            // Character.isLetterOrDigit asks if it is a letter (a-z) or a number (0-9)
            boolean isSafe = Character.isLetterOrDigit(one)
                    || one == '.' || one == '_' || one == '-';

            if (isSafe) {
                result = result + one;
            } else {
                result = result + "_";
            }
        }

        return result;
    }
}
