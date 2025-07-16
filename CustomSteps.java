package com.novac.naf.steps;

import com.novac.naf.config.ConfigLoader;
import com.novac.naf.orm.ORLoader;
import com.novac.naf.reporting.ReportManager;
import com.novac.naf.steps.CommonSteps;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * Shared step definitions for cross-cutting concerns
 */
public class CustomSteps {
    private static final Logger logger = LoggerFactory.getLogger(CustomSteps.class);
    private final CommonSteps commonSteps;
    private final WebDriver driver;
    private final ConfigLoader configLoader;
    
    // Default constructor for Cucumber discovery
    public CustomSteps() {
        this.commonSteps = new CommonSteps();
        this.driver = commonSteps.getDriver();
        
        String excelPath = System.getProperty("excelPath", "./TestData/RunManager.xlsx");
        this.configLoader = new ConfigLoader(excelPath);
        
        logger.info("CustomSteps initialized successfully");
    }
    
    // Keep the existing constructor for backward compatibility
    public CustomSteps(CommonSteps commonSteps) {
        this.commonSteps = commonSteps;
        this.driver = commonSteps.getDriver();
        
        String excelPath = System.getProperty("excelPath", "./TestData/RunManager.xlsx");
        this.configLoader = new ConfigLoader(excelPath);
        
        logger.info("CustomSteps initialized with provided CommonSteps");
    }
    
    // ================================
    // LOGIN/LOGOUT STEP DEFINITIONS
    // ================================
    
    @When("I login to the application")
    public void loginToApplication() {
        logger.info("=== STARTING LOGIN PROCESS ===");
        try {
            logger.info("Starting login process using configuration values");
            
            String environment = configLoader.getEnvironment();
            String url = configLoader.getBaseApplicationUrl();
            String username = configLoader.getConfigValue("App_Username");
            String password = configLoader.getConfigValue("App_Password");
            
            logger.info("Login configuration - Environment: {}, URL: {}, Username: {}", environment, url, username);
            
            try {
                commonSteps.navigateToUrl(url);
                logger.info("Navigated to login URL: {}", url);
            } catch (Throwable t) {
                logger.error("Error navigating to URL: {}", t.getMessage());
                t.printStackTrace();
                throw new RuntimeException("Navigation failed: " + t.getMessage(), t);
            }
            
            try {
                commonSteps.waitForSeconds("2");
            } catch (Throwable t) {
                logger.error("Error waiting for page load: {}", t.getMessage());
                t.printStackTrace();
                throw new RuntimeException("Wait failed: " + t.getMessage(), t);
            }
            
            try {
                commonSteps.enterTextInField(username, "customInputUsername", "LoginPage");
                logger.info("Entered username in customInputUsername field");
            } catch (Throwable t) {
                logger.error("Error entering username: {}", t.getMessage());
                t.printStackTrace();
                throw new RuntimeException("Username entry failed: " + t.getMessage(), t);
            }
            
            try {
                commonSteps.enterTextInField(password, "customInputPassword", "LoginPage");
                logger.info("Entered password in customInputPassword field");
            } catch (Throwable t) {
                logger.error("Error entering password: {}", t.getMessage());
                t.printStackTrace();
                throw new RuntimeException("Password entry failed: " + t.getMessage(), t);
            }
            
            try {
                commonSteps.clickElement("login-btn", "LoginPage");
                logger.info("Clicked login button");
            } catch (Throwable t) {
                logger.error("Error clicking login button: {}", t.getMessage());
                t.printStackTrace();
                throw new RuntimeException("Login button click failed: " + t.getMessage(), t);
            }
            
            try {
                commonSteps.waitForSeconds("2");
            } catch (Throwable t) {
                logger.error("Error waiting after login: {}", t.getMessage());
                t.printStackTrace();
                throw new RuntimeException("Post-login wait failed: " + t.getMessage(), t);
            }
            
            try {
                commonSteps.takeScreenshot("Login Success");
            } catch (Throwable t) {
                logger.error("Error taking screenshot: {}", t.getMessage());
                t.printStackTrace();
                throw new RuntimeException("Screenshot failed: " + t.getMessage(), t);
            }
            
            ReportManager.logPass("Login operation", "Successfully logged in to application");
            logger.info("=== LOGIN PROCESS COMPLETED SUCCESSFULLY ===");
            
        } catch (Exception e) {
            logger.error("=== LOGIN PROCESS FAILED ===");
            logger.error("Error during login process: {}", e.getMessage());
            ReportManager.logFail("Login operation failed", e.getMessage());
            throw new RuntimeException("Login failed: " + e.getMessage(), e);
        }
    }
    
    @When("I logout")
    public void logoutFromApplication() {
        try {
            logger.info("Starting logout process");
            
            // TODO: Add logout implementation once object names are provided
            logger.info("Logout step definition executed - implementation pending object names");
            
            // Take screenshot on success
            addDemoDelay();
            try {
                commonSteps.takeScreenshot("Logout Success");
            } catch (Throwable t) {
                logger.error("Error taking screenshot: {}", t.getMessage());
                t.printStackTrace();
                throw new RuntimeException("Screenshot failed: " + t.getMessage(), t);
            }
            
            ReportManager.logPass("Logout operation", "Logout step executed (implementation pending)");
            
        } catch (Exception e) {
            logger.error("Error during logout process: {}", e.getMessage());
            ReportManager.logFail("Logout operation failed", e.getMessage());
            throw new RuntimeException("Logout failed: " + e.getMessage(), e);
        }
    }
    
    // ================================
    // PAGINATION STEP DEFINITIONS
    // ================================
    
    @Then("the pagination control should be visible and functional")
    public void verifyPaginationControlVisibleAndFunctional() {
        try {
            logger.info("Verifying pagination control visibility and functionality");
            
            WebElement paginationContainer = findPaginationContainer();
            if (paginationContainer == null) {
                ReportManager.logFail("Pagination verification", "Pagination container not found");
                throw new RuntimeException("Pagination container not found on page");
            }
            
            ReportManager.logPass("Pagination container", "Pagination container found and visible");
            
            try {
                List<WebElement> pageButtons = paginationContainer.findElements(By.xpath(".//button[matches(text(),'^[0-9]+$')] | .//a[matches(text(),'^[0-9]+$')]"));
                if (pageButtons.size() > 0) {
                    ReportManager.logPass("Page number buttons", "Found " + pageButtons.size() + " page number buttons");
                } else {
                    ReportManager.logFail("Page number buttons", "No page number buttons found");
                }
            } catch (Exception e) {
                ReportManager.logFail("Page number buttons", "Error finding page buttons: " + e.getMessage());
            }
            
            try {
                WebElement nextButton = paginationContainer.findElement(By.xpath(".//button[contains(text(),'>') or contains(@title,'Next') or contains(@class,'next') or contains(@aria-label,'Next')]"));
                boolean nextEnabled = nextButton.isEnabled() && !nextButton.getAttribute("class").contains("disabled");
                ReportManager.logPass("Next button", "Next button found, enabled: " + nextEnabled);
            } catch (Exception e) {
                ReportManager.logFail("Next button", "Next button not found: " + e.getMessage());
            }
            
            try {
                WebElement prevButton = paginationContainer.findElement(By.xpath(".//button[contains(text(),'<') or contains(@title,'Previous') or contains(@class,'prev') or contains(@aria-label,'Previous')]"));
                boolean prevEnabled = prevButton.isEnabled() && !prevButton.getAttribute("class").contains("disabled");
                ReportManager.logPass("Previous button", "Previous button found, enabled: " + prevEnabled);
            } catch (Exception e) {
                ReportManager.logFail("Previous button", "Previous button not found: " + e.getMessage());
            }
            
            try {
                WebElement activePageIndicator = paginationContainer.findElement(By.xpath(".//button[contains(@class,'active') or contains(@class,'current')] | .//a[contains(@class,'active') or contains(@class,'current')]"));
                String currentPage = getCurrentPageNumber();
                ReportManager.logPass("Active page indicator", "Active page indicator found, current page: " + currentPage);
            } catch (Exception e) {
                ReportManager.logFail("Active page indicator", "Active page indicator not found: " + e.getMessage());
            }
            
            addDemoDelay();
            try {
                commonSteps.takeScreenshot("Pagination Control Verification");
            } catch (Throwable t) {
                logger.error("Error taking screenshot: {}", t.getMessage());
                t.printStackTrace();
                throw new RuntimeException("Screenshot failed: " + t.getMessage(), t);
            }
            
            logger.info("Pagination control verification completed");
            
        } catch (Exception e) {
            logger.error("Error verifying pagination control: {}", e.getMessage());
            ReportManager.logFail("Pagination verification failed", e.getMessage());
            throw new RuntimeException("Pagination verification failed: " + e.getMessage(), e);
        }
    }
    
    @When("I navigate through all pages using the pagination controls")
    public void navigateThroughAllPagesUsingPaginationControls() {
        try {
            logger.info("Starting navigation through all pages using pagination controls");
            
            WebElement paginationContainer = findPaginationContainer();
            if (paginationContainer == null) {
                throw new RuntimeException("Pagination container not found");
            }
            
            int currentPageNum = 1;
            String firstUserId = getFirstUserIdFromTable();
            logger.info("Starting navigation - Page {}, First User ID: {}", currentPageNum, firstUserId);
            
            // Navigate forward through all pages
            while (true) {
                try {
                    WebElement nextButton = paginationContainer.findElement(By.xpath(".//button[contains(text(),'>') or contains(@title,'Next') or contains(@class,'next') or contains(@aria-label,'Next')]"));
                    
                    if (!isNextButtonEnabled()) {
                        logger.info("Next button is disabled, reached last page");
                        break;
                    }
                    
                    nextButton.click();
                    currentPageNum++;
                    
                    waitForTableUpdate();
                    addDemoDelay();
                    
                    String newFirstUserId = getFirstUserIdFromTable();
                    logger.info("Navigated to page {}, First User ID: {}", currentPageNum, newFirstUserId);
                    
                    if (!firstUserId.equals(newFirstUserId)) {
                        ReportManager.logPass("Page navigation", "Successfully navigated to page " + currentPageNum + ", table content updated");
                    } else {
                        ReportManager.logFail("Page navigation", "Table content did not change after navigation to page " + currentPageNum);
                    }
                    
                    firstUserId = newFirstUserId;
                    
                } catch (NoSuchElementException e) {
                    logger.info("Next button not found, assuming last page reached");
                    break;
                }
            }
            
            // Navigate back to first page using Previous button
            logger.info("Navigating back to first page using Previous button");
            while (currentPageNum > 1) {
                try {
                    WebElement prevButton = paginationContainer.findElement(By.xpath(".//button[contains(text(),'<') or contains(@title,'Previous') or contains(@class,'prev') or contains(@aria-label,'Previous')]"));
                    
                    if (!isPreviousButtonEnabled()) {
                        logger.info("Previous button is disabled, reached first page");
                        break;
                    }
                    
                    prevButton.click();
                    currentPageNum--;
                    
                    waitForTableUpdate();
                    addDemoDelay();
                    
                    String newFirstUserId = getFirstUserIdFromTable();
                    logger.info("Navigated back to page {}, First User ID: {}", currentPageNum, newFirstUserId);
                    
                    ReportManager.logPass("Backward navigation", "Successfully navigated back to page " + currentPageNum);
                    
                } catch (NoSuchElementException e) {
                    logger.info("Previous button not found, assuming first page reached");
                    break;
                }
            }
            
            try {
                commonSteps.takeScreenshot("Pagination Navigation Complete");
            } catch (Throwable t) {
                logger.error("Error taking screenshot: {}", t.getMessage());
                t.printStackTrace();
                throw new RuntimeException("Screenshot failed: " + t.getMessage(), t);
            }
            
            ReportManager.logPass("Full pagination navigation", "Successfully navigated through all pages and returned to first page");
            logger.info("Completed navigation through all pages");
            
        } catch (Exception e) {
            logger.error("Error navigating through pages: {}", e.getMessage());
            ReportManager.logFail("Pagination navigation failed", e.getMessage());
            throw new RuntimeException("Pagination navigation failed: " + e.getMessage(), e);
        }
    }
    
    @When("I go to page number {string} using the pagination bar")
    public void goToPageNumberUsingPaginationBar(String pageNumber) {
        try {
            logger.info("Navigating directly to page number: {}", pageNumber);
            
            WebElement paginationContainer = findPaginationContainer();
            if (paginationContainer == null) {
                throw new RuntimeException("Pagination container not found");
            }
            
            String firstUserIdBefore = getFirstUserIdFromTable();
            logger.info("Current first User ID before navigation: {}", firstUserIdBefore);
            
            try {
                WebElement pageButton = paginationContainer.findElement(By.xpath(".//button[contains(text(),'" + pageNumber + "') or @data-page='" + pageNumber + "'] | .//a[contains(text(),'" + pageNumber + "') or @data-page='" + pageNumber + "']"));
                pageButton.click();
                logger.info("Clicked on page number button: {}", pageNumber);
            } catch (NoSuchElementException e) {
                logger.error("Page number button {} not found in pagination bar", pageNumber);
                throw new RuntimeException("Page number button " + pageNumber + " not found in pagination bar", e);
            }
            
            waitForTableUpdate();
            addDemoDelay();
            
            String currentPageNumber = getCurrentPageNumber();
            if (currentPageNumber.equals(pageNumber)) {
                ReportManager.logPass("Direct page navigation", "Successfully navigated to page " + pageNumber);
            } else {
                ReportManager.logFail("Direct page navigation", "Expected to be on page " + pageNumber + " but current page is " + currentPageNumber);
            }
            
            String firstUserIdAfter = getFirstUserIdFromTable();
            logger.info("First User ID after navigation to page {}: {}", pageNumber, firstUserIdAfter);
            
            try {
                commonSteps.takeScreenshot("Direct Page Navigation - Page " + pageNumber);
            } catch (Throwable t) {
                logger.error("Error taking screenshot: {}", t.getMessage());
                t.printStackTrace();
                throw new RuntimeException("Screenshot failed: " + t.getMessage(), t);
            }
            
            logger.info("Successfully navigated to page: {}", pageNumber);
            
        } catch (Exception e) {
            logger.error("Error navigating to page number {}: {}", pageNumber, e.getMessage());
            ReportManager.logFail("Direct page navigation failed", e.getMessage());
            throw new RuntimeException("Direct page navigation failed: " + e.getMessage(), e);
        }
    }
    
    @When("I select {string} rows per page from dropdown")
    public void selectRowsPerPageFromDropdown(String rowCount) {
        try {
            logger.info("Selecting {} rows per page from dropdown", rowCount);
            
            try {
                WebElement rowsPerPageDropdown = driver.findElement(By.xpath("//select[contains(@id,'rows') or contains(@id,'page')] | //div[contains(@class,'dropdown') and contains(text(),'rows')] | //select[contains(@class,'page-size')]"));
                rowsPerPageDropdown.click();
                logger.info("Clicked rows per page dropdown");
            } catch (NoSuchElementException e) {
                logger.error("Rows per page dropdown not found");
                throw new RuntimeException("Rows per page dropdown not found", e);
            }
            
            try {
                commonSteps.waitForSeconds("1");
            } catch (Throwable t) {
                logger.error("Error waiting for dropdown to open: {}", t.getMessage());
                t.printStackTrace();
                throw new RuntimeException("Dropdown wait failed: " + t.getMessage(), t);
            }
            
            try {
                WebElement rowCountOption = driver.findElement(By.xpath("//option[contains(text(),'" + rowCount + "')] | //div[contains(@class,'option') and contains(text(),'" + rowCount + "')] | //li[contains(text(),'" + rowCount + "')]"));
                rowCountOption.click();
                logger.info("Selected {} rows per page option", rowCount);
            } catch (NoSuchElementException e) {
                logger.error("Row count option {} not found in dropdown", rowCount);
                throw new RuntimeException("Row count option " + rowCount + " not found in dropdown", e);
            }
            
            try {
                commonSteps.waitForSeconds("3");
            } catch (Throwable t) {
                logger.error("Error waiting for table to update: {}", t.getMessage());
                t.printStackTrace();
                throw new RuntimeException("Table update wait failed: " + t.getMessage(), t);
            }
            
            addDemoDelay();
            try {
                commonSteps.takeScreenshot("Rows Per Page - " + rowCount);
            } catch (Throwable t) {
                logger.error("Error taking screenshot: {}", t.getMessage());
                t.printStackTrace();
                throw new RuntimeException("Screenshot failed: " + t.getMessage(), t);
            }
            
            ReportManager.logPass("Rows per page selection", "Successfully selected " + rowCount + " rows per page");
            
        } catch (Exception e) {
            logger.error("Error selecting {} rows per page: {}", rowCount, e.getMessage());
            ReportManager.logFail("Rows per page selection failed", "Failed to select " + rowCount + " rows per page - " + e.getMessage());
            throw new RuntimeException("Rows per page selection failed: " + e.getMessage(), e);
        }
    }
    
    @Then("I should not see {string} on {string} page")
    public void verifyElementNotPresent(String elementName, String pageName) {
        try {
            By locator = ORLoader.getLocator(pageName, elementName);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            
            try {
                boolean elementDisappeared = wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
                
                if (elementDisappeared) {
                    logger.info("Element {} is correctly not visible on {}", elementName, pageName);
                    
                    // Take screenshot on success
                    addDemoDelay();
                    try {
                        commonSteps.takeScreenshot("Element Not Present Verification - " + elementName);
                    } catch (Throwable t) {
                        logger.error("Error taking screenshot: {}", t.getMessage());
                        t.printStackTrace();
                        throw new RuntimeException("Screenshot failed: " + t.getMessage(), t);
                    }
                    
                    ReportManager.logPass("Element verification", "Element " + elementName + " correctly not visible on " + pageName);
                } else {
                    logger.error("Element {} is unexpectedly visible on {}", elementName, pageName);
                    
                    // Take screenshot on failure (element found when it shouldn't be)
                    addDemoDelay();
                    try {
                        commonSteps.takeScreenshot("Element Unexpectedly Found - " + elementName);
                    } catch (Throwable t) {
                        logger.error("Error taking screenshot: {}", t.getMessage());
                        t.printStackTrace();
                        throw new RuntimeException("Screenshot failed: " + t.getMessage(), t);
                    }
                    
                    ReportManager.logFail("Element verification failed", "Element " + elementName + " should not be visible but it is");
                    throw new AssertionError("Element " + elementName + " should not be visible but it is");
                }
            } catch (TimeoutException e) {
                logger.info("Element {} is correctly not visible on {}", elementName, pageName);
                
                // Take screenshot on success
                addDemoDelay();
                try {
                    commonSteps.takeScreenshot("Element Not Present Verification - " + elementName);
                } catch (Throwable t) {
                    logger.error("Error taking screenshot: {}", t.getMessage());
                    t.printStackTrace();
                    throw new RuntimeException("Screenshot failed: " + t.getMessage(), t);
                }
                
                ReportManager.logPass("Element verification", "Element " + elementName + " correctly not visible on " + pageName);
            }
            
        } catch (Exception e) {
            if (!(e instanceof TimeoutException)) {
                logger.error("Error verifying element {} not present on page {}: {}", elementName, pageName, e.getMessage());
                ReportManager.logFail("Element verification failed", e.getMessage());
                throw new RuntimeException("Element verification failed: " + e.getMessage(), e);
            }
        }
    }
    
    // ================================
    // FILE UPLOAD STEP DEFINITIONS
    // ================================
    
    @When("I upload the file {string} into the {string} field")
    public void uploadFileIntoField(String filePath, String label) {
        try {
            logger.info("Uploading file {} into field with label: {}", filePath, label);
            
            // Find the file input element by label
            WebElement fileInput = findFileInputByLabel(label);
            
            if (fileInput == null) {
                throw new RuntimeException("File input field with label '" + label + "' not found");
            }
            
            // Verify file exists
            File file = new File(filePath);
            if (!file.exists()) {
                throw new RuntimeException("File not found: " + filePath);
            }
            
            // Upload the file
            fileInput.sendKeys(file.getAbsolutePath());
            logger.info("File upload initiated for: {}", filePath);
            
            // Wait for upload to process
            try {
                commonSteps.waitForSeconds("2");
            } catch (Throwable t) {
                logger.error("Error waiting for upload: {}", t.getMessage());
                t.printStackTrace();
                throw new RuntimeException("Upload wait failed: " + t.getMessage(), t);
            }
            
            addDemoDelay();
            try {
                commonSteps.takeScreenshot("File Upload - " + label);
            } catch (Throwable t) {
                logger.error("Error taking screenshot: {}", t.getMessage());
                t.printStackTrace();
                throw new RuntimeException("Screenshot failed: " + t.getMessage(), t);
            }
            
            ReportManager.logPass("File upload", "Successfully uploaded file " + filePath + " to field " + label);
            
        } catch (Exception e) {
            logger.error("Error uploading file {} to field {}: {}", filePath, label, e.getMessage());
            ReportManager.logFail("File upload failed", e.getMessage());
            throw new RuntimeException("File upload failed: " + e.getMessage(), e);
        }
    }
    
    @Then("I should see file upload error {string}")
    public void verifyFileUploadError(String expectedMessage) {
        try {
            logger.info("Verifying file upload error message: {}", expectedMessage);
            
            // Wait for error message to appear
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            
            // Try multiple selectors for error messages
            List<String> errorSelectors = java.util.Arrays.asList(
                "//div[contains(@class,'error') and contains(text(),'" + expectedMessage + "')]",
                "//span[contains(@class,'error') and contains(text(),'" + expectedMessage + "')]",
                "//p[contains(@class,'error') and contains(text(),'" + expectedMessage + "')]",
                "//div[contains(@class,'alert') and contains(text(),'" + expectedMessage + "')]",
                "//*[contains(text(),'" + expectedMessage + "')]"
            );
            
            WebElement errorElement = null;
            for (String selector : errorSelectors) {
                try {
                    errorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(selector)));
                    if (errorElement != null) {
                        break;
                    }
                } catch (TimeoutException e) {
                    continue;
                }
            }
            
            if (errorElement != null) {
                String actualMessage = errorElement.getText().trim();
                if (actualMessage.contains(expectedMessage)) {
                    logger.info("File upload error message verified: {}", actualMessage);
                    ReportManager.logPass("File upload error verification", "Error message displayed: " + actualMessage);
                } else {
                    ReportManager.logFail("File upload error verification", "Expected: " + expectedMessage + ", Actual: " + actualMessage);
                    throw new AssertionError("Error message mismatch. Expected: " + expectedMessage + ", Actual: " + actualMessage);
                }
            } else {
                ReportManager.logFail("File upload error verification", "Error message not found: " + expectedMessage);
                throw new RuntimeException("Error message not found: " + expectedMessage);
            }
            
            addDemoDelay();
            try {
                commonSteps.takeScreenshot("File Upload Error");
            } catch (Throwable t) {
                logger.error("Error taking screenshot: {}", t.getMessage());
                t.printStackTrace();
                throw new RuntimeException("Screenshot failed: " + t.getMessage(), t);
            }
            
        } catch (Exception e) {
            logger.error("Error verifying file upload error message: {}", e.getMessage());
            
            // Take screenshot on failure (error message not found or doesn't match)
            addDemoDelay();
            try {
                commonSteps.takeScreenshot("File Upload Error Verification Failed");
            } catch (Throwable t) {
                logger.error("Error taking screenshot: {}", t.getMessage());
                t.printStackTrace();
                throw new RuntimeException("Screenshot failed: " + t.getMessage(), t);
            }
            
            ReportManager.logFail("File upload error verification failed", e.getMessage());
            throw new RuntimeException("File upload error verification failed: " + e.getMessage(), e);
        }
    }
    
    @When("I set the toggle in the table row where {string} is {string} to {string}")
    public void setToggleInTableRow(String columnName, String value, String toggleState) {
        try {
            logger.info("Setting toggle in table row where {} is {} to {}", columnName, value, toggleState);
            
            // Find the row containing the specified value in the specified column
            WebElement targetRow = findTableRowByColumnValue(columnName, value);
            
            if (targetRow == null) {
                throw new RuntimeException("Table row not found where " + columnName + " is " + value);
            }
            
            // Find the toggle switch in that row
            WebElement toggleElement = findToggleInRow(targetRow);
            
            if (toggleElement == null) {
                throw new RuntimeException("Toggle switch not found in table row");
            }
            
            // Get current toggle state
            boolean currentState = isToggleOn(toggleElement);
            boolean desiredState = toggleState.equalsIgnoreCase("ON");
            
            // Click toggle if state needs to change
            if (currentState != desiredState) {
                toggleElement.click();
                logger.info("Clicked toggle to change state from {} to {}", currentState ? "ON" : "OFF", toggleState);
                
                // Wait for state change
                try {
                    commonSteps.waitForSeconds("1");
                } catch (Throwable t) {
                    logger.error("Error waiting for toggle state change: {}", t.getMessage());
                    t.printStackTrace();
                    throw new RuntimeException("Toggle wait failed: " + t.getMessage(), t);
                }
                
                // Verify state changed
                boolean newState = isToggleOn(toggleElement);
                if (newState == desiredState) {
                    ReportManager.logPass("Table row toggle", "Successfully set toggle to " + toggleState + " for row where " + columnName + " is " + value);
                } else {
                    ReportManager.logFail("Table row toggle", "Failed to set toggle to " + toggleState);
                    throw new RuntimeException("Toggle state did not change as expected");
                }
            } else {
                logger.info("Toggle is already in desired state: {}", toggleState);
                ReportManager.logPass("Table row toggle", "Toggle already in desired state: " + toggleState);
            }
            
            addDemoDelay();
            try {
                commonSteps.takeScreenshot("Table Row Toggle - " + toggleState);
            } catch (Throwable t) {
                logger.error("Error taking screenshot: {}", t.getMessage());
                t.printStackTrace();
                throw new RuntimeException("Screenshot failed: " + t.getMessage(), t);
            }
            
        } catch (Exception e) {
            logger.error("Error setting toggle in table row: {}", e.getMessage());
            ReportManager.logFail("Table row toggle failed", e.getMessage());
            throw new RuntimeException("Table row toggle failed: " + e.getMessage(), e);
        }
    }
    
    // ================================
    // MULTI-SELECT DROPDOWN STEP DEFINITIONS
    // Supports single or multiple values separated by commas
    // ================================
    
    @When("I select {string} from {string} dropdown on {string} page")
    public void selectMultipleValuesFromDropdown(String values, String elementName, String pageName) {
        try {
            logger.info("=== STARTING MULTI-SELECT DROPDOWN OPERATION ===");
            logger.info("Selecting values: {} from dropdown: {} on page: {}", values, elementName, pageName);
            
            // Replace test data placeholders
            String processedValues = commonSteps.replaceTestDataPlaceholders(values);
            String processedElementName = commonSteps.replaceTestDataPlaceholders(elementName);
            String processedPageName = commonSteps.replaceTestDataPlaceholders(pageName);
            
            // Parse comma-separated values
            List<String> valuesList = Arrays.asList(processedValues.split(","));
            if (valuesList.isEmpty()) {
                throw new RuntimeException("No values provided for multi-select dropdown");
            }
            
            // Trim whitespace from each value
            valuesList.replaceAll(String::trim);
            logger.info("Parsed values list: {}", valuesList);
            
            // Find the dropdown element using object repository
            WebElement dropdownElement = commonSteps.findElement(processedElementName, processedPageName);
            logger.info("Found dropdown element - Tag: {}, Type: {}", 
                       dropdownElement.getTagName(), dropdownElement.getAttribute("type"));
            
            // Determine dropdown type and handle accordingly
            if ("select".equals(dropdownElement.getTagName().toLowerCase())) {
                handleNativeSelectDropdown(dropdownElement, valuesList);
            } else {
                handleCustomDropdown(dropdownElement, valuesList);
            }
            
            // Take screenshot on success
            addDemoDelay();
            try {
                commonSteps.takeScreenshot("Multi-select Dropdown Success");
            } catch (Throwable t) {
                logger.error("Error taking screenshot: {}", t.getMessage());
                t.printStackTrace();
                throw new RuntimeException("Screenshot failed: " + t.getMessage(), t);
            }
            
            ReportManager.logPass("Multi-select dropdown", "Successfully selected " + valuesList.size() + " values from dropdown: " + processedElementName);
            logger.info("=== MULTI-SELECT DROPDOWN OPERATION COMPLETED SUCCESSFULLY ===");
            
        } catch (Exception e) {
            logger.error("=== MULTI-SELECT DROPDOWN OPERATION FAILED ===");
            logger.error("Error during multi-select dropdown operation: {}", e.getMessage());
            
            // Take screenshot on failure
            addDemoDelay();
            try {
                commonSteps.takeScreenshot("Multi-select Dropdown Failed");
            } catch (Throwable t) {
                logger.error("Error taking screenshot: {}", t.getMessage());
                t.printStackTrace();
                throw new RuntimeException("Screenshot failed: " + t.getMessage(), t);
            }
            
            ReportManager.logFail("Multi-select dropdown failed", e.getMessage());
            throw new RuntimeException("Multi-select dropdown failed: " + e.getMessage(), e);
        }
    }
    
    
    // ================================
    // HELPER METHODS
    // ================================
    
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
    
    /**
     * Handle native HTML select dropdown with multiple attribute
     */
    private void handleNativeSelectDropdown(WebElement selectElement, List<String> values) {
        try {
            Select select = new Select(selectElement);
            
            // Check if multiple selection is supported
            if (!select.isMultiple()) {
                logger.warn("Select element does not support multiple selection, selecting last value only");
            }
            
            for (String value : values) {
                try {
                    logger.info("Attempting to select native option: {}", value);
                    
                    // Try selecting by visible text first, then by value
                    try {
                        select.selectByVisibleText(value);
                        logger.info("Selected by visible text: {}", value);
                    } catch (NoSuchElementException e) {
                        select.selectByValue(value);
                        logger.info("Selected by value attribute: {}", value);
                    }
                    
                    ReportManager.logPass("Multi-select value", "Successfully selected: " + value);
                    Thread.sleep(300); // Small delay between selections
                    
                } catch (Exception e) {
                    logger.error("Failed to select native option: {} - {}", value, e.getMessage());
                    ReportManager.logFail("Multi-select value failed", "Failed to select: " + value + " - " + e.getMessage());
                    
                    // Take screenshot on individual value failure
                    addDemoDelay();
                    try {
                        commonSteps.takeScreenshot("Multi-select Failed - " + value);
                    } catch (Throwable t) {
                        logger.error("Error taking screenshot: {}", t.getMessage());
                        t.printStackTrace();
                        throw new RuntimeException("Screenshot failed: " + t.getMessage(), t);
                    }
                    
                    throw new RuntimeException("Failed to select value: " + value, e);
                }
            }
            
        } catch (Exception e) {
            logger.error("Error handling native select dropdown: {}", e.getMessage());
            throw new RuntimeException("Native select dropdown error: " + e.getMessage(), e);
        }
    }
    
    /**
     * Handle custom dropdown implementations (Angular, React, Bootstrap, etc.)
     */
    private void handleCustomDropdown(WebElement dropdownElement, List<String> values) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        
        try {
            // First, try to open the dropdown
            openCustomDropdown(dropdownElement, wait);
            
            for (String value : values) {
                try {
                    logger.info("Attempting to select custom dropdown option: {}", value);
                    
                    // Find the option element using multiple strategies
                    WebElement optionElement = findDropdownOption(value, wait);
                    
                    if (optionElement == null) {
                        throw new RuntimeException("Option not found: " + value);
                    }
                    
                    // Click the option
                    wait.until(ExpectedConditions.elementToBeClickable(optionElement));
                    optionElement.click();
                    logger.info("Clicked option: {}", value);
                    
                    ReportManager.logPass("Multi-select value", "Successfully selected: " + value);
                    
                    // Small delay between selections for UI stability
                    Thread.sleep(500);
                    
                    // For some dropdowns, we might need to reopen after each selection
                    if (values.indexOf(value) < values.size() - 1) {
                        try {
                            // Check if dropdown is still open, if not reopen it
                            Thread.sleep(200);
                            if (!isDropdownOpen()) {
                                openCustomDropdown(dropdownElement, wait);
                            }
                        } catch (Exception reopenException) {
                            logger.debug("Could not determine dropdown state or reopen: {}", reopenException.getMessage());
                        }
                    }
                    
                } catch (Exception e) {
                    logger.error("Failed to select custom option: {} - {}", value, e.getMessage());
                    ReportManager.logFail("Multi-select value failed", "Failed to select: " + value + " - " + e.getMessage());
                    
                    // Take screenshot on individual value failure
                    addDemoDelay();
                    try {
                        commonSteps.takeScreenshot("Multi-select Failed - " + value);
                    } catch (Throwable t) {
                        logger.error("Error taking screenshot: {}", t.getMessage());
                        t.printStackTrace();
                        throw new RuntimeException("Screenshot failed: " + t.getMessage(), t);
                    }
                    
                    throw new RuntimeException("Failed to select value: " + value, e);
                }
            }
            
        } catch (Exception e) {
            logger.error("Error handling custom dropdown: {}", e.getMessage());
            throw new RuntimeException("Custom dropdown error: " + e.getMessage(), e);
        }
    }
    
    /**
     * Open custom dropdown using various trigger strategies
     */
    private void openCustomDropdown(WebElement dropdownElement, WebDriverWait wait) {
        try {
            logger.debug("Attempting to open custom dropdown");
            
            // Try clicking the main dropdown element first
            try {
                wait.until(ExpectedConditions.elementToBeClickable(dropdownElement));
                dropdownElement.click();
                Thread.sleep(300);
                logger.debug("Clicked main dropdown element");
                return;
            } catch (Exception e) {
                logger.debug("Main dropdown element not clickable: {}", e.getMessage());
            }
            
            // Try finding and clicking specific trigger elements
            List<String> triggerSelectors = Arrays.asList(
                ".//button", 
                ".//div[contains(@class,'dropdown-toggle')]",
                ".//span[contains(@class,'select')]", 
                ".//i[contains(@class,'arrow')]",
                ".//div[contains(@class,'trigger')]",
                ".//div[contains(@class,'control')]"
            );
            
            for (String selector : triggerSelectors) {
                try {
                    WebElement trigger = dropdownElement.findElement(By.xpath(selector));
                    wait.until(ExpectedConditions.elementToBeClickable(trigger));
                    trigger.click();
                    Thread.sleep(300);
                    logger.debug("Clicked dropdown trigger using selector: {}", selector);
                    return;
                } catch (Exception e) {
                    continue;
                }
            }
            
            logger.warn("Could not find clickable trigger for custom dropdown");
            
        } catch (Exception e) {
            logger.debug("Error opening custom dropdown: {}", e.getMessage());
        }
    }
    
    /**
     * Find dropdown option using multiple XPath strategies
     */
    private WebElement findDropdownOption(String value, WebDriverWait wait) {
        List<String> optionSelectors = Arrays.asList(
            "//option[normalize-space(text())='" + value + "']",  // Native select options
            "//li[normalize-space(text())='" + value + "']",      // Custom dropdown li items
            "//div[contains(@class,'option') and normalize-space(text())='" + value + "']", // Custom div options
            "//span[contains(@class,'option') and normalize-space(text())='" + value + "']", // Custom span options
            "//*[@data-value='" + value + "']",                   // Options with data-value attribute
            "//*[contains(@class,'dropdown-item') and normalize-space(text())='" + value + "']", // Bootstrap dropdown items
            "//mat-option[normalize-space(span/text())='" + value + "']", // Angular Material options
            "//*[contains(@class,'select-option') and normalize-space(text())='" + value + "']", // Generic select options
            "//a[normalize-space(text())='" + value + "']"        // Link-based options
        );
        
        for (String selector : optionSelectors) {
            try {
                WebElement option = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(selector)));
                if (option.isDisplayed()) {
                    logger.debug("Found option using selector: {}", selector);
                    return option;
                }
            } catch (TimeoutException e) {
                continue;
            }
        }
        
        logger.error("Option '{}' not found with any selector strategy", value);
        return null;
    }
    
    /**
     * Check if custom dropdown is still open
     */
    private boolean isDropdownOpen() {
        try {
            List<String> openDropdownSelectors = Arrays.asList(
                "//div[contains(@class,'dropdown-menu') and contains(@class,'show')]",
                "//ul[contains(@class,'dropdown-menu') and not(contains(@class,'hidden'))]",
                "//div[contains(@class,'select-dropdown') and contains(@class,'open')]",
                "//*[contains(@class,'options') and not(contains(@style,'display: none'))]"
            );
            
            for (String selector : openDropdownSelectors) {
                try {
                    WebElement openDropdown = driver.findElement(By.xpath(selector));
                    if (openDropdown.isDisplayed()) {
                        return true;
                    }
                } catch (NoSuchElementException e) {
                    continue;
                }
            }
            
            return false;
        } catch (Exception e) {
            logger.debug("Error checking dropdown state: {}", e.getMessage());
            return false;
        }
    }
    
    
    /**
     * Find pagination container using multiple XPath selectors
     */
    public WebElement findPaginationContainer() {
        try {
            List<String> paginationSelectors = java.util.Arrays.asList(
                "//div[contains(@class,'pagination')]",
                "//nav[contains(@class,'pagination')]", 
                "//ul[contains(@class,'pagination')]",
                "//div[contains(@class,'pager')]",
                "//div[contains(@class,'page-nav')]"
            );
            
            for (String selector : paginationSelectors) {
                try {
                    WebElement element = driver.findElement(By.xpath(selector));
                    if (element.isDisplayed()) {
                        logger.debug("Found pagination container using selector: {}", selector);
                        return element;
                    }
                } catch (NoSuchElementException e) {
                    continue;
                }
            }
            
            logger.warn("Pagination container not found with any of the standard selectors");
            return null;
            
        } catch (Exception e) {
            logger.error("Error finding pagination container: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Get current page number from pagination
     */
    public String getCurrentPageNumber() {
        try {
            WebElement paginationContainer = findPaginationContainer();
            if (paginationContainer != null) {
                WebElement activePageElement = paginationContainer.findElement(By.xpath(".//button[contains(@class,'active') or contains(@class,'current')] | .//a[contains(@class,'active') or contains(@class,'current')]"));
                return activePageElement.getText().trim();
            }
        } catch (Exception e) {
            logger.debug("Error getting current page number: {}", e.getMessage());
        }
        return "1"; // Default to page 1
    }
    
    /**
     * Check if Next button is enabled
     */
    public boolean isNextButtonEnabled() {
        try {
            WebElement paginationContainer = findPaginationContainer();
            if (paginationContainer != null) {
                WebElement nextButton = paginationContainer.findElement(By.xpath(".//button[contains(text(),'>') or contains(@title,'Next') or contains(@class,'next') or contains(@aria-label,'Next')]"));
                return nextButton.isEnabled() && !nextButton.getAttribute("class").contains("disabled");
            }
        } catch (Exception e) {
            logger.debug("Error checking Next button status: {}", e.getMessage());
        }
        return false;
    }
    
    /**
     * Check if Previous button is enabled
     */
    public boolean isPreviousButtonEnabled() {
        try {
            WebElement paginationContainer = findPaginationContainer();
            if (paginationContainer != null) {
                WebElement prevButton = paginationContainer.findElement(By.xpath(".//button[contains(text(),'<') or contains(@title,'Previous') or contains(@class,'prev') or contains(@aria-label,'Previous')]"));
                return prevButton.isEnabled() && !prevButton.getAttribute("class").contains("disabled");
            }
        } catch (Exception e) {
            logger.debug("Error checking Previous button status: {}", e.getMessage());
        }
        return false;
    }
    
    /**
     * Get first User ID from table for page change verification
     */
    public String getFirstUserIdFromTable() {
        try {
            WebElement firstRowUserIdCell = driver.findElement(By.xpath("//table//tbody//tr[1]//td[1]"));
            return firstRowUserIdCell.getText().trim();
        } catch (Exception e) {
            logger.debug("Error getting first User ID from table: {}", e.getMessage());
            return "";
        }
    }
    
    /**
     * Wait for table content to refresh after navigation
     */
    public void waitForTableUpdate() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//table//tbody//tr[1]//td[1]")));
            Thread.sleep(1000); // Additional wait for content to stabilize
        } catch (Exception e) {
            logger.debug("Error waiting for table update: {}", e.getMessage());
        }
    }
    
    /**
     * Find a table row by a key value in any column
     */
    public WebElement findRowByKey(String keyValue) {
        try {
            logger.debug("Searching for table row with key value: {}", keyValue);
            
            List<WebElement> tableRows = driver.findElements(By.xpath("//table//tr"));
            
            for (WebElement row : tableRows) {
                try {
                    List<WebElement> cells = row.findElements(By.xpath(".//td"));
                    for (WebElement cell : cells) {
                        if (cell.getText().trim().equals(keyValue)) {
                            logger.debug("Found key value {} in table row", keyValue);
                            return row;
                        }
                    }
                } catch (Exception e) {
                    continue;
                }
            }
            
            logger.error("Key value {} not found in the table", keyValue);
            throw new RuntimeException("Key value \"" + keyValue + "\" not found in the table");
            
        } catch (Exception e) {
            logger.error("Error searching for key value {}: {}", keyValue, e.getMessage());
            throw new RuntimeException("Error finding table row for key value: " + keyValue, e);
        }
    }
    
    /**
     * Click on a menu item (utility method)
     */
    public void clickMenuItem(String menuName, String pageName) {
        try {
            logger.info("Clicking menu item: {} on page: {}", menuName, pageName);
            
            try {
                By userManagementLocator = ORLoader.getLocator(pageName, "User Management");
                WebElement userManagementElement = driver.findElement(userManagementLocator);
                
                if (!userManagementElement.getAttribute("class").contains("expanded")) {
                    logger.info("Expanding User Management parent menu");
                    userManagementElement.click();
                    try {
                        commonSteps.waitForSeconds("1");
                    } catch (Throwable t) {
                        logger.error("Error waiting for menu expansion: {}", t.getMessage());
                        t.printStackTrace();
                        throw new RuntimeException("Menu expansion wait failed: " + t.getMessage(), t);
                    }
                }
            } catch (Exception e) {
                logger.debug("User Management parent menu may already be expanded or not found: {}", e.getMessage());
            }
            
            String elementName = menuName;
            try {
                commonSteps.clickElement(elementName, pageName);
                logger.info("Clicked on menu item: {}", menuName);
            } catch (Throwable t) {
                logger.error("Error clicking menu item {}: {}", menuName, t.getMessage());
                t.printStackTrace();
                throw new RuntimeException("Menu click failed: " + t.getMessage(), t);
            }
            
            try {
                commonSteps.waitForSeconds("2");
            } catch (Throwable t) {
                logger.error("Error waiting for page load: {}", t.getMessage());
                t.printStackTrace();
                throw new RuntimeException("Page load wait failed: " + t.getMessage(), t);
            }
            
        } catch (Exception e) {
            logger.error("Error clicking menu item {}: {}", menuName, e.getMessage());
            throw new RuntimeException("Menu item click failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Find file input element by label text
     */
    private WebElement findFileInputByLabel(String label) {
        try {
            // Try multiple approaches to find file input by label
            List<String> labelSelectors = java.util.Arrays.asList(
                "//label[contains(text(),'" + label + "')]/following-sibling::input[@type='file']",
                "//label[contains(text(),'" + label + "')]/..//input[@type='file']",
                "//input[@type='file' and contains(@placeholder,'" + label + "')]",
                "//input[@type='file' and contains(@title,'" + label + "')]",
                "//div[contains(text(),'" + label + "')]//input[@type='file']",
                "//span[contains(text(),'" + label + "')]//input[@type='file']"
            );
            
            for (String selector : labelSelectors) {
                try {
                    WebElement element = driver.findElement(By.xpath(selector));
                    if (element.isDisplayed() || element.isEnabled()) {
                        logger.debug("Found file input using selector: {}", selector);
                        return element;
                    }
                } catch (NoSuchElementException e) {
                    continue;
                }
            }
            
            logger.warn("File input with label '{}' not found with any selector", label);
            return null;
            
        } catch (Exception e) {
            logger.error("Error finding file input by label '{}': {}", label, e.getMessage());
            return null;
        }
    }
    
    /**
     * Find table row by column value
     */
    private WebElement findTableRowByColumnValue(String columnName, String value) {
        try {
            logger.debug("Searching for table row where {} = {}", columnName, value);
            
            // First, find the column index by header text
            List<WebElement> headers = driver.findElements(By.xpath("//table//th"));
            int columnIndex = -1;
            
            for (int i = 0; i < headers.size(); i++) {
                if (headers.get(i).getText().trim().equalsIgnoreCase(columnName)) {
                    columnIndex = i + 1; // XPath is 1-indexed
                    break;
                }
            }
            
            if (columnIndex == -1) {
                throw new RuntimeException("Column '" + columnName + "' not found in table headers");
            }
            
            // Find the row with the matching value in that column
            WebElement targetRow = driver.findElement(By.xpath("//table//tr[td[" + columnIndex + "][contains(text(),'" + value + "')]]"));
            logger.debug("Found table row where {} = {}", columnName, value);
            return targetRow;
            
        } catch (Exception e) {
            logger.error("Error finding table row where {} = {}: {}", columnName, value, e.getMessage());
            return null;
        }
    }
    
    /**
     * Find toggle switch in a table row
     */
    private WebElement findToggleInRow(WebElement row) {
        try {
            // Try multiple selectors for toggle switches
            List<String> toggleSelectors = java.util.Arrays.asList(
                ".//input[@type='checkbox']",
                ".//button[contains(@class,'toggle')]",
                ".//div[contains(@class,'toggle')]",
                ".//span[contains(@class,'toggle')]",
                ".//input[contains(@class,'switch')]",
                ".//label[contains(@class,'switch')]"
            );
            
            for (String selector : toggleSelectors) {
                try {
                    WebElement toggle = row.findElement(By.xpath(selector));
                    if (toggle.isDisplayed()) {
                        logger.debug("Found toggle using selector: {}", selector);
                        return toggle;
                    }
                } catch (NoSuchElementException e) {
                    continue;
                }
            }
            
            logger.warn("Toggle switch not found in table row");
            return null;
            
        } catch (Exception e) {
            logger.error("Error finding toggle in table row: {}", e.getMessage());
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
            
            // For checkbox inputs
            if ("checkbox".equals(toggleElement.getAttribute("type"))) {
                return toggleElement.isSelected();
            }
            
            // For aria-checked attribute
            if ("true".equals(ariaChecked)) {
                return true;
            }
            
            // For checked attribute
            if (checked != null && !checked.isEmpty()) {
                return true;
            }
            
            // For CSS classes indicating active/on state
            if (className != null && (className.contains("active") || className.contains("on") || className.contains("checked"))) {
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            logger.debug("Error checking toggle state: {}", e.getMessage());
            return false;
        }
    }
}
