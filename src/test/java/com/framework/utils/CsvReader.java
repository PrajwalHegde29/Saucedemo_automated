package com.framework.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.framework.models.TestData;

/**
 * Reads testdata/credentials.csv and gives back a list of TestData objects.
 *
 * Why a csv at all? So the passwords are not typed inside the test code. You can change
 * a password in one file instead of hunting through Java files, and the password is not
 * sitting in the source.
 *
 * WHY IS THIS IN src/test/java AND NOT WITH THE OTHER utils?
 *
 * Because this class mentions TestData, and TestData is test data. Java will not let a
 * class in src/main/java import a class in src/test/java. So as soon as this reader
 * names TestData, it is test code, and it belongs here next to the tests.
 *
 * A more advanced version of this class would take the "how to build one row" as an
 * argument, so it would not mention TestData at all and could sit in src/main/java. That
 * is better design, but it needs generics and method references, which are harder to
 * read. This version is the plain one.
 */
public class CsvReader {

    /*
     * The column positions in the csv. These numbers have to match the order of the
     * columns in the file.
     *
     * We count from 0, not 1. That is normal in programming.
     */
    private static final int USERNAME = 0;
    private static final int PASSWORD = 1;
    private static final int ERROR = 3;
    private static final int FIRSTNAME = 4;
    private static final int LASTNAME = 5;
    private static final int ZIPCODE = 6;

    /**
     * Reads the whole file and returns every row as a TestData object.
     *
     * @param resource where the file is, for example testdata/credentials.csv
     */
    public static List<TestData> read(String resource) {

        // an empty list to add rows into
        List<TestData> allRows = new ArrayList<TestData>();

        try {
            // find the file on the classpath
            InputStream file = CsvReader.class.getClassLoader()
                    .getResourceAsStream(resource);

            if (file == null) {
                throw new RuntimeException(resource + " is missing from src/test/resources");
            }

            // BufferedReader reads a text file one line at a time
            BufferedReader reader = new BufferedReader(new InputStreamReader(file));

            // the first line is the column names, not real data, so skip it
            reader.readLine();

            // keep reading until readLine() gives back null, which means end of file
            String line = reader.readLine();
            while (line != null) {

                // skip blank lines, otherwise we would try to read a row that is not there
                if (line.trim().length() > 0) {

                    // split the line on every comma. -1 keeps empty columns at the end,
                    // so a short row does not lose its place
                    String[] parts = line.split(",", -1);

                    // make one object and fill it in
                    TestData row = new TestData();
                    row.username = getValue(parts, USERNAME);
                    row.password = getValue(parts, PASSWORD);
                    row.error = getValue(parts, ERROR);
                    row.firstname = getValue(parts, FIRSTNAME);
                    row.lastname = getValue(parts, LASTNAME);
                    row.zipcode = getValue(parts, ZIPCODE);

                    allRows.add(row);
                }

                line = reader.readLine();
            }

            reader.close();
            file.close();

        } catch (IOException e) {
            throw new RuntimeException("Could not read " + resource, e);
        }

        return allRows;
    }

    /**
     * Grabs one column out of a row.
     *
     * A row can be shorter than we expect, because the last columns may be empty and a
     * csv does not always keep the trailing commas. If the column is not there we return
     * an empty string instead of crashing.
     */
    private static String getValue(String[] parts, int column) {
        if (parts.length > column) {
            return parts[column].trim();
        }
        return "";
    }
}
