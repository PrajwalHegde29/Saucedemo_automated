# 🧪 SauceDemo Automation Framework

A Selenium WebDriver test automation framework built with **Java, TestNG and Maven**, using the **Page Object Model**, CSV-driven test data, and rich reporting (TestNG, Extent, Log4j, and step-by-step screenshots in Word documents).

![Java](https://img.shields.io/badge/Java-8-orange)
![Selenium](https://img.shields.io/badge/Selenium-WebDriver-43B02A)
![TestNG](https://img.shields.io/badge/TestNG-Framework-red)
![Maven](https://img.shields.io/badge/Maven-Build-C71A36)

---

## 📑 Table of Contents

- [Tech Stack](#-tech-stack)
- [Quick Start](#-quick-start)
- [Running the Tests](#-running-the-tests)
- [Test Cases](#-test-cases)
- [How the Framework Works](#-how-the-framework-works)
- [Project Structure](#-project-structure)
- [Configuration](#-configuration)
- [Test Data](#-test-data)
- [Reports and Screenshots](#-reports-and-screenshots)

---

## 🛠 Tech Stack

| Area | Tool |
|---|---|
| Browser automation | Selenium WebDriver |
| Language | Java |
| Test runner | TestNG |
| Build tool | Maven |
| Design pattern | Page Object Model |
| Test data | CSV |
| Reporting | ExtentReports, TestNG HTML, Log4j |
| Word documents | Apache POI (.docx writer) |

---

## 🚀 Quick Start

1. Install **JDK 21** and **Maven**.
2. Import the project into any IDE (Eclipse or VS Code).
3. Run in the terminal:

```bash
mvn clean test      # build and run all 3 tests
mvn clean compile   # build only, no tests
```

---

## ▶️ Running the Tests

**Run all test cases**

```bash
mvn clean test
```

> ⚠️ **Order matters:** `clean` first, `test` second. `mvn test clean` runs the tests and then deletes the results.

**Run a single test case**

```bash
mvn test -Dtest=LoginTest#testValidLogin
mvn test -Dtest=LoginTest#testInvalidLoginError
mvn test -Dtest=LoginTest#testAddToCartAndCheckout
```

You can also run one test from the IDE: open `src/test/java/com/framework/tests/LoginTest.java` and run a single method.

---

## ✅ Test Cases

| # | Test method | What it verifies |
|---|---|---|
| 1 | `testValidLogin` | A valid user lands on the inventory page |
| 2 | `testInvalidLoginError` | An invalid user sees the correct error message |
| 3 | `testAddToCartAndCheckout` | Login, add a product to the cart, checkout, and place the order |

---

## 🧩 How the Framework Works

A framework is simply a set of classes your tests reuse instead of repeating the same setup. There is no magic and no special tool.

It has three layers:

| Layer | Classes | Responsibility |
|---|---|---|
| **Tests** | `LoginTest` | What we are checking |
| **Pages** | `LoginPage`, `CartPage`, `CheckoutPage` | How to drive one screen |
| **Support** | `DriverFactory`, `BasePage`, `ConfigReader`, `Report`, `ScreenshotStore`, `ExtentManager` | Browser, config, waiting, reporting. This is the part worth reusing in your own projects. |

A test never touches the browser directly. It asks a page object to do something, and the page objects use the support classes. This is why you could swap Selenium out one day and only change one layer.

### 1. Inheritance

- `LoginTest extends BaseTest` gets the open browser, the CSV data and the browser teardown for free. `BaseTest` uses `@BeforeClass`, `@BeforeMethod` and `@AfterClass`, so those run automatically. `LoginTest` contains only the actual checks, with no setup code.
- The same idea is used in the pages: `CartPage extends BasePage` and gets `type()`, `click()` and `waitFor()` for free.

### 2. Configuration

`config.properties` holds values you can change without touching Java code:

```properties
browser=chrome
url=https://www.saucedemo.com
headless=false
```

`ConfigReader` reads this file once and hands the values out. Nothing else in the project opens a properties file.

The other configuration file is `testng.xml`. It defines which test classes to run and registers `TestListener`. Surefire is pointed to it in `pom.xml`.

### Key design notes

- **`Report` is the only class page objects talk to.** One call such as `Report.info("added to cart")` writes a line to `automation.log`, adds a step to the Extent report, and takes a screenshot. Page objects never mention Extent or Log4j, so reporting can be replaced without touching any page.
- **`TestListener` is registered in `testng.xml`**, not as a parent of the test classes. TestNG creates a separate listener instance that cannot see the browser `BaseTest` opened, so it fetches it with `DriverFactory.get()`.
- **Every test starts from the login page.** The browser opens once per class, so without this the second test would begin wherever the first one ended. It also means tests can run in any order.
- **Browser password-breach popup is disabled.** Chrome and Edge show a "password found in a data breach" bubble after login. It is browser UI, so Selenium cannot click it, and it can block clicks on the cart link. `DriverFactory` turns this warning off.

---

## 📁 Project Structure

```
src/
├── main/java/com/framework/            ← reusable part (no TestNG here)
│   ├── core/
│   │   └── DriverFactory               starts and stops the browser
│   ├── pages/
│   │   ├── BasePage                    shared waiting and typing
│   │   ├── LoginPage                   login form and error box
│   │   ├── CartPage                    product grid and cart
│   │   └── CheckoutPage                checkout steps and confirmation
│   └── utils/
│       ├── ConfigReader                reads config.properties
│       ├── Report                      single place where a step is logged
│       ├── ExtentManager               builds the Extent HTML report
│       └── ScreenshotStore             takes screenshots, writes Word docs
│
└── test/
    ├── java/com/framework/             ← only makes sense with tests
    │   ├── core/
    │   │   ├── BaseTest                opens browser, reads CSV
    │   │   └── TestListener            builds the reports
    │   ├── models/
    │   │   └── TestData                one row of the CSV
    │   ├── utils/
    │   │   └── CsvReader               reads CSV into TestData objects
    │   └── tests/
    │       └── LoginTest               the 3 test cases
    └── resources/
        ├── testng.xml                  which tests to run
        └── testdata/
            └── credentials.csv         test data
```

### Why split `src/main/java` and `src/test/java`?

- `src/main/java` holds code another project could reuse as-is.
- `src/test/java` holds code that only makes sense next to a test.

`CsvReader` lives in `src/test/java` because it references `TestData`, which is test data. Java does not allow `src/main/java` to import from `src/test/java`, so any class that names `TestData` must be test code.

**Verify it yourself:**

```bash
mvn package -DskipTests
```

Then open `target/com.saucedemo-0.0.1-SNAPSHOT.jar`. It contains `DriverFactory`, the page objects, `ConfigReader`, `Report`, `ExtentManager` and `ScreenshotStore`, but no `BaseTest`, `TestListener` or `TestData`.

---

## ⚙️ Configuration

| File | Purpose |
|---|---|
| `config.properties` | Browser, URL, headless mode |
| `testng.xml` | Test classes to run, listener registration |
| `pom.xml` | Dependencies, Surefire pointing to `testng.xml` |

---

## 📊 Test Data

**File:** `src/test/resources/testdata/credentials.csv`

**Columns:** `username,password,valid,error,firstname,lastname,zipcode`

| Row | Purpose |
|---|---|
| 1 | Valid user (`standard_user`) |
| 2 | Invalid user (`invalid_user`) |

Checkout info used: first name `prajwal`, last name `hegde`, zip code `576227`.

> 🔒 Passwords live in the CSV and nowhere else. They are never written to `automation.log` or to any report, because reports get emailed and pasted into tickets.

---

## 📈 Reports and Screenshots

All reports are generated in `reports/` at the project root after each run:

| Report | Path | Contents |
|---|---|---|
| TestNG | `reports/testng/index.html` | Pass/fail per test |
| Extent | `reports/extent/index.html` | Every step with a screenshot |
| Log4j | `reports/logs/automation.log` | What the browser did |

### 📄 Word documents with screenshots

One screenshot is taken at every step and embedded in that test's Word document. These are saved in `screenshots/` at the project root (outside `reports/`):

| File | Screenshots |
|---|---|
| `TC01_Valid_login_lands_on_the_home_page.docx` | 3 |
| `TC02_Invalid_login_shows_the_error_message.docx` | 3 |
| `TC03_Login__add_to_cart_and_place_the_order.docx` | 11 |

Files are named `TCnn_test description`, numbered in run order, and restart at `TC01` every run. The same number appears as the first line inside each document, so a page pulled out of the file still shows which test case it belongs to.

### 💡 Good to know

- **Extent report** is a single self-contained file, so it can be emailed or attached to a ticket and still shows all screenshots. It is about **2.7 MB** for 3 tests because every screenshot is embedded.
- **Headless mode** shrinks each picture.
- **Run time** increases by about 30 seconds with screenshots, since each one is a round trip to the browser.
- **TestNG report** cannot show screenshots (its HTML reporter has no image attachment feature). Use the Extent report or the Word documents for pictures.
- **TestNG report** needs its 9 side files (CSS, JS, icons) to render, so open it from inside `reports/testng/` rather than copying `index.html` out alone. It opens with the test list collapsed: click **(show)** under *Passed methods*, then click a test name to load its details.
- `reports/` and `target/` are generated by runs and listed in `.gitignore`.
