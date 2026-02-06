package org.jbehave.web.selenium;

import java.util.Locale;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

/**
 * Provides WebDriver instances based on system property "browser":
 * <ul>
 * <li>"chrome": {@link ChromeDriver}</li>
 * <li>"edge": {@link EdgeDriver}</li>
 * <li>"firefox": {@link FirefoxDriver}</li>
 * </ul>
 * Property values are case-insensitive and defaults to "firefox" if no
 * "browser" system property is found.
 */
public class PropertyWebDriverProvider extends DelegatingWebDriverProvider {

    public enum Browser {
        CHROME, EDGE, FIREFOX
    }

    public void initialize() {
        Browser browser = Browser.valueOf(Browser.class, System.getProperty("browser", "firefox").toUpperCase(usingLocale()));
        delegate.set(createDriver(browser));
    }

    private WebDriver createDriver(Browser browser) {
        switch (browser) {
        case CHROME:
            return createChromeDriver();
        case EDGE:
            return createEdgeDriver();
        case FIREFOX:
        default:
            return createFirefoxDriver();
        }
    }

    protected ChromeDriver createChromeDriver() {
        return new ChromeDriver();
    }

    protected EdgeDriver createEdgeDriver() {
        return new EdgeDriver();
    }

    protected FirefoxDriver createFirefoxDriver() {
        return new FirefoxDriver();
    }

    protected Locale usingLocale() {
        return Locale.getDefault();
    }

}
