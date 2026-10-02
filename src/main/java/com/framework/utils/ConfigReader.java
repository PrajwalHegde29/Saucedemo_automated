package com.framework.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Reads the file config.properties and gives the values to anyone who asks.
 *
 * Why bother? Because the browser, the website address and the headless switch are
 * things you often want to change without recompiling. If they were hardcoded in Java
 * you would have to edit code every time. Now you edit one text file.
 *
 * Everything here is static, meaning you call it like ConfigReader.browser()
 * without creating an object.
 */
public class ConfigReader {

    // A Properties object is just a key + value box that reads .properties files.
    // static means there is only ONE box for the whole program, shared by everyone.
    private static Properties data = new Properties();

    /*
     * The static block runs ONE time, automatically, the first time this class is used.
     * We use it here to read the file before any test asks for a value.
     *
     * getResourceAsStream finds the file inside src/main/resources on the classpath.
     * This is the standard way to read a file that is bundled with the code.
     */
    static {
        try {
            InputStream file = ConfigReader.class.getClassLoader()
                    .getResourceAsStream("config.properties");

            if (file == null) {
                throw new RuntimeException("config.properties is missing from src/main/resources");
            }

            // load() copies every key=value line from the file into the "data" box
            data.load(file);
            file.close();

        } catch (IOException e) {
            // If reading fails we stop the whole run straight away. Running the tests
            // with no config would fail later in a much more confusing way.
            throw new RuntimeException("Could not read config.properties", e);
        }
    }

    /**
     * One general method to read any key. Then the small methods below give the keys
     * friendly names so the rest of the code never sees a raw string like "browser".
     */
    public static String get(String key) {
        return data.getProperty(key);
    }

    /** returns "chrome" if the file does not mention a browser */
    public static String browser() {
        String value = data.getProperty("browser");
        if (value == null) {
            return "chrome";
        }
        return value;
    }

    /** the website we are testing */
    public static String url() {
        return data.getProperty("url");
    }

    /**
     * headless=true means run without opening a visible browser window.
     * The file holds text, so "true" has to be turned into a real boolean.
     * Boolean.parseBoolean does that, and gives false for anything it does not understand.
     */
    public static boolean isHeadless() {
        return Boolean.parseBoolean(data.getProperty("headless"));
    }
}
