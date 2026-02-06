package org.jbehave.web.selenium;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.CommandExecutor;
import org.openqa.selenium.remote.DriverCommand;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.Response;

/**
 * <p>
 * Provides a {@link RemoteWebDriver} that connects to a URL specified by system
 * property "REMOTE_WEBDRIVER_URL".
 * </p>
 * <p>
 * The default {@link Capabilities}, specified by
 * {@link #makeDesiredCapabilities()}, are for Firefox.
 * </p>
 */
public class RemoteWebDriverProvider extends DelegatingWebDriverProvider {

    private final Capabilities desiredCapabilities;
    private boolean verbose = false;

    /**
     * With default capabilities
     * @see RemoteWebDriverProvider#makeDesiredCapabilities()
     */
    public RemoteWebDriverProvider() {
        this(null);
    }

    /**
     * Default Desired Capabilities: Firefox, unless something is specified
     * via a system-property "browser.version".
     * @return a Capabilities matching the above.
     */
    protected MutableCapabilities makeDesiredCapabilities() {
        MutableCapabilities capabilities = new MutableCapabilities();
        capabilities.setCapability("browserName", "firefox");
        String browserVersion = getBrowserVersion();
        if (browserVersion != null) {
            capabilities.setCapability("browserVersion", browserVersion);
        }
        return capabilities;
    }

    /**
     * Get the default browser version for use on the Remote system.
     * @return null or whatever you have specified on system property 'browser.version'
     */
    protected String getBrowserVersion() {
        return System.getProperty("browser.version");
    }

    public RemoteWebDriverProvider(Capabilities desiredCapabilities) {
        if (desiredCapabilities == null) {
            this.desiredCapabilities = makeDesiredCapabilities();
        } else {
            this.desiredCapabilities = desiredCapabilities;
        }
    }

    public void initialize() {
        URL url = null;
        WebDriver remoteWebDriver;
        try {
            url = createRemoteURL();
            remoteWebDriver = new ScreenshootingRemoteWebDriver(wrapCommandExecutor(new HttpCommandExecutor(url)), desiredCapabilities);
        } catch (Throwable e) {
            if (verbose) {
                System.err.println("*********** Remote WebDriver Initialization Failure ************");
                e.printStackTrace(System.err);
            }
            throw new UnsupportedOperationException("Connecting to remote URL '" + url + "' failed: " + e.getMessage(),
                    e);
        }
        delegate.set(remoteWebDriver);
    }

    /**
     * Override this to instrument CommandExecutor
     * @return a CommandExecutor instance.
     * @param commandExecutor a CommandExecutor that communicates over the wire.
     */
    protected CommandExecutor wrapCommandExecutor(CommandExecutor commandExecutor) {
        return commandExecutor;
    }

    public URL createRemoteURL() throws MalformedURLException {
        String url = System.getProperty("REMOTE_WEBDRIVER_URL");
        if (url == null) {
            throw new UnsupportedOperationException("REMOTE_WEBDRIVER_URL property not specified");
        }
        return new URL(url);
    }

    static class ScreenshootingRemoteWebDriver extends RemoteWebDriver {

        private boolean sauceJobEnded = false;

        public ScreenshootingRemoteWebDriver(CommandExecutor commandExecutor, Capabilities capabilities) {
            super(commandExecutor, capabilities);
        }

        @Override
        protected Response execute(String driverCommand, Map<String, ?> parameters) {
            if (sauceJobEnded) {
                throw new SauceLabsJobHasEnded();
            }
            try {
                return super.execute(driverCommand, parameters);
            } catch (WebDriverException e) {
                if (e.getMessage().indexOf("Job on Sauce is already complete") > -1) {
                    sauceJobEnded = true;
                    throw new SauceLabsJobHasEnded();
                }
                throw e;
            } catch (RuntimeException e) {
                throw e;
            }
        }
    }

    public static class SauceLabsJobHasEnded extends WebDriverException {
        public SauceLabsJobHasEnded() {
            super("SauceLabs job has ended.  It may have timed-out previously.  Not even screen-shots, " +
                    "after-scenario or after-story steps are possible after this for this WebDriver instance");
        }
    }

    public void useVerbosity(boolean verbose) {
        this.verbose = verbose;
    }
}
