package org.jbehave.web.selenium;

import org.jbehave.core.configuration.Configuration;

/**
 * Extends Configuration to provide Selenium-based components
 * using the WebDriver API.
 */
public class SeleniumConfiguration extends Configuration {

    private SeleniumContext seleniumContext;
    private WebDriverProvider driverProvider;

    public SeleniumConfiguration() {
    }

    public SeleniumContext seleniumContext() {
        synchronized (this) {
            if (seleniumContext == null) {
                seleniumContext = new SeleniumContext();
            }
        }
        return seleniumContext;
    }

    public SeleniumConfiguration useSeleniumContext(SeleniumContext seleniumContext) {
        this.seleniumContext = seleniumContext;
        return this;
    }

    public WebDriverProvider webDriverProvider() {
        return driverProvider;
    }

    public SeleniumConfiguration useWebDriverProvider(WebDriverProvider webDriverProvider){
        this.driverProvider = webDriverProvider;
        return this;
    }

}
