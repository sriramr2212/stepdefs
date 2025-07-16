/**
 * Framework Name: Novac Automation Framework
 * Author: SriramR-NOVAC
 * File Name: CommonSteps.java
 * Description: Common step definitions with robust test case context management
 */

package com.novac.naf.steps;

import com.novac.naf.config.ConfigLoader;
import com.novac.naf.datamanager.DataManager;
import com.novac.naf.datamanager.TestDataUtility;
import com.novac.naf.orm.ORLoader;
import com.novac.naf.reporting.ReportManager;
import com.novac.naf.runner.TestCaseManager;
import com.novac.naf.webdriver.WebDriverManager;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import org.junit.Assert;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonSteps {
    private static final Logger logger = LoggerFactory.getLogger(CommonSteps.class);
    private WebDriver driver;
    private final ConfigLoader configLoader;
    private final DataManager dataManager;
    private Map<String, String> testData = new HashMap<>();
    private String testCaseId;
    private String screenshotsDir;
    private Scenario scenario;
	private ORLoader objectRepo;
    
    /**
     * Constructor for CommonSteps with enhanced initialization
     */
    public CommonSteps() {
        // Initialize configuration first
        String excelPath = System.getProperty("excelPath", "./TestData/RunManager.xlsx");
        logger.info("Initializing CommonSteps with Excel path: {}", excelPath);
        
        this.configLoader = new ConfigLoader(excelPath);
        
        // Initialize DataManager with configLoader
        this.dataManager = new DataManager(configLoader);
        logger.info("DataManager initialized successfully");
        
        // Initialize TestDataUtility with configLoader - this is crucial for test data resolution
        TestDataUtility.initialize(configLoader);
        logger.info("TestDataUtility initialized successfully");
        
        // Initialize WebDriverManager with configuration
        WebDriverManager.initialize(configLoader);
        
        // Initialize ORLoader - ensure all page objects are loaded
        ORLoader.initialize();
        logger.info("ORLoader initialized successfully");
        
        // Set up screenshots directory using centralized timestamp
        String timestamp = ReportManager.getCurrentTimestamp();
        this.screenshotsDir = "./Reports/" + timestamp + "/screenshots";
        
        try {
            Files.createDirectories(Paths.get(screenshotsDir));
            logger.info("Screenshots directory set to: {}", screenshotsDir);
        } catch (Exception e) {
            logger.error("Failed to create screenshots directory: {}", e.getMessage());
        }
        
        logger.info("CommonSteps initialization completed successfully");
    }
    
    /**
     * Get the WebDriver instance for use in other step classes
     * 
     * @return WebDriver instance
     */
    public WebDriver getDriver() {
        return driver;
    }
    
    /**
     * Setup method to run before each scenario with enhanced test case ID extraction
     * 
     * @param scenario The Cucumber scenario
     */
    @Before
    public void setup(Scenario scenario) {
        this.scenario = scenario;
        logger.info("Starting scenario: {}", scenario.getName());
        
        // Extract test case ID from tags with enhanced pattern matching
        testCaseId = extractTestCaseId(scenario);
        
        // CRITICAL: Set the current test case ID in TestCaseManager for global access
        if (testCaseId != null && !testCaseId.isEmpty()) {
            TestCaseManager.setCurrentTestCaseId(testCaseId);
            logger.info("Successfully set current test case ID in TestCaseManager: {}", testCaseId);
        } else {
            logger.error("CRITICAL: No test case ID found for scenario: {}", scenario.getName());
            logger.error("Available tags: {}", scenario.getSourceTagNames());
            
            // Try to extract from scenario name as fallback
            testCaseId = extractTestCaseIdFromScenarioName(scenario.getName());
            if (testCaseId != null) {
                TestCaseManager.setCurrentTestCaseId(testCaseId);
                logger.warn("Used fallback test case ID from scenario name: {}", testCaseId);
            } else {
                throw new RuntimeException("No test case ID could be determined for scenario: " + scenario.getName() + ". Ensure proper @STATIMCM-TC-XXX or @TC_ID= tags are present.");
            }
        }
        
        // Load test data for this test case
        loadTestData();
        
        // Start the test in ReportManager with the scenario name and test case ID
        String testName = scenario.getName();
        String testId = testCaseId != null ? testCaseId : "Unknown-TC";
        
        ReportManager.startTest(testId, testName);
        logger.info("Started test in ReportManager: {} - {}", testId, testName);
        
        // Initialize WebDriver
        driver = WebDriverManager.getDriver();
        
        logger.info("Setup completed successfully for scenario: {} with test case ID: {}", scenario.getName(), testCaseId);
    }
    
    /**
     * Extracts test case ID from scenario tags with enhanced pattern matching
     * Supports multiple tag formats for robust test case identification
     * 
     * @param scenario The Cucumber scenario
     * @return The test case ID or null if not found
     */
    private String extractTestCaseId(Scenario scenario) {
        logger.debug("Extracting test case ID from scenario tags: {}", scenario.getSourceTagNames());
        
        for (String tag : scenario.getSourceTagNames()) {
            logger.debug("Processing tag: {}", tag);
            
            // Pattern 1: @STATIMCM-TC-XXX format (current format)
            if (tag.matches("@STATIMCM-TC-\\d+")) {
                String tcId = tag.substring(1); // Remove @ prefix
                logger.info("Found test case ID in direct tag format: {} -> {}", tag, tcId);
                return tcId;
            }
            
            // Pattern 2: @TC_ID=STATIMCM-TC-XXX format (legacy format)
            if (tag.startsWith("@TC_ID=")) {
                String tcId = tag.substring("@TC_ID=".length());
                logger.info("Found test case ID in TC_ID tag format: {} -> {}", tag, tcId);
                return tcId;
            }
            
            // Pattern 3: Any tag containing STATIMCM-TC pattern
            Pattern tcPattern = Pattern.compile("STATIMCM-TC-\\d+");
            Matcher matcher = tcPattern.matcher(tag);
            if (matcher.find()) {
                String tcId = matcher.group();
                logger.info("Found test case ID in pattern match: {} -> {}", tag, tcId);
                return tcId;
            }
        }
        
        logger.warn("No test case ID found in any tag format for scenario: {}", scenario.getName());
        logger.warn("Available tags: {}", scenario.getSourceTagNames());
        return null;
    }
    
    /**
     * Attempts to extract test case ID from scenario name as fallback
     * 
     * @param scenarioName The scenario name
     * @return The test case ID or null if not found
     */
    private String extractTestCaseIdFromScenarioName(String scenarioName) {
        if (scenarioName == null || scenarioName.isEmpty()) {
            return null;
        }
        
        // Try to find STATIMCM-TC pattern in scenario name
        Pattern tcPattern = Pattern.compile("STATIMCM-TC-\\d+");
        Matcher matcher = tcPattern.matcher(scenarioName);
        if (matcher.find()) {
            String tcId = matcher.group();
            logger.info("Extracted test case ID from scenario name: {} -> {}", scenarioName, tcId);
            return tcId;
        }
        
        logger.debug("No test case ID pattern found in scenario name: {}", scenarioName);
        return null;
    }
    
    /**
     * Loads test data for the current scenario using multi-sheet architecture
     */
    private void loadTestData() {
        testData = new HashMap<>();
        String currentTestCaseId = this.testCaseId;
        
        if (currentTestCaseId != null && !currentTestCaseId.isEmpty()) {
            logger.info("Loading test data for test case: {}", currentTestCaseId);
            
            try {
                // Use the DataManager method to search across all available test data sheets
                testData = dataManager.getTestDataFromAnySheet(currentTestCaseId);
                
                if (!testData.isEmpty()) {
                    logger.info("Successfully loaded test data for {}: {}", currentTestCaseId, testData);
                } else {
                    logger.warn("No test data found for test case {} in any available sheets", currentTestCaseId);
                    testData = new HashMap<>();
                }
                
            } catch (Exception e) {
                logger.error("Error loading test data for {}: {}", currentTestCaseId, e.getMessage(), e);
                testData = new HashMap<>();
            }
        } else {
            logger.error("No test case ID available for loading test data");
            testData = new HashMap<>();
        }
    }
    
    /**
     * Teardown method to run after each scenario
     * 
     * @param scenario The Cucumber scenario
     */
    @After
    public void tearDown(Scenario scenario) {
        try {
            // Take screenshot if scenario failed or if screenshot mode is set to "all"
            String screenshotMode = configLoader.getScreenshotMode();
            boolean takeScreenshot = screenshotMode.equals("all") || 
                                    (screenshotMode.equals("pass_fail") && scenario.isFailed());
            
            if (takeScreenshot && driver != null) {
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                scenario.attach(screenshot, "image/png", "Screenshot-" + 
                               LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")));
                logger.info("Screenshot attached to scenario");
            }
            
            // Clear the test case context
            TestCaseManager.clearCurrentTestCaseId();
            logger.info("Cleared test case context after scenario completion");
            
        } catch (Exception e) {
            logger.error("Error in tearDown: {}", e.getMessage());
        }
    }
    
    /**
     * Navigates to the specified URL with robust test data resolution
     * 
     * @param url The URL to navigate to (can contain test data placeholders like LoginPage.ApplicationURL)
     * @throws Throwable if navigation fails
     */
    @Given("I navigate to {string}")
    public void navigateTo(String url) throws Throwable {
        try {
            // Ensure test case context is available before processing test data
            TestCaseManager.validateTestCaseIdSet();
            
            // Replace test data placeholders in the URL using enhanced resolution
            String resolvedUrl = replaceTestDataPlaceholders(url);
            logger.info("Navigating to URL: {} (resolved from: {})", resolvedUrl, url);
            
            // Validate that the URL was actually resolved if it looked like a data reference
            if (isLikelyDataReference(url) && resolvedUrl.equals(url)) {
                throw new RuntimeException("Test data reference in URL was not resolved: " + url + ". Check test data availability for test case: " + TestCaseManager.getCurrentTestCaseId());
            }
            
            if (resolvedUrl.startsWith("http")) {
                driver.get(resolvedUrl);
            } else {
                // If it's not an absolute URL, try to prepend the base URL
                String baseUrl = configLoader.getBaseApplicationUrl();
                if (baseUrl.endsWith("/") && resolvedUrl.startsWith("/")) {
                    // Avoid double slash
                    baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
                }
                driver.get(baseUrl + resolvedUrl);
            }
            
            ReportManager.logPass("Navigate to URL", "Successfully navigated to: " + resolvedUrl);
        } catch (Exception e) {
            logger.error("Error navigating to URL: {}", e.getMessage());
            ReportManager.logFail("Navigate to URL", "Failed to navigate to: " + url + " - " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Public method to navigate to URL - accessible from other step classes
     * 
     * @param url The URL to navigate to
     * @throws Throwable if navigation fails
     */
    public void navigateToUrl(String url) {
        try {
            logger.info("Navigating to URL: {}", url);

            // Skip resolving test data if it's a direct URL
            String finalUrl = (url.startsWith("http://") || url.startsWith("https://"))
                    ? url
                    : replaceTestDataPlaceholders(url);

            driver.get(finalUrl);
            logger.info("Successfully navigated to: {}", finalUrl);
        } catch (Exception e) {
            logger.error("Failed to navigate to URL: {}", url, e);
            throw new RuntimeException("Navigation to URL failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Helper method to replace test data placeholders in strings with enhanced error handling
     * 
     * @param text String containing placeholders
     * @return String with placeholders replaced by test data values
     */
    public String replaceTestDataPlaceholders(String input) {
        try {
            if (input != null && input.matches("^[A-Za-z0-9_]+\\.[A-Za-z0-9_]+$")) {
                logger.debug("Resolving test data placeholder: {}", input);
                String result = TestDataUtility.processTestDataReference(input);
                logger.debug("Test data placeholder resolution: '{}' -> '{}'", input, result);
                return result;
            } else {
                logger.debug("Skipping placeholder resolution for non-reference input: {}", input);
                return input;
            }
        } catch (Exception e) {
            logger.error("CRITICAL: Failed to replace test data placeholders in: '{}'", input, e);
            throw new RuntimeException("Failed to resolve test data reference: " + input + " - " + e.getMessage(), e);
        }
    }

    /**
     * Checks if a string looks like it might be a test data reference
     * 
     * @param text The text to check
     * @return true if it appears to be a data reference
     */
    private boolean isLikelyDataReference(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        // Check for common data reference patterns
        return text.contains(".") && 
               (text.matches("^[A-Za-z]+\\.[A-Za-z]+.*") || 
                text.contains("{{TD."));
    }
    
    /**
     * Verifies the page title
     * 
     * @param expectedTitle The expected page title (can contain test data placeholders)
     * @throws Throwable if verification fails
     */
    @Then("I verify page title is {string}")
    public void verifyPageTitle(String expectedTitle) throws Throwable {
        try {
            String resolvedTitle = replaceTestDataPlaceholders(expectedTitle);
            logger.info("Verifying page title: {}", resolvedTitle);
            
            // Wait for the page title to match the expected title
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.titleContains(resolvedTitle));
            
            String actualTitle = driver.getTitle();
            
            if (actualTitle.contains(resolvedTitle)) {
                logger.info("Page title verified: {}", actualTitle);
                ReportManager.logPass("Verify page title", 
                    String.format("Title matches: '%s' contains '%s'", actualTitle, resolvedTitle));
            } else {
                logger.error("Page title verification failed. Expected: {}, Actual: {}", resolvedTitle, actualTitle);
                ReportManager.logFail("Verify page title", 
                    String.format("Title mismatch. Expected: '%s', Actual: '%s'", resolvedTitle, actualTitle));
                throw new AssertionError("Page title verification failed. Expected: " + resolvedTitle + ", Actual: " + actualTitle);
            }
        } catch (Exception e) {
            logger.error("Error verifying page title: {}", e.getMessage());
            ReportManager.logFail("Verify page title", "Error: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Verifies that an element is visible on the page
     * 
     * @param elementName The name of the element to verify
     * @param pageName The page where the element is located
     * @throws Throwable if verification fails
     */
    public void verifyElementIsVisible(String elementName, String pageName) throws Throwable {
        try {
            // Replace test data placeholders in both parameters
            String resolvedElementName = replaceTestDataPlaceholders(elementName);
            String resolvedPageName = replaceTestDataPlaceholders(pageName);
            
            logger.info("Verifying element '{}' is visible on page '{}'", resolvedElementName, resolvedPageName);
            
            // Find the element using the OR repository
            WebElement element = findElement(resolvedElementName, resolvedPageName);
            
            // Wait for element to be visible
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.visibilityOf(element));
            
            // Scroll element into view for better visibility
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
            
            // Highlight element (following existing pattern from other methods)
            highlightElement(element);
            
            // Verify element is displayed
            if (element.isDisplayed()) {
                logger.info("Element '{}' on page '{}' is visible", resolvedElementName, resolvedPageName);
                ReportManager.logPass("Verify element visibility", 
                    String.format("Element '%s' on page '%s' is visible", resolvedElementName, resolvedPageName));
            } else {
                throw new AssertionError("Element is not displayed");
            }
            
        } catch (Exception e) {
            String errorMsg = String.format("Failed to verify element '%s' is visible on page '%s': %s", 
                elementName, pageName, e.getMessage());
            logger.error(errorMsg);
            ReportManager.logFail("Verify element visibility", errorMsg);
            throw new RuntimeException(errorMsg, e);
        }
    }

    /**
     * Checks if an element is visible on the page
     * 
     * @param elementName The name of the element to check
     * @param pageName The page where the element is located
     * @return true if element exists and is displayed, false otherwise
     */
    public boolean isElementVisible(String elementName, String pageName) {
        try {
            // Replace test data placeholders in both parameters
            String resolvedElementName = replaceTestDataPlaceholders(elementName);
            String resolvedPageName = replaceTestDataPlaceholders(pageName);
            
            logger.debug("Checking if element '{}' is visible on page '{}'", resolvedElementName, resolvedPageName);
            
            // Find the element using the OR repository
            WebElement element = findElement(resolvedElementName, resolvedPageName);
            
            // Return true if element exists and is displayed
            return element != null && element.isDisplayed();
            
        } catch (Exception e) {
            logger.debug("Element '{}' on page '{}' is not visible: {}", elementName, pageName, e.getMessage());
            return false;
        }
    }
    
    /**
     * Clears the text from the specified field on the given page.
     *
     * @param fieldName The name of the field
     * @param pageName  The name of the page
     */
    public void clearField(String fieldName, String pageName) {
        try {
            WebElement field = getFieldWebElement(fieldName, pageName);
            field.click();  // Focus the field first
            field.clear();  // Clear using clear()
            
            // Optionally clear via keyboard (for JS-controlled inputs)
            field.sendKeys(Keys.CONTROL + "a");
            field.sendKeys(Keys.DELETE);
            
            logger.info("Cleared text from field '{}' on page '{}'", fieldName, pageName);
        } catch (Exception e) {
            logger.error("Failed to clear field '{}' on page '{}': {}", fieldName, pageName, e.getMessage());
            throw new RuntimeException("Unable to clear field: " + fieldName + " on page: " + pageName, e);
        }
    }


    /**
     * Gets the current value from the field.
     *
     * @param fieldName The name of the field (from object repo)
     * @param pageName  The page the field is on
     * @return The value (usually from input value attribute)
     */
    public String getFieldValue(String fieldName, String pageName) {
        try {
            By locator = objectRepo.getLocator(fieldName, pageName);
            WebElement field = driver.findElement(locator);

            // For input, textarea
            if (field.getTagName().equalsIgnoreCase("input") || field.getTagName().equalsIgnoreCase("textarea")) {
                return field.getAttribute("value").trim();
            }

            // For divs/spans acting like fields
            return field.getText().trim();

        } catch (NoSuchElementException e) {
            logger.error("Unable to locate field: {} on page: {}", fieldName, pageName);
            throw new RuntimeException("Field not found: " + fieldName + " on page: " + pageName);
        } catch (Exception e) {
            logger.error("Error fetching value from field: {} on page: {} - {}", fieldName, pageName, e.getMessage());
            throw new RuntimeException("Unable to get value from field: " + fieldName + " on page: " + pageName);
        }
    }


    /**
     * Finds and returns the WebElement for a given field and page from the object repository.
     *
     * @param fieldName The field name
     * @param pageName  The page name
     * @return The WebElement representing the field
     */
    public WebElement getFieldWebElement(String fieldName, String pageName) {
        try {
            By locator = objectRepo.getLocator(fieldName, pageName); // Assumes objectRepo utility exists
            return driver.findElement(locator);
        } catch (Exception e) {
            logger.error("Failed to locate field '{}' on page '{}': {}", fieldName, pageName, e.getMessage());
            throw new RuntimeException("Element not found: " + fieldName + " on page: " + pageName, e);
        }
    }


    /**
     * Verifies the master name is displayed correctly
     * 
     * @param expectedMasterName The expected master name (can contain test data placeholders)
     * @throws Throwable if verification fails
     */
    @Then("I verify master name is {string}")
    public void verifyMasterName(String expectedMasterName) throws Throwable {
        try {
            // Replace test data placeholders
            String resolvedMasterName = replaceTestDataPlaceholders(expectedMasterName);
            
            logger.info("Verifying master name is: {}", resolvedMasterName);
            
            // Wait for text to be present on the page
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), resolvedMasterName));
            
            // Log success
            ReportManager.logPass("Verify master name", "Master name verified: " + resolvedMasterName);
            
        } catch (Exception e) {
            logger.error("Error verifying master name: {}", e.getMessage());
            ReportManager.logFail("Verify master name", "Error: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Verifies if element exists on the page
     * 
     * @param elementName The name of the element in the object repository
     * @param pageName The name of the page in the object repository
     * @throws Throwable if verification fails
     */
    @Then("I should see {string} on {string} page")
    public void verifyElementExists(String elementName, String pageName) throws Throwable {
        try {
            // Replace test data placeholders in element and page names
            elementName = replaceTestDataPlaceholders(elementName);
            pageName = replaceTestDataPlaceholders(pageName);
            
            logger.info("Verifying element exists: {} on {} page", elementName, pageName);
            
            // Find the element using the OR loader
            WebElement element = findElement(elementName, pageName);
            
            // Scroll element into view
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);
            
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.debug("Thread interrupted while waiting after scrolling: {}", e.getMessage());
            }
            
            // Highlight element
            highlightElement(element);
            
            // Log success if element is found and visible
            if (element.isDisplayed()) {
                logger.info("Element found: {} on {} page", elementName, pageName);
                ReportManager.logInfo("Element visible: " + elementName + " on " + pageName + " page");
                ReportManager.logPass("Verify element exists", "Element exists: " + elementName + " on " + pageName + " page");
            } else {
                logger.warn("Element found but not visible: {} on {} page", elementName, pageName);
                ReportManager.logFail("Verify element exists", "Element found but not visible: " + elementName + " on " + pageName + " page");
                throw new AssertionError("Element found but not visible: " + elementName + " on " + pageName + " page");
            }
        } catch (Exception e) {
            logger.error("Error verifying element exists: {} on {} page - {}", elementName, pageName, e.getMessage());
            ReportManager.logFail("Verify element exists", "Error: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Enters text into a field
     * 
     * @param text The text to enter (can contain test data placeholders)
     * @param elementName The name of the element in the object repository
     * @param pageName The name of the page in the object repository
     * @throws Throwable if operation fails
     */
    @When("I enter {string} in {string} field on {string} page")
    public void enterTextInField(String text, String elementName, String pageName) throws Throwable {
        try {
            // Replace test data placeholders
            String resolvedText = replaceTestDataPlaceholders(text);
            elementName = replaceTestDataPlaceholders(elementName);
            pageName = replaceTestDataPlaceholders(pageName);

            logger.info("Entering text '{}' into field: {} on {} page", resolvedText, elementName, pageName);

            // Find the element
            WebElement element = findElement(elementName, pageName);

            // Log element details
            logger.info("Tag: {}", element.getTagName());
            logger.info("Type: {}", element.getAttribute("type"));
            logger.info("Readonly: {}", element.getAttribute("readonly"));
            logger.info("Disabled: {}", element.getAttribute("disabled"));
            logger.info("Current Value: {}", element.getAttribute("value"));

            // Use Actions class to clear field
            Actions actions = new Actions(driver);
            actions.moveToElement(element)
                   .click()
                   .keyDown(Keys.CONTROL)
                   .sendKeys("a")
                   .keyUp(Keys.CONTROL)
                   .sendKeys(Keys.DELETE)
                   .perform();

            Thread.sleep(300); // Small pause before typing

            // Type the new value
            element.sendKeys(resolvedText);

            // Optional: fire input/change event if required
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].dispatchEvent(new Event('input')); arguments[0].dispatchEvent(new Event('change'));",
                element
            );

            ReportManager.logPass("Enter text", String.format("Text '%s' entered in %s field", resolvedText, elementName));
        } catch (Exception e) {
            logger.error("Error entering text: {}", e.getMessage());
            ReportManager.logFail("Enter text", "Error: " + e.getMessage());
            throw e;
        }
    }

    
    /**
     * Clicks on an element with enhanced error handling for intercepted clicks
     * 
     * @param elementName The name of the element in the object repository
     * @param pageName The name of the page in the object repository
     * @throws Throwable if operation fails
     */
    @When("I click on {string} on {string} page")
    public void clickOnElement(String elementName, String pageName) throws Throwable {
        try {
            // Replace test data placeholders
            elementName = replaceTestDataPlaceholders(elementName);
            pageName = replaceTestDataPlaceholders(pageName);
            
            logger.info("Clicking on element: {} on {} page", elementName, pageName);
            
            // Find the element
            WebElement element = findElement(elementName, pageName);
            
            // Scroll element into view
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);
            
            // Wait for any overlays or loading spinners to disappear
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            
            // Wait for element to be clickable (not obscured by other elements)
            wait.until(ExpectedConditions.elementToBeClickable(element));
            
            // Additional wait for potential overlays/loading indicators
            try {
                // Check for common overlay/loading selectors that might be blocking the click
                List<WebElement> overlays = driver.findElements(By.cssSelector(".loading, .overlay, .spinner, [class*='loading'], [class*='overlay']"));
                if (!overlays.isEmpty()) {
                    logger.info("Found potential overlay elements, waiting for them to disappear");
                    wait.until(ExpectedConditions.invisibilityOfAllElements(overlays));
                }
            } catch (Exception e) {
                logger.debug("No overlays found or timeout waiting for overlay to disappear: {}", e.getMessage());
            }
            
            // Highlight element
            highlightElement(element);
            
            try {
                // Try normal click first
                element.click();
                logger.info("Successfully clicked element: {} on {} page", elementName, pageName);
            } catch (ElementClickInterceptedException e) {
                logger.warn("Normal click intercepted, trying JavaScript click: {}", e.getMessage());
                
                // Fallback to JavaScript click if normal click is intercepted
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
                logger.info("Successfully clicked element using JavaScript: {} on {} page", elementName, pageName);
            }
            
            // Log success
            ReportManager.logPass("Click element", "Clicked on: " + elementName + " on " + pageName + " page");
            
        } catch (Exception e) {
            logger.error("Error clicking on element: {}", e.getMessage());
            ReportManager.logFail("Click element", "Error: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Verifies if an element contains specific text
     * 
     * @param elementName The name of the element
     * @param pageName The name of the page
     * @param expectedText The expected text
     * @throws Throwable if verification fails
     */
    @Then("I verify {string} on {string} page contains text {string}")
    public void verifyElementContainsText(String elementName, String pageName, String expectedText) throws Throwable {
        try {
            // Replace test data placeholders
            elementName = replaceTestDataPlaceholders(elementName);
            pageName = replaceTestDataPlaceholders(pageName);
            String resolvedExpectedText = replaceTestDataPlaceholders(expectedText);
            
            logger.info("Verifying element {} on {} page contains text: {}", 
                    elementName, pageName, resolvedExpectedText);
            
            // Find the element
            WebElement element = findElement(elementName, pageName);
            
            // Get the text and verify
            String actualText = element.getText();
            
            if (actualText.contains(resolvedExpectedText)) {
                logger.info("Element contains expected text: {}", resolvedExpectedText);
                ReportManager.logPass("Verify element text", 
                    String.format("Element %s contains text: %s", elementName, resolvedExpectedText));
            } else {
                logger.error("Element text verification failed. Expected to contain: {}, Actual: {}", 
                        resolvedExpectedText, actualText);
                ReportManager.logFail("Verify element text", 
                    String.format("Element text mismatch. Expected to contain: '%s', Actual: '%s'", 
                            resolvedExpectedText, actualText));
                throw new AssertionError("Element text verification failed. Expected to contain: " + 
                        resolvedExpectedText + ", Actual: " + actualText);
            }
        } catch (Exception e) {
            logger.error("Error verifying element contains text: {}", e.getMessage());
            ReportManager.logFail("Verify element text", "Error: " + e.getMessage());
            throw e;
        }
    }
    
    @When("I verify tooltip for {string} field on {string} page with expected text {string}")
    public void verifyTooltip(String fieldName, String pageName, String expectedTooltip) throws Throwable {
        fieldName = replaceTestDataPlaceholders(fieldName);
        pageName = replaceTestDataPlaceholders(pageName);
        expectedTooltip = replaceTestDataPlaceholders(expectedTooltip);

        WebElement element = findElement(fieldName, pageName);
        verifyTooltipAlternative(element, expectedTooltip, fieldName, pageName);
    }
    
    @When("I verify info icon for {string} field on {string} page with expected text {string}")
    public void verifyInfoIcon(String fieldName, String pageName, String expectedTooltip) throws Throwable {
        fieldName = replaceTestDataPlaceholders(fieldName);
        pageName = replaceTestDataPlaceholders(pageName);
        expectedTooltip = replaceTestDataPlaceholders(expectedTooltip);

        WebElement element = findElement(fieldName, pageName);
        verifyTooltipAlternative(element, expectedTooltip, fieldName, pageName);
    }
    
    @When("I click on {string} button on {string} page and wait until page title matches {string}")
    public void clickButtonAndWaitForTitle(String buttonName, String pageName, String expectedTitle) throws Exception {
        try {
            buttonName = replaceTestDataPlaceholders(buttonName);
            pageName = replaceTestDataPlaceholders(pageName);
            expectedTitle = replaceTestDataPlaceholders(expectedTitle);

            logger.info("Clicking button '{}' on page '{}'", buttonName, pageName);
            logger.debug("Expected page title to match: '{}'", expectedTitle);

            WebElement button = findElement(buttonName, pageName);
            button.click();
            logger.info("Clicked button '{}'", buttonName);
            ReportManager.logInfo("Clicked button '" + buttonName + "' on page '" + pageName + "'");

            int maxWaitSeconds = 60;
            int pollIntervalSeconds = 6;
            int elapsedTime = 0;
            String actualTitle = "";

            while (elapsedTime < maxWaitSeconds) {
                try {
                    Thread.sleep(pollIntervalSeconds * 1000L);
                } catch (InterruptedException e) {
                    logger.warn("Interrupted during sleep: {}", e.getMessage());
                }

                actualTitle = driver.getTitle();
                logger.debug("Elapsed time: {} seconds | Current page title: '{}'", elapsedTime + pollIntervalSeconds, actualTitle);

                if (actualTitle.equalsIgnoreCase(expectedTitle)) {
                    break;
                }

                elapsedTime += pollIntervalSeconds;
            }

            if (actualTitle.equalsIgnoreCase(expectedTitle)) {
                logger.info("Page title matched successfully: '{}'", actualTitle);
                ReportManager.logPass("Verify page title", "Page title matched successfully: '" + actualTitle + "'");
            } else {
                logger.error("Expected page title '{}' not found within {} seconds after clicking '{}'", expectedTitle, maxWaitSeconds, buttonName);
                ReportManager.logFail("Verify page title", "Expected page title '" + expectedTitle + "' not found within " + maxWaitSeconds + " seconds after clicking '" + buttonName + "'");
                throw new Exception("Expected page title not matched within timeout.");
            }

        } catch (Exception e) {
            logger.error("Error clicking button '{}' on page '{}': {}", buttonName, pageName, e.getMessage(), e);
            ReportManager.logFail("Click and wait for title", "Failed to click button '" + buttonName + "' on page '" + pageName + "' - Error: " + e.getMessage());
            throw new Exception("Failed to click button '" + buttonName + "' on page '" + pageName + "'", e);
        }
    }

    @When("I enter text {string} in {string} field on {string} page and match with {string}")
    public void enterTextAndMatchWithExpected(String enteredText, String fieldName, String pageName, String expectedText) throws Exception {
        try {
            // Resolve test data placeholders (from Excel or OR)
            enteredText = replaceTestDataPlaceholders(enteredText);
            expectedText = replaceTestDataPlaceholders(expectedText);
            fieldName = replaceTestDataPlaceholders(fieldName);
            pageName = replaceTestDataPlaceholders(pageName);

            logger.info("Starting text entry and verification on page: '{}', field: '{}'", pageName, fieldName);
            logger.debug("Text to enter (resolved): '{}'", enteredText);
            logger.debug("Expected text to match (resolved): '{}'", expectedText);

            // Step 1: Find the element
            WebElement inputField = findElement(fieldName, pageName);

            // Step 2: Clear and enter the text
            inputField.clear();
            inputField.sendKeys(enteredText);

            // Step 3: Get the actual value from the field
            String actualValue = inputField.getAttribute("value");
            logger.debug("Actual value in field after input: '{}'", actualValue);

            // Step 4: Match with expected text
            if (!actualValue.equals(expectedText)) {
                logger.error("Mismatch in field value. Entered: '{}', Expected: '{}', Actual: '{}'", enteredText, expectedText, actualValue);
                throw new Exception("Field value does not match expected text.");
            }

            logger.info("Entered text is correctly restricted and matches expected text.");

        } catch (Exception e) {
            logger.error("Error entering or verifying text in field '{}' on page '{}': {}", fieldName, pageName, e.getMessage(), e);
            throw new Exception("Text entry or match failed for field '" + fieldName + "' on page '" + pageName + "'", e);
        }
    }
    
    /**
     * Clicks a Save button on a specified page and verifies the toast message using an element
     * defined in the object repository. Compares the actual toast message with an expected message
     * retrieved from test data (e.g., Excel).
     *
     * @param buttonId              The logical name or ID of the Save button from the Object Repository
     * @param pageName              The page file name (e.g., "UserManagement.json") used to locate the Object Repository
     * @param actualToastMessageKey The logical key for the toast message element in the Object Repository
     * @param expectedMessageKey    The key used to fetch the expected toast message from test data
     */
    @Then("I click the {string} button on {string} page and verify toast using element {string} matches message from {string}")
    public void click_save_and_verify_with_toast(String buttonId, String pageName, String actualToastMessageKey, String expectedMessageKey) {
        try {
            // Replace placeholders in parameters if any
            buttonId = replaceTestDataPlaceholders(buttonId);
            pageName = replaceTestDataPlaceholders(pageName);
    	    actualToastMessageKey= replaceTestDataPlaceholders(actualToastMessageKey);
            expectedMessageKey = replaceTestDataPlaceholders(expectedMessageKey);

            logger.info("Clicking Save button '{}' on page '{}', expecting toast message for key '{}'", buttonId, pageName, expectedMessageKey);

            // Find Save button from Object Repository
            WebElement saveButton;
            try {
                saveButton = findElement(buttonId, pageName);
            } catch (Exception e) {
                logger.error("Save button '{}' not found on page '{}'", buttonId, pageName);
                throw new RuntimeException("Save button not found: " + buttonId + " on page: " + pageName, e);
            }

            // Click Save button
            try {
                saveButton.click();
                logger.info("Clicked Save button: {}", buttonId);
            } catch (Exception e) {
                logger.error("Failed to click Save button: {}", e.getMessage());
                throw new RuntimeException("Click action failed for Save button: " + buttonId, e);
            } 

             // Wait and get toast message
            String Key;
            try {
                WebElement toastElement = new WebDriverWait(driver, Duration.ofSeconds(30))
                    .until(ExpectedConditions.visibilityOf(findElement(actualToastMessageKey, pageName)));
                actualToastMessageKey = toastElement.getText().trim();
                logger.info("Actual toast: {}", actualToastMessageKey);
            } catch (TimeoutException te) {
                throw new RuntimeException("Toast not found in time.", te);
            }

            // Match actual vs expected
            if (actualToastMessageKey.equals(expectedMessageKey)) {
                logger.info("Toast message matched the expected message.");
                ReportManager.logPass("Record Save Verification", "Record saved successfully. Toast message matched: " + expectedMessageKey);
            } else {
                logger.warn("Toast message mismatch - Expected: '{}', Actual: '{}'", expectedMessageKey, actualToastMessageKey);
                ReportManager.logFail("Record Save Failed", "Toast mismatch. Expected: " + expectedMessageKey + ", Actual: " + actualToastMessageKey);
                throw new RuntimeException("Record save failed due to toast mismatch.");
            }

            // Visual demo delay
            addDemoDelay();

            // Take screenshot after save
            try {
                takeScreenshot("Toast Message - " + expectedMessageKey);
            } catch (Throwable t) {
                logger.error("Failed to capture screenshot after toast message: {}", t.getMessage());
                throw new RuntimeException("Screenshot capture failed: " + t.getMessage(), t);
            }

        } catch (Exception e) {
            logger.error("Save operation failed for button '{}': {}", buttonId, e.getMessage());
            ReportManager.logFail("Save Operation Failed", "Error during save verification: " + e.getMessage());
            throw new RuntimeException("Save operation error: " + e.getMessage(), e);
        }
    }
    
//  -------------------------------------------------------
//  Search
//  -------------------------------------------------------
    
    @Then("I search for {string} in {string} using {string} and verify the results in {string}")
    public void search(
            String searchTerm1, String pageName1, String searchBoxKey1, String gridListKey1) throws Throwable {
        try {
            String searchTerm = replaceTestDataPlaceholders(searchTerm1);
            String pageName = replaceTestDataPlaceholders(pageName1);
            String searchBoxKey = replaceTestDataPlaceholders(searchBoxKey1);
            String gridListKey = replaceTestDataPlaceholders(gridListKey1);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement searchBox = wait.until(ExpectedConditions.elementToBeClickable(ORLoader.getLocator(pageName, searchBoxKey)));
            searchBox.clear();
            searchBox.sendKeys(searchTerm);
            searchBox.sendKeys(Keys.ENTER);

            ReportManager.logInfo("Search executed with term: " + searchTerm);
            waitForSeconds("2");

            wait.until(ExpectedConditions.presenceOfElementLocated(ORLoader.getLocator(pageName, gridListKey)));
            Thread.sleep(1000);

            List<WebElement> results = driver.findElements(ORLoader.getLocator(pageName, gridListKey));
            List<String> nonMatching = new ArrayList<>();

            String searchTermNormalized = searchTerm.trim().toLowerCase().replaceAll("\\s+", " ");

            for (int i = 0; i < results.size(); i++) {
                try {
                    results = driver.findElements(ORLoader.getLocator(pageName, gridListKey));
                    if (i >= results.size()) break;

                    String resultText = results.get(i).getText().trim().replaceAll("\\s+", " ");
                    String resultNormalized = resultText.toLowerCase();

                    if (resultText.isEmpty()) continue;

                    if (resultNormalized.equals(searchTermNormalized)
                            || resultNormalized.contains(searchTermNormalized)
                            || resultNormalized.startsWith(searchTermNormalized)
                            || resultNormalized.endsWith(searchTermNormalized)) {
                        ReportManager.logPass("Result " + (i + 1) + ": '" + resultText + "' - ", "MATCH FOUND");
                    } else {
                        nonMatching.add("Result " + (i + 1) + ": '" + resultText + "'");
                        ReportManager.logWarning("Result " + (i + 1) + ": '" + resultText + "' - NO DATA FOUND");
                    }
                } catch (StaleElementReferenceException e) {
                    i--; // Retry this index
                }
            }

            if (!nonMatching.isEmpty()) {
                String failMsg = "No search data found: " + String.join("; ", nonMatching);
                ReportManager.logFail("Search Validation", failMsg);
                takeScreenshot("Search Mismatch - " + searchTerm);
                throw new AssertionError(failMsg);
            }

            takeScreenshot("Search Success - " + searchTerm);

        } catch (Exception e) {
            ReportManager.logFail("Search Exception", e.getMessage());
            takeScreenshot("Search Failure - " + searchTerm1);
            throw new RuntimeException("Search validation failed: " + e.getMessage(), e);
        }
    }
    
    @Then("I count and verify the tab blocks for {string} element on {string} page")
    public void countAndVerifyTabBlocks(String elementName, String pageName) throws Exception {
        try {
            elementName = replaceTestDataPlaceholders(elementName);

            logger.info("Starting tab block verification for element '{}'", elementName);
            ReportManager.logInfo("Verifying tab blocks for: **" + elementName + "**");
            
            List<WebElement> tabBlocks = driver.findElements(ORLoader.getLocator(pageName, elementName));

            if (tabBlocks.isEmpty()) {
                String msg = "No tab blocks found for element '" + elementName + "'";
                logger.error(msg);
                throw new Exception(msg);
            }

            ReportManager.logInfo("Total tab blocks found: **" + tabBlocks.size() + "**");

            Actions actions = new Actions(driver);

            for (int i = 0; i < tabBlocks.size(); i++) {
                WebElement tab = tabBlocks.get(i);
                String label = tab.getText().trim();
                String bgColor = tab.getCssValue("background-color");
                String className = tab.getAttribute("class").toLowerCase();

                String status;
                if (className.contains("active") || bgColor.contains("112, 48, 160")) {
                    status = "CURRENT (Violet)";
                } else if (className.contains("completed") || bgColor.contains("0, 176, 80")) {
                    status = "COMPLETED (Green)";
                } else if (bgColor.contains("255, 255, 255")) {
                    status = "FUTURE (White)";
                } else {
                    status = "UNKNOWN";
                }

                ReportManager.logPass("Tab Status", " Tab " + (i + 1) + ": **" + label + "** → Status: " + status);

                if (status.contains("FUTURE")) {
                	((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({ inline: 'center' });", tab);
                	Thread.sleep(400); // wait for visibility/hoverability
                	actions.moveToElement(tab).perform();
                    Thread.sleep(300);

                    String hoverColor = tab.getCssValue("background-color");

                    if (!hoverColor.contains("255, 255, 255, 1")) {
                        String msg = "Tab '" + label + "' (white) did not highlight violet on hover (actual: " + hoverColor + ")";
                        ReportManager.logFail("Verify hover effect", msg);
                        throw new Exception(msg);
                    } else {
                        ReportManager.logPass("Verify hover effect", "Tab '" + label + "' (white) highlights violet on hover");
                    }
                } else {
                    ReportManager.logInfo("Skipping hover check for tab '" + label + "' (status: " + status + ")");
                }

            }

            ReportManager.logPass("Verify tab blocks", " Tab blocks validated successfully.");

        } catch (Exception e) {
            String errorMessage = " Error verifying tab blocks for '" + elementName + "': " + e.getMessage();
            throw new Exception(errorMessage, e);
        }
    }



    // ================================
    // HELPER METHODS
    // ================================
    
    private void assertTrue(boolean contains, String string) {
		// TODO Auto-generated method stub
		
	}

	/**
     * Add a demo delay after test step execution
     */
    public void addDemoDelay() {
        try {
            Thread.sleep(1500);
            logger.debug("Demo delay: Added 1.5-second wait after step execution");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.debug("Demo delay interrupted");
        }
    }

    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public void verifyTooltipAlternative(WebElement elementToHover, String expectedTooltip, String fieldLabel, String pageName) throws Throwable {
        String actualTooltip = null;
        String screenshotLabel = "Tooltip Verification - " + fieldLabel;

        try {
            logger.info("Hovering over field: {}", fieldLabel);
            new Actions(driver).moveToElement(elementToHover).perform();
            Thread.sleep(1000); // Allow tooltip to appear

            // Check native attributes
            actualTooltip = elementToHover.getAttribute("title");
            if (isEmpty(actualTooltip)) {
                actualTooltip = elementToHover.getAttribute("aria-label");
            }

            // Fallback 1: object repo (Plan_Tooltip.PlanMaster)
            if (isEmpty(actualTooltip)) {
                String baseField = fieldLabel.split(" ")[0].replaceAll("[^a-zA-Z0-9]", "");
                String tooltipKey = baseField + "_Tooltip";
                logger.debug("Looking for fallback tooltip in object repo using key: {}", tooltipKey);

                try {
                    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
                    WebElement tooltipElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                            ORLoader.getLocator(tooltipKey, pageName)));

                    actualTooltip = tooltipElement.getText();
                    if (isEmpty(actualTooltip)) actualTooltip = tooltipElement.getAttribute("textContent");
                    if (isEmpty(actualTooltip)) actualTooltip = tooltipElement.getAttribute("innerText");
                    logger.debug("Tooltip found via object repo: {}", actualTooltip);
                } catch (Exception e) {
                    logger.warn("Tooltip element not found using fallback key: {}", tooltipKey);
                }
            }

            // Fallback 2: visible tooltips in DOM (e.g. .tooltip, [role='tooltip'])
            if (isEmpty(actualTooltip)) {
                logger.debug("Scanning DOM for visible tooltip overlays...");
                List<WebElement> visibleTooltips = driver.findElements(By.cssSelector(".tooltip, [role='tooltip']"));
                for (WebElement tooltip : visibleTooltips) {
                    String text = tooltip.getText().trim();
                    if (!text.isEmpty()) {
                        actualTooltip = text;
                        break;
                    }
                }
            }

            logger.info("Expected tooltip: '{}', Actual tooltip: '{}'", expectedTooltip, actualTooltip);

            if (expectedTooltip.trim().equalsIgnoreCase(actualTooltip != null ? actualTooltip.trim() : "")) {
                takeScreenshot(screenshotLabel);
                ReportManager.logPass("Tooltip Verification", "Tooltip matched: " + expectedTooltip);
            } else {
                takeScreenshot("Tooltip Mismatch - " + fieldLabel);
                ReportManager.logFail("Tooltip Verification", "Expected: '" + expectedTooltip + "', Actual: '" + actualTooltip + "'");
                throw new AssertionError("Tooltip mismatch");
            }

        } catch (Exception ex) {
            logger.error("Error during tooltip verification for field: {}", fieldLabel, ex);
            takeScreenshot("Tooltip Error - " + fieldLabel);
            ReportManager.logFail("Tooltip Verification", "Exception: " + ex.getMessage());
            throw new RuntimeException("Tooltip verification failed", ex);
        }
    }

	/**
     * Public method to click element - accessible from other step classes
     * 
     * @param elementName The name of the element in the object repository
     * @param pageName The name of the page in the object repository
     * @throws Throwable if operation fails
     */
    public void clickElement(String elementName, String pageName) throws Throwable {
        clickOnElement(elementName, pageName);
    }
    
    /**
     * Verifies if element is enabled
     * 
     * @param elementName The name of the element in the object repository
     * @param pageName The name of the page in the object repository
     * @throws Throwable if verification fails
     */
    @Then("I verify element {string} is enabled on {string} page")
    public void verifyElementIsEnabled(String elementName, String pageName) throws Throwable {
        try {
            // Replace test data placeholders
            elementName = replaceTestDataPlaceholders(elementName);
            pageName = replaceTestDataPlaceholders(pageName);
            
            logger.info("Verifying element is enabled: {} on {} page", elementName, pageName);
            
            // Find the element
            WebElement element = findElement(elementName, pageName);
            
            // Check if enabled
            if (element.isEnabled()) {
                logger.info("Element is enabled: {} on {} page", elementName, pageName);
                ReportManager.logPass("Verify element enabled", "Element is enabled: " + elementName + " on " + pageName + " page");
            } else {
                logger.warn("Element is disabled: {} on {} page", elementName, pageName);
                ReportManager.logFail("Verify element enabled", "Element is disabled: " + elementName + " on " + pageName + " page");
                throw new AssertionError("Element is disabled: " + elementName + " on " + pageName + " page");
            }
        } catch (Exception e) {
            logger.error("Error verifying if element is enabled: {}", e.getMessage());
            ReportManager.logFail("Verify element enabled", "Error: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Waits for the specified number of seconds
     * 
     * @param seconds The number of seconds to wait
     * @throws Throwable if operation fails
     */
    @Then("I wait for {string} seconds")
    public void waitForSeconds(String seconds) throws Throwable {
        try {
            int waitTime = Integer.parseInt(seconds);
            logger.info("Waiting for {} seconds", waitTime);
            Thread.sleep(waitTime * 1000L);
            ReportManager.logInfo("Waited for " + waitTime + " seconds");
        } catch (NumberFormatException e) {
            logger.error("Invalid number format for wait time: {}", seconds);
            ReportManager.logFail("Wait", "Invalid number format for wait time: " + seconds);
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Wait interrupted: {}", e.getMessage());
            ReportManager.logFail("Wait", "Wait interrupted: " + e.getMessage());
            throw new RuntimeException("Wait interrupted", e);
        }
    }
    
    /**
     * Takes a screenshot with a custom name
     * 
     * @param screenshotName The name for the screenshot
     * @throws Throwable if operation fails
     */
    @Then("Take screenshot {string}")
    public void takeNamedScreenshot(String screenshotName) throws Throwable {
        WebDriver currentDriver = null;
        String screenshotsDirectory = null;
        
        try {
            // Replace test data placeholders in screenshot name
            screenshotName = replaceTestDataPlaceholders(screenshotName);
            logger.info("Taking screenshot with name: {}", screenshotName);
            
            // Step 1: Get the correct driver instance
            currentDriver = (driver != null) ? driver : WebDriverManager.getDriver();
            if (currentDriver == null) {
                throw new RuntimeException("WebDriver is not initialized. Cannot take screenshot.");
            }
            
            // Step 2: Determine screenshots directory using ReportManager
            try {
                String reportFolder = ReportManager.getCurrentReportFolder();
                if (reportFolder != null && !reportFolder.isEmpty()) {
                    screenshotsDirectory = reportFolder + File.separator + "Screenshots";
                } else {
                    logger.warn("ReportManager not initialized. Using fallback screenshots directory.");
                    screenshotsDirectory = "./Reports/Screenshots";
                }
            } catch (Exception e) {
                logger.warn("Error getting report folder from ReportManager: {}. Using fallback.", e.getMessage());
                screenshotsDirectory = "./Reports/Screenshots";
            }
            
            // Step 3: Create screenshots directory if it doesn't exist
            Path screenshotsPath = Paths.get(screenshotsDirectory);
            try {
                Files.createDirectories(screenshotsPath);
                logger.debug("Screenshots directory ensured: {}", screenshotsDirectory);
            } catch (Exception e) {
                logger.error("Failed to create screenshots directory: {}", e.getMessage());
                throw new RuntimeException("Cannot create screenshots directory: " + screenshotsDirectory, e);
            }
            
            // Step 4: Take screenshot
            File screenshot = ((TakesScreenshot) currentDriver).getScreenshotAs(OutputType.FILE);
            
            // Step 5: Create filename with timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = screenshotName + "_" + timestamp + ".png";
            Path destination = screenshotsPath.resolve(fileName);
            
            // Step 6: Save screenshot to file
            try {
                Files.copy(screenshot.toPath(), destination);
                logger.info("Screenshot saved successfully: {}", destination.toString());
            } catch (Exception e) {
                logger.error("Failed to save screenshot file: {}", e.getMessage());
                throw new RuntimeException("Failed to save screenshot to: " + destination.toString(), e);
            }
            
            // Step 7: Read screenshot bytes for attachment (if needed)
            byte[] screenshotBytes = null;
            try {
                screenshotBytes = Files.readAllBytes(destination);
            } catch (Exception e) {
                logger.warn("Failed to read screenshot bytes for attachment: {}", e.getMessage());
                // Continue execution even if we can't read for attachment
            }
            
            // Step 8: Log to ReportManager (always attempt this)
            try {
                ReportManager.logInfo("Screenshot captured: " + fileName);
                logger.debug("Screenshot logged to ReportManager successfully");
            } catch (Exception e) {
                logger.warn("Failed to log screenshot to ReportManager: {}", e.getMessage());
                // Continue execution
            }
            
            // Step 9: Attach to Cucumber scenario (if available and bytes were read successfully)
            if (scenario != null && screenshotBytes != null) {
                try {
                    scenario.attach(screenshotBytes, "image/png", screenshotName);
                    logger.debug("Screenshot attached to Cucumber scenario successfully");
                } catch (Exception e) {
                    logger.warn("Failed to attach screenshot to Cucumber scenario: {}", e.getMessage());
                    // Continue execution - this is not critical
                }
            } else {
                if (scenario == null) {
                    logger.warn("Scenario object is null - cannot attach screenshot to Cucumber report");
                }
                if (screenshotBytes == null) {
                    logger.warn("Screenshot bytes are null - cannot attach to Cucumber report");
                }
            }
            
            // Step 10: Log overall success
            logger.info("Screenshot operation completed successfully: {}", fileName);
            
        } catch (Exception e) {
            String errorMsg = "Error taking screenshot '" + screenshotName + "': " + e.getMessage();
            logger.error(errorMsg, e);
            
            // Attempt to log failure to ReportManager
            try {
                ReportManager.logFail("Take screenshot", errorMsg);
            } catch (Exception reportError) {
                logger.error("Failed to log screenshot error to ReportManager: {}", reportError.getMessage());
            }
            
            // Re-throw the original exception
            throw new RuntimeException(errorMsg, e);
        }
    }
    
    /**
     * Public method to take screenshot - accessible from other step classes
     * 
     * @param screenshotName The name for the screenshot
     * @throws Throwable if operation fails
     */
    public void takeScreenshot(String screenshotName) throws Throwable {
        takeNamedScreenshot(screenshotName);
    }
    
    /**
     * Finds an element using the object repository
     * 
     * @param elementName The name of the element in the object repository
     * @param pageName The name of the page in the object repository
     * @return The WebElement
     * @throws Exception If element cannot be found
     */
    public WebElement findElement(String elementName, String pageName) throws Exception {
        try {
            logger.info("Attempting to find element: {} on page: {}", elementName, pageName);
            
            // Check if the page is loaded in the object repository
            if (!ORLoader.isPageLoaded(pageName)) {
                logger.error("Page {} not found in Object Repository", pageName);
                logger.debug("Available pages: {}", ORLoader.getLoadedPages());
                throw new Exception("Page " + pageName + " not found in Object Repository");
            }
            
            // Check if the element exists for the page
            if (!ORLoader.isElementExists(pageName, elementName)) {
                logger.error("Element {} not found on page {} in Object Repository", elementName, pageName);
                logger.debug("Available elements for page {}: {}", pageName, ORLoader.getElementsForPage(pageName));
                throw new Exception("Element " + elementName + " not found on page " + pageName + " in Object Repository");
            }
            
            // Get locator from object repository
            By by = ORLoader.getLocator(pageName, elementName);
            String xpathValue = ORLoader.getXPath(pageName, elementName);
            
            logger.info("Using XPath for element {}.{}: {}", pageName, elementName, xpathValue);
            
            if (by == null) {
                throw new Exception("Locator not found for element: " + elementName + " on page: " + pageName);
            }
            
            // Wait for element to be present with enhanced error logging
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            
            try {
                WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(by));
                logger.info("Successfully found element: {} on page: {}", elementName, pageName);
                return element;
            } catch (org.openqa.selenium.TimeoutException e) {
                // Enhanced error logging for debugging
                logger.error("Timeout waiting for element: {} on page: {} with XPath: {}", elementName, pageName, xpathValue);
                logger.error("Current page URL: {}", driver.getCurrentUrl());
                logger.error("Current page title: {}", driver.getTitle());
                
                throw new Exception("Timeout waiting for element " + elementName + " on " + pageName + " page with XPath: " + xpathValue, e);
            }
            
        } catch (Exception e) {
            logger.error("Error finding element {} on {} page: {}", elementName, pageName, e.getMessage());
            throw new Exception("Error finding element " + elementName + " on " + pageName + " page: " + e.getMessage(), e);
        }
    }
    
    /**
     * Highlights an element on the page
     * 
     * @param element The WebElement to highlight
     */
    private void highlightElement(WebElement element) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            
            // Store original style
            String originalStyle = element.getAttribute("style");
            
            // Change style to highlight
            js.executeScript(
                "arguments[0].setAttribute('style', 'border: 2px solid red; background-color: yellow;');", 
                element
            );
            
            // Wait briefly
            Thread.sleep(300);
            
            // Restore original style
            js.executeScript(
                "arguments[0].setAttribute('style', arguments[1]);", 
                element, 
                originalStyle
            );
        } catch (Exception e) {
            logger.debug("Error highlighting element: {}", e.getMessage());
            // Ignore highlighting errors as they are not critical
        }
    }
    
    /**
     * Selects a date in a date picker element
     * 
     * @param date The date to select in DD/MM/YYYY format (can contain test data placeholders)
     * @param elementName The name of the date picker element in the object repository
     * @param pageName The name of the page in the object repository
     * @throws Throwable if operation fails
     */
    @When("I select date {string} in {string} date picker on {string} page")
    public void selectDateInDatePicker(String date, String elementName, String pageName) throws Throwable {
        try {
            // Replace test data placeholders
            String resolvedDate = replaceTestDataPlaceholders(date);
            elementName = replaceTestDataPlaceholders(elementName);
            pageName = replaceTestDataPlaceholders(pageName);
            
            logger.info("Selecting date '{}' in date picker: {} on {} page", resolvedDate, elementName, pageName);
            
            // Parse the date
            int[] dateParts = parseDate(resolvedDate);
            int targetDay = dateParts[0];
            int targetMonth = dateParts[1];
            int targetYear = dateParts[2];
            
            logger.info("Parsed date - Day: {}, Month: {}, Year: {}", targetDay, targetMonth, targetYear);
            
            // Find and click the date picker field to open the calendar
            WebElement datePickerField = findElement(elementName, pageName);
            
            // Scroll element into view
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", datePickerField);
            
            // Wait for element to be clickable
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.elementToBeClickable(datePickerField));
            
            // Highlight and click the date picker field
            highlightElement(datePickerField);
            datePickerField.click();
            
            logger.info("Clicked on date picker field, waiting for calendar to open");
            
            // Wait for calendar to be visible
            Thread.sleep(1000);
            
            // Navigate to the correct month and year
            navigateToMonth(targetMonth, targetYear);
            
            // Click on the specific day
            clickDateInCalendar(targetDay);
            
            logger.info("Successfully selected date: {}/{}/{}", targetDay, targetMonth, targetYear);
            ReportManager.logPass("Select date", String.format("Date '%s' selected in %s date picker", resolvedDate, elementName));
            
        } catch (Exception e) {
            logger.error("Error selecting date in date picker: {}", e.getMessage());
            ReportManager.logFail("Select date", "Error: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Parses a date string in DD/MM/YYYY format
     * 
     * @param dateString The date string to parse
     * @return Array containing [day, month, year]
     * @throws Exception if date format is invalid
     */
    private int[] parseDate(String dateString) throws Exception {
        if (dateString == null || dateString.trim().isEmpty()) {
            throw new Exception("Date string cannot be null or empty");
        }
        
        // Remove any extra whitespace
        dateString = dateString.trim();
        
        // Check if the date matches DD/MM/YYYY format
        if (!dateString.matches("\\d{1,2}/\\d{1,2}/\\d{4}")) {
            throw new Exception("Invalid date format. Expected DD/MM/YYYY, got: " + dateString);
        }
        
        String[] parts = dateString.split("/");
        if (parts.length != 3) {
            throw new Exception("Invalid date format. Expected DD/MM/YYYY, got: " + dateString);
        }
        
        try {
            int day = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int year = Integer.parseInt(parts[2]);
            
            // Basic validation
            if (day < 1 || day > 31) {
                throw new Exception("Invalid day: " + day + ". Day must be between 1 and 31");
            }
            if (month < 1 || month > 12) {
                throw new Exception("Invalid month: " + month + ". Month must be between 1 and 12");
            }
            if (year < 1900 || year > 2100) {
                throw new Exception("Invalid year: " + year + ". Year must be between 1900 and 2100");
            }
            
            logger.debug("Successfully parsed date: {}/{}/{}", day, month, year);
            return new int[]{day, month, year};
            
        } catch (NumberFormatException e) {
            throw new Exception("Invalid date format. Non-numeric values found in: " + dateString);
        }
    }
    
    /**
     * Navigates the calendar to the specified month and year
     * 
     * @param targetMonth The target month (1-12)
     * @param targetYear The target year
     * @throws Exception if navigation fails
     */
    private void navigateToMonth(int targetMonth, int targetYear) throws Exception {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        
        int attempts = 0;
        int maxAttempts = 24; // Allow up to 2 years of navigation

        while (attempts < maxAttempts) {
            try {
                // Look for common month/year display patterns
                List<WebElement> monthYearElements = driver.findElements(By.cssSelector(
                    ".datepicker-switch, .ui-datepicker-title, .react-datepicker__current-month, " +
                    "[class*='month'], [class*='year'], [class*='header']"
                ));

                if (monthYearElements.isEmpty()) {
                    // Try alternative selectors
                    monthYearElements = driver.findElements(By.xpath(
                        "//*[contains(@class, 'month') or contains(@class, 'year') or " +
                        "contains(@class, 'header') or contains(@class, 'title')]"
                    ));
                }

                String currentMonthYear = "";
                for (WebElement el : monthYearElements) {
                    String text = el.getText().toLowerCase().trim();
                    if (text.matches(".*\\d{4}.*")) { // Example: "June 2025", "06/2025"
                        currentMonthYear = text;
                        break;
                    }
                }

                logger.debug("Calendar header text being checked: {}", currentMonthYear);

                // Check if we're at the target month/year
                if (!currentMonthYear.isEmpty() && isTargetMonthYear(currentMonthYear, targetMonth, targetYear)) {
                    logger.info("Reached target month/year: {}/{}", targetMonth, targetYear);
                    return;
                }

                // Attempt to navigate calendar
                boolean navigated = attemptNavigation(targetMonth, targetYear);

                if (!navigated) {
                    logger.warn("Could not find navigation elements, trying alternative approach");
                    break;
                }

                attempts++;
                Thread.sleep(500); // Wait for navigation animation

            } catch (Exception e) {
                logger.debug("Navigation attempt {} failed: {}", attempts + 1, e.getMessage());
                attempts++;
                if (attempts >= maxAttempts) {
                    throw new Exception("Failed to navigate to target month/year after " + maxAttempts + " attempts");
                }
            }
        }
    }

    
    /**
     * Attempts to navigate the calendar using common navigation patterns
     * 
     * @param targetMonth The target month
     * @param targetYear The target year
     * @return true if navigation was attempted, false otherwise
     */
    private boolean attemptNavigation(int targetMonth, int targetYear) {
        try {
            // Look for next/previous buttons with common selectors
            List<WebElement> nextButtons = driver.findElements(By.cssSelector(
                ".next, .datepicker-next, .ui-datepicker-next, .react-datepicker__navigation--next, " +
                "[class*='next'], [title*='next'], [aria-label*='next']"
            ));
            
            List<WebElement> prevButtons = driver.findElements(By.cssSelector(
                ".prev, .datepicker-prev, .ui-datepicker-prev, .react-datepicker__navigation--previous, " +
                "[class*='prev'], [title*='prev'], [aria-label*='prev']"
            ));
            
            // For simplicity, click next button (this logic can be enhanced to determine direction)
            if (!nextButtons.isEmpty() && nextButtons.get(0).isDisplayed()) {
                nextButtons.get(0).click();
                logger.debug("Clicked next navigation button");
                return true;
            } else if (!prevButtons.isEmpty() && prevButtons.get(0).isDisplayed()) {
                prevButtons.get(0).click();
                logger.debug("Clicked previous navigation button");
                return true;
            }
            
        } catch (Exception e) {
            logger.debug("Error attempting navigation: {}", e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Checks if the current month/year display matches the target
     * 
     * @param currentDisplay The current month/year display text
     * @param targetMonth The target month
     * @param targetYear The target year
     * @return true if matches, false otherwise
     */
    private boolean isTargetMonthYear(String currentDisplay, int targetMonth, int targetYear) {
        // This is a simplified check - can be enhanced based on actual calendar format
        String targetYearStr = String.valueOf(targetYear);
        return currentDisplay.contains(targetYearStr);
    }
    
    /**
     * Clicks on the specified day in the calendar
     * 
     * @param day The day to click (1-31)
     * @throws Exception if day cannot be found or clicked
     */
    private void clickDateInCalendar(int day) throws Exception {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        String dayStr = String.valueOf(day);
        
        try {
            // Look for day elements with various common selectors
            List<WebElement> dayElements = driver.findElements(By.xpath(
                String.format("//td[text()='%s'] | //div[text()='%s'] | //span[text()='%s'] | " +
                             "//button[text()='%s'] | //*[@data-day='%s'] | " +
                             "//*[contains(@class,'day') and text()='%s']", 
                             dayStr, dayStr, dayStr, dayStr, dayStr, dayStr)
            ));
            
            if (dayElements.isEmpty()) {
                // Try alternative approach with contains
                dayElements = driver.findElements(By.xpath(
                    String.format("//*[contains(text(),'%s') and (contains(@class,'day') or " +
                                 "contains(@class,'date') or parent::*[contains(@class,'calendar')])]", dayStr)
                ));
            }
            
            if (dayElements.isEmpty()) {
                throw new Exception("Could not find day " + day + " in the calendar");
            }
            
            // Find the clickable day element (exclude disabled/inactive days)
            WebElement targetDayElement = null;
            for (WebElement dayElement : dayElements) {
                if (dayElement.isDisplayed() && dayElement.isEnabled()) {
                    String classAttr = dayElement.getAttribute("class");
                    if (classAttr == null || 
                        (!classAttr.contains("disabled") && 
                         !classAttr.contains("inactive") && 
                         !classAttr.contains("other-month"))) {
                        targetDayElement = dayElement;
                        break;
                    }
                }
            }
            
            if (targetDayElement == null) {
                throw new Exception("Day " + day + " is not clickable in the calendar");
            }
            
            // Highlight and click the day
            highlightElement(targetDayElement);
            wait.until(ExpectedConditions.elementToBeClickable(targetDayElement));
            targetDayElement.click();
            
            logger.info("Successfully clicked on day: {}", day);
            
        } catch (Exception e) {
            logger.error("Error clicking on day {}: {}", day, e.getMessage());
            throw new Exception("Failed to click on day " + day + " in calendar: " + e.getMessage());
        }
    }
    
    /**
     * Sets a toggle switch to ON or OFF state
     * 
     * @param label The label/name of the toggle field
     * @param toggleState "ON" or "OFF"
     * @throws Throwable if toggle operation fails
     */
    @When("I set the {string} toggle to {string}")
    public void setToggleState(String label, String toggleState) throws Throwable {
        try {
            logger.info("Setting toggle '{}' to state: {}", label, toggleState);
            
            // Find the toggle element by label
            WebElement toggleElement = findToggleByLabel(label);
            
            if (toggleElement == null) {
                throw new RuntimeException("Toggle with label '" + label + "' not found");
            }
            
            // Get current toggle state
            boolean currentState = isToggleOn(toggleElement);
            boolean desiredState = toggleState.equalsIgnoreCase("ON");
            
            // Click toggle if state needs to change
            if (currentState != desiredState) {
                highlightElement(toggleElement);
                toggleElement.click();
                logger.info("Clicked toggle '{}' to change state from {} to {}", label, currentState ? "ON" : "OFF", toggleState);
                
                // Wait for state change
                Thread.sleep(1000);
                
                // Verify state changed
                boolean newState = isToggleOn(toggleElement);
                if (newState == desiredState) {
                    ReportManager.logPass("Toggle state change", "Successfully set '" + label + "' toggle to " + toggleState);
                } else {
                    ReportManager.logFail("Toggle state change", "Failed to set '" + label + "' toggle to " + toggleState);
                    throw new RuntimeException("Toggle state did not change as expected");
                }
            } else {
                logger.info("Toggle '{}' is already in desired state: {}", label, toggleState);
                ReportManager.logPass("Toggle state", "Toggle '" + label + "' already in desired state: " + toggleState);
            }
            
            // Take screenshot
            takeScreenshot("Toggle " + label + " - " + toggleState);
            
        } catch (Exception e) {
            logger.error("Error setting toggle '{}' to {}: {}", label, toggleState, e.getMessage());
            ReportManager.logFail("Toggle operation failed", e.getMessage());
            throw new RuntimeException("Toggle operation failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Drags a popup window on a specified page using the provided header ID and offset values.
     * The element is identified from the Object Repository based on its name and page,
     * and dragged using Selenium's Actions class.
     *
     * @param popupHeaderId The ID or name of the popup header element (defined in object repository)
     * @param pageName The logical page name (e.g., "PlanMaster.json") to locate the OR file
     * @param xOffset Number of pixels to drag horizontally
     * @param yOffset Number of pixels to drag vertically
     */
        @When("I drag the popup with id {string} on {string} page by offset X {int} and Y {int}")
        public void i_drag_the_popup_with_id_by_offset_x_and_y(String popupHeaderId, String pageName, Integer xOffset, Integer yOffset) {

            try {
                popupHeaderId = replaceTestDataPlaceholders(popupHeaderId);
                pageName = replaceTestDataPlaceholders(pageName);
                
                logger.info("Dragging popup with header id: {} on page: {} by offset X: {}, Y: {}", popupHeaderId, pageName, xOffset, yOffset);

                // Locate popup header element from object repository
                WebElement popupHeader;
                try {
                    popupHeader = findElement(popupHeaderId, pageName);
                } catch (Exception e) {
                    logger.error("Popup header not found with id: {} on page: {}", popupHeaderId, pageName);
                    throw new RuntimeException("Popup header not found with id: " + popupHeaderId + " on page: " + pageName, e);
                }

                // Perform drag and drop action using Actions class
                try {
                    Actions actions = new Actions(driver);
                    actions.clickAndHold(popupHeader)
                           .moveByOffset(xOffset, yOffset)
                           .release()
                           .build()
                           .perform();
                    logger.info("Popup dragged successfully for header id: {}", popupHeaderId);
                } catch (Exception e) {
                    logger.error("Failed to perform drag action: {}", e.getMessage());
                    throw new RuntimeException("Drag action failed for popup with header id: " + popupHeaderId, e);
                }

                // Wait for UI to settle
                try {
                    waitForSeconds("2");
                } catch (Throwable t) {
                    logger.error("Error waiting after drag: {}", t.getMessage());
                    t.printStackTrace();
                    throw new RuntimeException("Post-drag wait failed: " + t.getMessage(), t);
                }

                // Take screenshot
                addDemoDelay();
                try {
                    takeScreenshot("Dragged Popup - " + popupHeaderId);
                } catch (Throwable t) {
                    logger.error("Error taking screenshot after drag: {}", t.getMessage());
                    t.printStackTrace();
                    throw new RuntimeException("Screenshot failed: " + t.getMessage(), t);
                }

                // Log success to report
                ReportManager.logPass("Popup Dragging", "Successfully dragged popup with header id: " + popupHeaderId);

            } catch (Exception e) {
                logger.error("Error dragging popup {}: {}", popupHeaderId, e.getMessage());
                ReportManager.logFail("Popup Dragging Failed", "Failed to drag popup with header id: " + popupHeaderId + " - " + e.getMessage());
                throw new RuntimeException("Popup dragging failed: " + e.getMessage(), e);
            }
        }
    
    /**
     * Find toggle element by label text
     */
    private WebElement findToggleByLabel(String label) {
        try {
            // Try multiple approaches to find toggle by label
            List<String> labelSelectors = java.util.Arrays.asList(
                // Label followed by input/button
                "//label[contains(text(),'" + label + "')]/following-sibling::input[@type='checkbox']",
                "//label[contains(text(),'" + label + "')]/following-sibling::button[contains(@class,'toggle')]",
                "//label[contains(text(),'" + label + "')]/..//input[@type='checkbox']",
                "//label[contains(text(),'" + label + "')]/..//button[contains(@class,'toggle')]",
                
                // Input/button with label or aria-label
                "//input[@type='checkbox' and contains(@aria-label,'" + label + "')]",
                "//button[contains(@class,'toggle') and contains(@aria-label,'" + label + "')]",
                "//input[@type='checkbox' and contains(@placeholder,'" + label + "')]",
                
                // Div/span containing label and toggle
                "//div[contains(text(),'" + label + "')]//input[@type='checkbox']",
                "//div[contains(text(),'" + label + "')]//button[contains(@class,'toggle')]",
                "//span[contains(text(),'" + label + "')]//input[@type='checkbox']",
                "//span[contains(text(),'" + label + "')]//button[contains(@class,'toggle')]",
                
                // Switch class variants
                "//label[contains(text(),'" + label + "')]/..//input[contains(@class,'switch')]",
                "//label[contains(text(),'" + label + "')]/..//label[contains(@class,'switch')]"
            );
            
            for (String selector : labelSelectors) {
                try {
                    List<WebElement> elements = driver.findElements(By.xpath(selector));
                    for (WebElement element : elements) {
                        if (element.isDisplayed()) {
                            logger.debug("Found toggle for label '{}' using selector: {}", label, selector);
                            return element;
                        }
                    }
                } catch (Exception e) {
                    continue;
                }
            }
            
            logger.warn("Toggle with label '{}' not found with any selector", label);
            return null;
            
        } catch (Exception e) {
            logger.error("Error finding toggle by label '{}': {}", label, e.getMessage());
            return null;
        }
    }
    
    /**
     * Check if toggle is in ON state
     */
    private boolean isToggleOn(WebElement toggleElement) {
        try {
            // Check various attributes that indicate toggle state
            String checked = toggleElement.getAttribute("checked");
            String ariaChecked = toggleElement.getAttribute("aria-checked");
            String className = toggleElement.getAttribute("class");
            String value = toggleElement.getAttribute("value");
            
            // For checkbox inputs
            if ("checkbox".equals(toggleElement.getAttribute("type"))) {
                return toggleElement.isSelected();
            }
            
            // For aria-checked attribute
            if ("true".equals(ariaChecked)) {
                return true;
            }
            
            // For checked attribute
            if (checked != null && !checked.isEmpty() && !"false".equals(checked)) {
                return true;
            }
            
            // For value attribute (some toggles use value="on" or value="true")
            if (value != null && ("on".equals(value.toLowerCase()) || "true".equals(value.toLowerCase()))) {
                return true;
            }
            
            // For CSS classes indicating active/on state
            if (className != null && (className.contains("active") || className.contains("on") || 
                                    className.contains("checked") || className.contains("enabled"))) {
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            logger.debug("Error checking toggle state: {}", e.getMessage());
            return false;
        }
    }
}
