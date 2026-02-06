package org.jbehave.web.selenium;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;

import java.net.MalformedURLException;
import java.net.URL;

/**
 *  Allows to connect to <a href="http://saucelabs.com/">Sauce Labs</a> to run
 *  Selenium tests in the cloud.  Requires Sauce credentials, username and access key, which
 *  can be provided via system properties "SAUCE_USERNAME" and "SAUCE_ACCESS_KEY".
 *
 *  Firefox is the default browser choice. This is done via Capabilities
 *  passed in through the constructor.
 */
public class SauceWebDriverProvider extends RemoteWebDriverProvider {

    /**
     * @param desiredCapabilities the desired capabilities
     */
    public SauceWebDriverProvider(Capabilities desiredCapabilities) {
        super(desiredCapabilities);
    }

    /**
     * With default capabilities and a selenium version specified in getSeleniumVersion()
     * @see RemoteWebDriverProvider#makeDesiredCapabilities
     * @see SauceWebDriverProvider#getSeleniumVersion()
     */
    public SauceWebDriverProvider() {
        super();
    }

    @Override
    protected MutableCapabilities makeDesiredCapabilities() {
        MutableCapabilities dc = super.makeDesiredCapabilities();
        dc.setCapability("name", "JBehave");
        dc.setCapability("selenium-version", getSeleniumVersion());
        dc.setCapability("max-duration", getMaxDuration());
        dc.setCapability("command-timeout", getCommandTimeout());
        dc.setCapability("idle-timeout", getIdleTimeout());
        return dc;
    }

    /**
     * Get selenium version from System property 'selenium.version' if there.
     * Use '4.40.0' if property not set.
     * @return Selenium version.
     */
    protected String getSeleniumVersion() {
        String seVersion = System.getProperty("selenium.version");
        if (seVersion == null) {
            return getDefaultSeleniumVersion();
        }
        return seVersion;
    }

    protected String getDefaultSeleniumVersion() {
        return "4.40.0";
    }

    @Override
    public URL createRemoteURL() throws MalformedURLException {
        return new URL("http://" + getSauceCredentials() + "@ondemand.saucelabs.com/wd/hub");
    }

    public static String getSauceUser() {
        String username = System.getProperty("SAUCE_USERNAME");
        if (username == null) {
            throw new UnsupportedOperationException("SAUCE_USERNAME property name variable not specified");
        }
        return username;
    }

    public static String getSauceAccessKey() {
        String access_key = System.getProperty("SAUCE_ACCESS_KEY");
        if (access_key == null) {
            throw new UnsupportedOperationException("SAUCE_ACCESS_KEY property name variable not specified");
        }
        return access_key;
    }

    public static String getSauceCredentials() {
        return getSauceUser() + ":" + getSauceAccessKey();
    }

    /**
     * Max duration of Job on Sauce Labs.  If you don't override this,
     * and have not set a value on system property 'SAUCE_MAX_DURATION',
     * 30 minutes is the default.
     * @return max duration in seconds
     */
    protected String getMaxDuration() {
        String maxDuration = System.getProperty("SAUCE_MAX_DURATION");
        if (maxDuration == null) {
            return "" + (30 * 60);
        }
        return maxDuration;
    }

    /**
     * Command Timeout for an individual command for a Job on Sauce Labs.
     * If you don't override this, and you don't specify a value on
     * a system property 'SAUCE_COMMAND_TIMEOUT', then 300 seconds is the default.
     * @return command timeout in seconds
     */
    protected String getCommandTimeout() {
        String commandTimeout = System.getProperty("SAUCE_COMMAND_TIMEOUT");
        if (commandTimeout == null) {
            return "300";
        }
        return commandTimeout;
    }

    /**
     * Idle Timeout for a Job on Sauce Labs.
     * If you don't override this, and don't specify a value on the system
     * property 'SAUCE_IDLE_TIMEOUT', then 90 seconds is the default.
     * @return command timeout in seconds
     */
    protected String getIdleTimeout() {
        String idleTimeout = System.getProperty("SAUCE_IDLE_TIMEOUT");
        if (idleTimeout == null) {
            return "90";
        }
        return idleTimeout;
    }

}
