# Framework Name: Novac Automation Framework
# Author: STATIM2 QA Team
# File Name: LoginTest.feature
# Description: Test script to verify login functionality

Feature: Application Login Feature
  As a user
  I want to verify the login page elements
  And login to the application

  @LoginTest @STATIMCM-TC-459
  Scenario: Verify login page elements and login functionality
    Given I navigate to "LoginPage.ApplicationURL"
    #Then I verify page title is "LoginPage.LoginPageTitle"
    
    # Verify all elements exist and are enabled abd then enter test data
    Then I should see "customInputUsername" on "LoginPage" page
    Then I verify element "customInputUsername" is enabled on "LoginPage" page
    And I enter "LoginPage.Username" in "customInputUsername" field on "LoginPage" page 

    Then I should see "customInputPassword" on "LoginPage" page
    Then I verify element "customInputPassword" is enabled on "LoginPage" page
    And I enter "LoginPage.Password" in "customInputPassword" field on "LoginPage" page
    
    Then I should see "login-btn" on "LoginPage" page
    Then I verify element "login-btn" is enabled on "LoginPage" page
    And I click on "login-btn" on "LoginPage" page
    
    # Verify successful login
    #Then I verify page title is "LoginPage.DashboardPageTitle"
    Then Take screenshot "LoginSuccess"