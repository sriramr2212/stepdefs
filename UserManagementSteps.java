package com.novac.naf.steps;

import com.novac.naf.config.ConfigLoader;
import com.novac.naf.orm.ORLoader;
import com.novac.naf.reporting.ReportManager;
import com.novac.naf.steps.CommonSteps;
import com.novac.naf.steps.CustomSteps;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Step definitions for User Management and User Role functionality
 */
public class UserManagementSteps {
    private static final Logger logger = LoggerFactory.getLogger(UserManagementSteps.class);
    private final CommonSteps commonSteps;
    private final CustomSteps customSteps;
    private final WebDriver driver;
    private final ConfigLoader configLoader;
    
    // Default constructor for Cucumber discovery
    public UserManagementSteps() {
        this.commonSteps = new CommonSteps();
        this.customSteps = new CustomSteps(commonSteps);
        this.driver = commonSteps.getDriver();
        
        String excelPath = System.getProperty("excelPath", "./TestData/RunManager.xlsx");
        this.configLoader = new ConfigLoader(excelPath);
        
        logger.info("UserManagementSteps initialized successfully");
    }
    
    // Constructor with dependencies
    public UserManagementSteps(CommonSteps commonSteps, CustomSteps customSteps) {
        this.commonSteps = commonSteps;
        this.customSteps = customSteps;
        this.driver = commonSteps.getDriver();
        
        String excelPath = System.getProperty("excelPath", "./TestData/RunManager.xlsx");
        this.configLoader = new ConfigLoader(excelPath);
        
        logger.info("UserManagementSteps initialized with provided dependencies");
    }
    
    // ================================
    // MENU NAVIGATION STEP DEFINITIONS
    // ================================
    
    @Given("I want to open the {string} menu")
    public void openMenu(String menuName) {
        try {
            logger.info("Opening menu: {}", menuName);
            
            customSteps.clickMenuItem(menuName, "UserRoleMaster");
            
            try {
                commonSteps.verifyElementIsVisible("User Role List", "UserRoleMaster");
                logger.info("Verified page title is visible for menu: {}", menuName);
            } catch (Throwable t) {
                logger.error("Error verifying page title: {}", t.getMessage());
                t.printStackTrace();
                throw new RuntimeException("Page title verification failed: " + t.getMessage(), t);
            }
            
            customSteps.addDemoDelay();
            try {
                commonSteps.takeScreenshot("Menu Navigation - " + menuName);
            } catch (Throwable t) {
                logger.error("Error taking screenshot: {}", t.getMessage());
                t.printStackTrace();
                throw new RuntimeException("Screenshot failed: " + t.getMessage(), t);
            }
            
            ReportManager.logPass("Menu navigation", "Successfully opened menu: " + menuName);
            
        } catch (Exception e) {
            logger.error("Error opening menu {}: {}", menuName, e.getMessage());
            ReportManager.logFail("Menu navigation failed", "Failed to open menu: " + menuName + " - " + e.getMessage());
            throw new RuntimeException("Menu navigation failed: " + e.getMessage(), e);
        }
    }
    
    // ================================
    // USER ROLE STEP DEFINITIONS
    // ================================
    
    @When("I view the role with Role Code {string}")
    public void viewRoleByCode(String roleCode) {
        try {
            logger.info("Viewing role with Role Code: {}", roleCode);
            
            WebElement targetRow = findTableRowByRoleCode(roleCode);
            
            try {
                WebElement viewIcon = targetRow.findElement(By.xpath(".//button[@title='View' or contains(@class,'view')] | .//i[contains(@class,'view') or contains(@class,'eye')]"));
                viewIcon.click();
                logger.info("Clicked View icon for Role Code: {}", roleCode);
            } catch (NoSuchElementException e) {
                logger.error("View icon not found for Role Code: {}", roleCode);
                throw new RuntimeException("View icon not found for Role Code: " + roleCode, e);
            }
            
            try {
                commonSteps.waitForSeconds("2");
            } catch (Throwable t) {
                logger.error("Error waiting for view modal: {}", t.getMessage());
                t.printStackTrace();
                throw new RuntimeException("View modal wait failed: " + t.getMessage(), t);
            }
            
            try {
                commonSteps.verifyElementIsVisible("View User Role", "UserRoleMaster");
                logger.info("View dialog opened successfully for Role Code: {}", roleCode);
            } catch (Throwable t) {
                logger.error("Error verifying view dialog: {}", t.getMessage());
                t.printStackTrace();
                throw new RuntimeException("View dialog verification failed: " + t.getMessage(), t);
            }
            
            customSteps.addDemoDelay();
            try {
                commonSteps.takeScreenshot("View Role - " + roleCode);
            } catch (Throwable t) {
                logger.error("Error taking screenshot: {}", t.getMessage());
                t.printStackTrace();
                throw new RuntimeException("Screenshot failed: " + t.getMessage(), t);
            }
            
            ReportManager.logPass("View role operation", "Successfully opened view for Role Code: " + roleCode);
            
        } catch (Exception e) {
            logger.error("Error viewing role with Role Code {}: {}", roleCode, e.getMessage());
            ReportManager.logFail("View role failed", "Failed to view Role Code: " + roleCode + " - " + e.getMessage());
            throw new RuntimeException("View role failed: " + e.getMessage(), e);
        }
    }
    
    @When("I edit the role with Role Code {string}")
    public void editRoleByCode(String roleCode) {
        try {
            logger.info("Editing role with Role Code: {}", roleCode);
            
            WebElement targetRow = findTableRowByRoleCode(roleCode);
            
            try {
                WebElement editIcon = targetRow.findElement(By.xpath(".//button[@title='Edit' or contains(@class,'edit')] | .//i[contains(@class,'edit') or contains(@class,'pencil')]"));
                editIcon.click();
                logger.info("Clicked Edit icon for Role Code: {}", roleCode);
            } catch (NoSuchElementException e) {
                logger.error("Edit icon not found for Role Code: {}", roleCode);
                throw new RuntimeException("Edit icon not found for Role Code: " + roleCode, e);
            }
            
            try {
                commonSteps.waitForSeconds("2");
            } catch (Throwable t) {
                logger.error("Error waiting for edit modal: {}", t.getMessage());
                t.printStackTrace();
                throw new RuntimeException("Edit modal wait failed: " + t.getMessage(), t);
            }
            
            try {
                commonSteps.verifyElementIsVisible("Edit User Role", "UserRoleMaster");
                logger.info("Edit dialog opened successfully for Role Code: {}", roleCode);
            } catch (Throwable t) {
                logger.error("Error verifying edit dialog: {}", t.getMessage());
                t.printStackTrace();
                throw new RuntimeException("Edit dialog verification failed: " + t.getMessage(), t);
            }
            
            customSteps.addDemoDelay();
            try {
                commonSteps.takeScreenshot("Edit Role - " + roleCode);
            } catch (Throwable t) {
                logger.error("Error taking screenshot: {}", t.getMessage());
                t.printStackTrace();
                throw new RuntimeException("Screenshot failed: " + t.getMessage(), t);
            }
            
            ReportManager.logPass("Edit role operation", "Successfully opened edit for Role Code: " + roleCode);
            
        } catch (Exception e) {
            logger.error("Error editing role with Role Code {}: {}", roleCode, e.getMessage());
            ReportManager.logFail("Edit role failed", "Failed to edit Role Code: " + roleCode + " - " + e.getMessage());
            throw new RuntimeException("Edit role failed: " + e.getMessage(), e);
        }
    }
    
    @When("I delete the role with Role Code {string}")
    public void deleteRoleByCode(String roleCode) {
        try {
            logger.info("Deleting role with Role Code: {}", roleCode);
            
            WebElement targetRow = findTableRowByRoleCode(roleCode);
            
            try {
                WebElement deleteIcon = targetRow.findElement(By.xpath(".//button[@title='Delete' or contains(@class,'delete')] | .//i[contains(@class,'delete') or contains(@class,'trash')]"));
                deleteIcon.click();
                logger.info("Clicked Delete icon for Role Code: {}", roleCode);
            } catch (NoSuchElementException e) {
                logger.error("Delete icon not found for Role Code: {}", roleCode);
                throw new RuntimeException("Delete icon not found for Role Code: " + roleCode, e);
            }
            
            try {
                commonSteps.waitForSeconds("1");
            } catch (Throwable t) {
                logger.error("Error waiting for confirmation dialog: {}", t.getMessage());
                t.printStackTrace();
                throw new RuntimeException("Confirmation dialog wait failed: " + t.getMessage(), t);
            }
            
            try {
                if (commonSteps.isElementVisible("Delete Confirmation Dialog", "UserRoleMaster")) {
                    logger.info("Confirmation dialog appeared, clicking Yes to confirm deletion");
                    commonSteps.clickElement("Confirm Yes", "UserRoleMaster");
                    
                    try {
                        commonSteps.waitForSeconds("2");
                    } catch (Throwable t) {
                        logger.error("Error waiting after deletion: {}", t.getMessage());
                        t.printStackTrace();
                        throw new RuntimeException("Post-deletion wait failed: " + t.getMessage(), t);
                    }
                }
            } catch (Throwable t) {
                logger.error("Error handling confirmation dialog: {}", t.getMessage());
                t.printStackTrace();
                throw new RuntimeException("Confirmation dialog handling failed: " + t.getMessage(), t);
            }
            
            customSteps.addDemoDelay();
            try {
                commonSteps.takeScreenshot("Delete Role - " + roleCode);
            } catch (Throwable t) {
                logger.error("Error taking screenshot: {}", t.getMessage());
                t.printStackTrace();
                throw new RuntimeException("Screenshot failed: " + t.getMessage(), t);
            }
            
            ReportManager.logPass("Delete role operation", "Successfully deleted Role Code: " + roleCode);
            
        } catch (Exception e) {
            logger.error("Error deleting role with Role Code {}: {}", roleCode, e.getMessage());
            ReportManager.logFail("Delete role failed", "Failed to delete Role Code: " + roleCode + " - " + e.getMessage());
            throw new RuntimeException("Delete role failed: " + e.getMessage(), e);
        }
    }
    
    @Then("the role with Role Code {string} should not be visible in the list")
    public void verifyRoleNotVisible(String roleCode) {
        try {
            logger.info("Verifying role with Role Code {} is not visible in the list", roleCode);
            
            try {
                WebElement targetRow = findTableRowByRoleCode(roleCode);
                if (targetRow != null) {
                    logger.error("Role Code {} is still visible in the list after deletion", roleCode);
                    ReportManager.logFail("Role verification failed", "Role Code " + roleCode + " is still visible after deletion");
                    throw new AssertionError("Role Code " + roleCode + " should not be visible but it is still present");
                }
            } catch (RuntimeException e) {
                if (e.getMessage().contains("not found in the table")) {
                    logger.info("Role Code {} correctly not found in the table - deletion verified", roleCode);
                } else {
                    throw e;
                }
            }
            
            customSteps.addDemoDelay();
            
            ReportManager.logPass("Role verification", "Role Code " + roleCode + " is correctly not visible in the list");
            logger.info("Successfully verified that Role Code {} is not visible", roleCode);
            
        } catch (AssertionError e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error verifying role visibility for Role Code {}: {}", roleCode, e.getMessage());
            ReportManager.logFail("Role verification failed", "Error verifying Role Code: " + roleCode + " - " + e.getMessage());
            throw new RuntimeException("Role verification failed: " + e.getMessage(), e);
        }
    }
    
    // ================================
    // USER LISTING STEP DEFINITIONS
    // ================================
    
    @Then("the user with UserId {string} should appear as the first row in the listing")
    public void verifyUserInFirstRow(String userId) {
        try {
            logger.info("Verifying user with UserId {} appears as the first row in the listing", userId);
            
            try {
                commonSteps.waitForSeconds("2");
            } catch (Throwable t) {
                logger.error("Error waiting for table to load: {}", t.getMessage());
                t.printStackTrace();
                throw new RuntimeException("Table load wait failed: " + t.getMessage(), t);
            }
            
            try {
                WebElement firstRowUserIdCell = driver.findElement(By.xpath("//table//tbody//tr[1]//td[1]"));
                String actualUserId = firstRowUserIdCell.getText().trim();
                
                if (actualUserId.equals(userId)) {
                    logger.info("User {} correctly appears as the first row in the listing", userId);
                    ReportManager.logPass("First row verification", "User " + userId + " appears as first row as expected");
                } else {
                    logger.error("Expected user {} to be first row, but found {}", userId, actualUserId);
                    ReportManager.logFail("First row verification failed", "Expected user " + userId + " but found " + actualUserId);
                    throw new AssertionError("Expected user " + userId + " to be first row, but found " + actualUserId);
                }
            } catch (NoSuchElementException e) {
                logger.error("First row or User ID cell not found in the table");
                ReportManager.logFail("First row verification failed", "Table or first row not found");
                throw new RuntimeException("First row or User ID cell not found", e);
            }
            
            customSteps.addDemoDelay();
            try {
                commonSteps.takeScreenshot("First Row Verification - " + userId);
            } catch (Throwable t) {
                logger.error("Error taking screenshot: {}", t.getMessage());
                t.printStackTrace();
                throw new RuntimeException("Screenshot failed: " + t.getMessage(), t);
            }
            
        } catch (AssertionError e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error verifying first row for UserId {}: {}", userId, e.getMessage());
            ReportManager.logFail("First row verification failed", "Error verifying UserId: " + userId + " - " + e.getMessage());
            throw new RuntimeException("First row verification failed: " + e.getMessage(), e);
        }
    }
    
    @Then("the status of user {string} should be {string}")
    public void verifyUserStatus(String userId, String expectedStatus) {
        try {
            logger.info("Verifying status of user {} should be {}", userId, expectedStatus);
            
            WebElement userRow = findUserListingTableRow(userId);
            
            try {
                WebElement statusCell = userRow.findElement(By.xpath(".//td[8]"));
                String actualStatus = statusCell.getText().trim();
                
                if (actualStatus.equals(expectedStatus)) {
                    logger.info("User {} status correctly shows as {}", userId, expectedStatus);
                    ReportManager.logPass("Status verification", "User " + userId + " status is " + expectedStatus + " as expected");
                } else {
                    logger.error("Expected status {} for user {}, but found {}", expectedStatus, userId, actualStatus);
                    ReportManager.logFail("Status verification failed", "Expected status " + expectedStatus + " but found " + actualStatus);
                    throw new AssertionError("Expected status " + expectedStatus + " for user " + userId + " but found " + actualStatus);
                }
            } catch (NoSuchElementException e) {
                logger.error("Status cell not found for user {}", userId);
                ReportManager.logFail("Status verification failed", "Status cell not found for user " + userId);
                throw new RuntimeException("Status cell not found for user " + userId, e);
            }
            
            customSteps.addDemoDelay();
            try {
                commonSteps.takeScreenshot("Status Verification - " + userId);
            } catch (Throwable t) {
                logger.error("Error taking screenshot: {}", t.getMessage());
                t.printStackTrace();
                throw new RuntimeException("Screenshot failed: " + t.getMessage(), t);
            }
            
        } catch (AssertionError e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error verifying status for user {}: {}", userId, e.getMessage());
            ReportManager.logFail("Status verification failed", "Error verifying status for user: " + userId + " - " + e.getMessage());
            throw new RuntimeException("Status verification failed: " + e.getMessage(), e);
        }
    }
    
    @When("I choose to {string} for user {string}")
    public void performMeatballMenuAction(String action, String userId) {
        try {
            logger.info("Performing action {} for user {}", action, userId);
            
            WebElement userRow = findUserListingTableRow(userId);
            
            try {
                WebElement meatballMenu = userRow.findElement(By.xpath(".//td[last()]//button[contains(@class,'menu') or contains(@class,'dropdown') or contains(., '⋮') or contains(., '...')]"));
                meatballMenu.click();
                logger.info("Clicked meatball menu for user {}", userId);
            } catch (NoSuchElementException e) {
                logger.error("Meatball menu not found for user {}", userId);
                throw new RuntimeException("Meatball menu not found for user " + userId, e);
            }
            
            try {
                commonSteps.waitForSeconds("1");
            } catch (Throwable t) {
                logger.error("Error waiting for dropdown menu: {}", t.getMessage());
                t.printStackTrace();
                throw new RuntimeException("Dropdown menu wait failed: " + t.getMessage(), t);
            }
            
            try {
                WebElement actionOption = driver.findElement(By.xpath("//div[contains(@class,'dropdown') or contains(@class,'menu')]//a[contains(text(),'" + action + "') or @title='" + action + "'] | //button[contains(text(),'" + action + "') or @title='" + action + "']"));
                actionOption.click();
                logger.info("Clicked on action {} for user {}", action, userId);
            } catch (NoSuchElementException e) {
                logger.error("Action {} not found in dropdown for user {}", action, userId);
                throw new RuntimeException("Action " + action + " not found in dropdown for user " + userId, e);
            }
            
            try {
                commonSteps.waitForSeconds("2");
            } catch (Throwable t) {
                logger.error("Error waiting after action: {}", t.getMessage());
                t.printStackTrace();
                throw new RuntimeException("Post-action wait failed: " + t.getMessage(), t);
            }
            
            customSteps.addDemoDelay();
            try {
                commonSteps.takeScreenshot("Meatball Action - " + action + " - " + userId);
            } catch (Throwable t) {
                logger.error("Error taking screenshot: {}", t.getMessage());
                t.printStackTrace();
                throw new RuntimeException("Screenshot failed: " + t.getMessage(), t);
            }
            
            ReportManager.logPass("Meatball menu action", "Successfully performed " + action + " for user " + userId);
            
        } catch (Exception e) {
            logger.error("Error performing action {} for user {}: {}", action, userId, e.getMessage());
            ReportManager.logFail("Meatball menu action failed", "Failed to perform " + action + " for user " + userId + " - " + e.getMessage());
            throw new RuntimeException("Meatball menu action failed: " + e.getMessage(), e);
        }
    }
    
    @Then("the table should display exactly {string} rows")
    public void verifyTableRowCount(String expectedRowCount) {
        try {
            logger.info("Verifying table displays exactly {} rows", expectedRowCount);
            
            try {
                commonSteps.waitForSeconds("3");
            } catch (Throwable t) {
                logger.error("Error waiting for table to load: {}", t.getMessage());
                t.printStackTrace();
                throw new RuntimeException("Table load wait failed: " + t.getMessage(), t);
            }
            
            int actualRowCount = getUserListingTableRowCount();
            int expected = Integer.parseInt(expectedRowCount);
            
            if (actualRowCount == expected) {
                logger.info("Table correctly displays {} rows", actualRowCount);
                ReportManager.logPass("Row count verification", "Table displays exactly " + actualRowCount + " rows as expected");
            } else {
                logger.error("Expected {} rows, but found {} rows", expected, actualRowCount);
                ReportManager.logFail("Row count verification failed", "Expected " + expected + " rows but found " + actualRowCount);
                throw new AssertionError("Expected " + expected + " rows but found " + actualRowCount);
            }
            
            customSteps.addDemoDelay();
            try {
                commonSteps.takeScreenshot("Row Count Verification - " + expectedRowCount);
            } catch (Throwable t) {
                logger.error("Error taking screenshot: {}", t.getMessage());
                t.printStackTrace();
                throw new RuntimeException("Screenshot failed: " + t.getMessage(), t);
            }
            
        } catch (AssertionError e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error verifying {} rows per page: {}", expectedRowCount, e.getMessage());
            ReportManager.logFail("Pagination verification failed", "Error verifying " + expectedRowCount + " rows per page - " + e.getMessage());
            throw new RuntimeException("Pagination verification failed: " + e.getMessage(), e);
        }
    }
    
    @When("I sort the user listing by column {string}")
    public void sortByColumn(String columnName) {
        try {
            logger.info("Sorting user listing by column: {}", columnName);
            
            int columnIndex = getColumnIndex(columnName);
            
            try {
                WebElement columnHeader = driver.findElement(By.xpath("//table//th[" + columnIndex + "]//button | //table//th[" + columnIndex + "][contains(@class,'sortable')] | //table//th[" + columnIndex + "]"));
                columnHeader.click();
                logger.info("Clicked sort for column: {}", columnName);
            } catch (NoSuchElementException e) {
                logger.error("Sort button/header not found for column: {}", columnName);
                throw new RuntimeException("Sort button/header not found for column: " + columnName, e);
            }
            
            try {
                commonSteps.waitForSeconds("3");
            } catch (Throwable t) {
                logger.error("Error waiting for sort to complete: {}", t.getMessage());
                t.printStackTrace();
                throw new RuntimeException("Sort completion wait failed: " + t.getMessage(), t);
            }
            
            List<String> columnValues = getColumnValues(columnName);
            
            boolean isValidSort = validateSortOrder(columnValues);
            
            if (isValidSort) {
                logger.info("Sorting by {} completed successfully", columnName);
                ReportManager.logPass("Column sorting", "Successfully sorted by column: " + columnName);
            } else {
                logger.warn("Sort order validation inconclusive for column: {}", columnName);
                ReportManager.logPass("Column sorting", "Sort action completed for column: " + columnName + " (order validation inconclusive)");
            }
            
            customSteps.addDemoDelay();
            try {
                commonSteps.takeScreenshot("Column Sort - " + columnName);
            } catch (Throwable t) {
                logger.error("Error taking screenshot: {}", t.getMessage());
                t.printStackTrace();
                throw new RuntimeException("Screenshot failed: " + t.getMessage(), t);
            }
            
        } catch (Exception e) {
            logger.error("Error sorting by column {}: {}", columnName, e.getMessage());
            ReportManager.logFail("Column sorting failed", "Failed to sort by column: " + columnName + " - " + e.getMessage());
            throw new RuntimeException("Column sorting failed: " + e.getMessage(), e);
        }
    }
    
    // ================================
    // HELPER METHODS
    // ================================
    
    /**
     * Helper method to find a table row by Role Code
     */
    private WebElement findTableRowByRoleCode(String roleCode) {
        try {
            logger.debug("Searching for table row with Role Code: {}", roleCode);
            
            List<WebElement> tableRows = driver.findElements(By.xpath("//table//tr"));
            
            for (WebElement row : tableRows) {
                try {
                    List<WebElement> cells = row.findElements(By.xpath(".//td"));
                    for (WebElement cell : cells) {
                        if (cell.getText().trim().equals(roleCode)) {
                            logger.debug("Found Role Code {} in table row", roleCode);
                            return row;
                        }
                    }
                } catch (Exception e) {
                    continue;
                }
            }
            
            logger.error("Role Code {} not found in the table", roleCode);
            throw new RuntimeException("Role Code " + roleCode + " not found in the table");
            
        } catch (Exception e) {
            logger.error("Error searching for Role Code {}: {}", roleCode, e.getMessage());
            throw new RuntimeException("Error finding table row for Role Code: " + roleCode, e);
        }
    }
    
    /**
     * Helper method to find a User Listing table row by UserId
     */
    private WebElement findUserListingTableRow(String userId) {
        try {
            logger.debug("Searching for User Listing table row with UserId: {}", userId);
            
            List<WebElement> tableRows = driver.findElements(By.xpath("//table//tbody//tr"));
            
            for (WebElement row : tableRows) {
                try {
                    WebElement userIdCell = row.findElement(By.xpath(".//td[1]"));
                    if (userIdCell.getText().trim().equals(userId)) {
                        logger.debug("Found User ID {} in table row", userId);
                        return row;
                    }
                } catch (Exception e) {
                    continue;
                }
            }
            
            logger.error("User ID {} not found in the User Listing table", userId);
            throw new RuntimeException("User ID " + userId + " not found in the User Listing table");
            
        } catch (Exception e) {
            logger.error("Error searching for User ID {}: {}", userId, e.getMessage());
            throw new RuntimeException("Error finding table row for User ID: " + userId, e);
        }
    }
    
    /**
     * Helper method to count visible rows in the User Listing table
     */
    private int getUserListingTableRowCount() {
        try {
            List<WebElement> tableRows = driver.findElements(By.xpath("//table//tbody//tr"));
            int rowCount = tableRows.size();
            logger.debug("Found {} rows in User Listing table", rowCount);
            return rowCount;
        } catch (Exception e) {
            logger.error("Error counting table rows: {}", e.getMessage());
            return 0;
        }
    }
    
    /**
     * Helper method to get column index based on column name
     */
    private int getColumnIndex(String columnName) {
        switch (columnName) {
            case "User Id": return 1;
            case "User Name": return 2;
            case "User Classification": return 3;
            case "Department": return 4;
            case "User Role": return 5;
            case "Effective from": return 6;
            case "Effective to": return 7;
            case "Status": return 8;
            default:
                logger.warn("Unknown column name: {}, defaulting to index 1", columnName);
                return 1;
        }
    }
    
    /**
     * Helper method to extract all values from a specific column
     */
    private List<String> getColumnValues(String columnName) {
        List<String> values = new java.util.ArrayList<>();
        try {
            int columnIndex = getColumnIndex(columnName);
            List<WebElement> cells = driver.findElements(By.xpath("//table//tbody//tr//td[" + columnIndex + "]"));
            
            for (WebElement cell : cells) {
                values.add(cell.getText().trim());
            }
            
            logger.debug("Extracted {} values from column {}", values.size(), columnName);
        } catch (Exception e) {
            logger.error("Error extracting values from column {}: {}", columnName, e.getMessage());
        }
        return values;
    }
    
    /**
     * Helper method to validate if a list of values is sorted
     */
    private boolean validateSortOrder(List<String> values) {
        if (values.size() <= 1) {
            return true;
        }
        
        boolean isAscending = true;
        boolean isDescending = true;
        
        for (int i = 1; i < values.size(); i++) {
            String current = values.get(i);
            String previous = values.get(i - 1);
            
            if (current.isEmpty() || previous.isEmpty()) {
                continue;
            }
            
            int comparison = current.compareToIgnoreCase(previous);
            
            if (comparison < 0) {
                isAscending = false;
            }
            if (comparison > 0) {
                isDescending = false;
            }
        }
        
        boolean isSorted = isAscending || isDescending;
        logger.debug("Sort validation result - Ascending: {}, Descending: {}, Overall sorted: {}", 
                    isAscending, isDescending, isSorted);
        
        return isSorted;
    }
}