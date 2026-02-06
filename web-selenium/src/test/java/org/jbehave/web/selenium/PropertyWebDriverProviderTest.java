package org.jbehave.web.selenium;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class PropertyWebDriverProviderTest {

    private WebDriverProvider provider;

    @After
    public void after() {
        if (provider != null) {
            provider.get().close();
        }
    }

    @Test
    @Ignore("Only when Firefox is available")
    public void shouldSupportFirefoxByDefault() {
        createProviderForProperty(null);
        assertThat(provider.get(), instanceOf(FirefoxDriver.class));
    }

    @Test
    @Ignore("Only when Firefox is available")
    public void shouldSupportFirefoxByProperty() {
        createProviderForProperty("firefox");
        assertThat(provider.get(), instanceOf(FirefoxDriver.class));
    }

    @Test
    @Ignore("Only when Chrome is available")
    public void shouldSupportChromeByProperty() {
        createProviderForProperty("chrome");
        assertThat(provider.get(), instanceOf(ChromeDriver.class));
    }

    @Test
    @Ignore("Only when Edge is available")
    public void shouldSupportEdgeByProperty() {
        createProviderForProperty("edge");
        assertThat(provider.get(), instanceOf(EdgeDriver.class));
    }

    private void createProviderForProperty(String browser) {
        if (browser != null) {
            System.setProperty("browser", browser);
        }
        provider = new PropertyWebDriverProvider();
        provider.initialize();
    }

}
