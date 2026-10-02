Setup

1. Install JDK 8 and Maven.
2. Import project to any IDE (Eclipse or VS Code).
3. In terminal run: mvn clean test  (builds and runs all 3 tests)
   or: mvn clean compile  (build only, no tests)

Running the tests

All 3 test cases:

   mvn clean test

Note the order: clean first, test second. "mvn test clean" runs the tests
and then deletes the results, so it is never what you want.

Just 1 test case, use -Dtest=LoginTest#methodName:

   mvn test -Dtest=LoginTest#testValidLogin
   mvn test -Dtest=LoginTest#testInvalidLoginError
   mvn test -Dtest=LoginTest#testAddToCartAndCheckout

You can also run one test from the IDE: open
src/test/java/com/framework/tests/LoginTest.java and run the single method.

What the framework is, in plain words
A framework is just other classes you wrote, that your tests use instead of
writing the same setup again and again. There is no magic and no special tool.

Think of it as three layers:

   the TESTS      LoginTest        what we are checking
   the PAGES      LoginPage,       how to drive one screen
                  CartPage,
                  CheckoutPage
   the SUPPORT    DriverFactory,   browser, config file, reading the csv,
                  BasePage,        waiting, reporting
                  ConfigReader,    <-- reuse these in your own project
                  Report,          This is the part worth copying.
                  ScreenshotStore,
                  ExtentManager

A test never touches the browser directly. It asks a page object to do
something, and the page objects use the support classes. That is the whole
idea, and it is why you can swap Selenium out one day and only have to change
one layer.

Two things to understand properly
1. INHERITANCE
   "class LoginTest extends BaseTest" means LoginTest gets everything in
   BaseTest for free: the open browser, the csv data, the close browser.
   BaseTest also uses @BeforeClass / @BeforeMethod / @AfterClass, so those
   methods run automatically. LoginTest therefore contains only the actual
   checking, with no setup code at all.

   The same trick is used inside the pages: CartPage extends BasePage, so it
   gets type(), click() and waitFor() for free.

2. CONFIGURATION
   config.properties holds the things you want to change without touching
   Java code:

      browser=chrome
      url=https://www.saucedemo.com
      headless=false

   ConfigReader reads that file once and hands the values out. Nothing else in
   the project opens a properties file.

   The other configuration file is testng.xml. It says which test classes to
   run and registers TestListener. Surefire is told to read it in pom.xml.

Project structure

src/main/java/com/framework/     the reusable part, no TestNG in here
   core/DriverFactory    starts and stops the browser
   pages/BasePage        shared waiting and typing, inherited by the pages
   pages/LoginPage       the login form and the error box
   pages/CartPage        the product grid and the cart
   pages/CheckoutPage    the checkout steps and the confirmation screen
   utils/ConfigReader    reads config.properties
   utils/Report          the one place a step gets logged
   utils/ExtentManager   builds the Extent HTML report
   utils/ScreenshotStore takes the pictures and writes the Word documents

src/test/java/com/framework/     the part that only makes sense with tests
   core/BaseTest         opens the browser, reads the csv, inherited by the tests
   core/TestListener     TestNG calls this, it builds the reports
   models/TestData       one row of the csv
   utils/CsvReader       reads the csv into TestData objects
   tests/LoginTest       the 3 test cases

src/test/resources/testng.xml          which tests to run
src/test/resources/testdata/           credentials.csv

Why is some of it in src/main/java and some in src/test/java?
The rule is: src/main/java holds code another project could reuse as it is.
src/test/java holds code that only makes sense next to a test.

CsvReader is in src/test/java because it mentions TestData, and TestData is
test data. Java does not allow a class in src/main/java to import a class in
src/test/java, so as soon as a class names TestData it has to be test code.

You can check this yourself: run mvn package -DskipTests, then open
target/com.saucedemo-0.0.1-SNAPSHOT.jar. It contains DriverFactory, the page
objects, ConfigReader, Report, ExtentManager and ScreenshotStore. No BaseTest,
no TestListener, no TestData. That is what "the production jar" looks like.

Which class does what
Report is the only class the page objects talk to. A call to
Report.info("added to cart") does three things at once: writes a line to
automation.log, adds a line to the Extent report, and takes a screenshot for
that step. The page objects never mention Extent or Log4j, so the reporting
can be replaced later without touching a single page.

TestListener is registered in testng.xml, not as a parent of the test classes.
TestNG builds a separate instance of a listener, so it cannot see the browser
that BaseTest created, and asks for it with DriverFactory.get() instead.

Each test starts by going back to the login page. The browser is opened once
for the whole class, so without that the second test would still be on the page
the first test finished on. It is also why the tests can run in any order.

Note: Chrome and Edge raise a "password found in a data breach, change it"
bubble after a login. That bubble is browser UI, not part of the page, so
Selenium cannot click it, and it sits over the header and swallows clicks meant
for the cart link. DriverFactory turns that warning off so it never appears.

Tool used
Selenium WebDriver, Java, TestNG, Maven, Page Object Model, CSV test data,
ExtentReports, Log4j, Apache POI (the .docx writer)

Reports
All 3 land in reports/ at the project root, next to each other:

   reports/testng/index.html    -> TestNG, pass/fail per test
   reports/extent/index.html    -> Extent, every step + a screenshot on each one
   reports/logs/automation.log  -> Log4j, what the browser did

Screenshots and Word documents
One screenshot is taken at every step and embedded into that test's Word
document. The only files written are the 3 documents, in screenshots/ at the
project root, outside reports/:

   screenshots/TC01_Valid_login_lands_on_the_home_page.docx       3 pictures
   screenshots/TC02_Invalid_login_shows_the_error_message.docx    3 pictures
   screenshots/TC03_Login__add_to_cart_and_place_the_order.docx  11 pictures

Each document is named TCnn_test description, numbered in the order the tests
run and restarting at TC01 every run. The same number is the first line inside
the document, so a page pulled out of a file still says which case it belongs
to.

Note the sizes. The Extent report embeds every screenshot too, so it is about
2.7 MB for 3 tests. Running headless shrinks each picture. The run also takes
about 30 seconds longer, because a screenshot is a round trip to the browser.

Open reports/extent/index.html to read a run. It is one self-contained file, so
it can be emailed or attached to a ticket and still show the screenshots.

The TestNG report cannot show screenshots at all. Its HTML reporter has no
image attachment feature, so read pictures from the Extent report or the Word
documents.

The TestNG report needs its 9 side files (css/js/icons) to render, so open it
from inside reports/testng/ rather than copying index.html out on its own. It
also opens with the test list collapsed: click "(show)" under "Passed methods",
then click a test name to load its details.

reports/ is created by the test run and is in .gitignore, along with target/.

Test cases
1. testValidLogin          -> valid user lands on inventory page
2. testInvalidLoginError   -> invalid user gets the error message text
3. testAddToCartAndCheckout -> login, add product to cart, checkout, place order

Test data
src/test/resources/testdata/credentials.csv
columns: username,password,valid,error,firstname,lastname,zipcode
row 1 = valid user (standard_user), row 2 = invalid user (invalid_user)
checkout info: firstname prajwal, lastname hegde, zipcode 576227

Passwords live in that csv and nowhere else. They are never written to
automation.log or to either report, because a report gets emailed and pasted
into tickets.
