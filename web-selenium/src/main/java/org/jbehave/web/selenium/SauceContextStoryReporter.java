package org.jbehave.web.selenium;

import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.steps.Timing;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;

import static org.jbehave.web.selenium.SauceWebDriverProvider.getSauceAccessKey;
import static org.jbehave.web.selenium.SauceWebDriverProvider.getSauceUser;

/**
 * A StoryReporter that passes back to SauceLabs the executed job results.
 */
public class SauceContextStoryReporter extends SeleniumContextStoryReporter {

    private final WebDriverProvider webDriverProvider;

    private ThreadLocal<String> storyName = new ThreadLocal<String>();
    private ThreadLocal<SessionId> sessionIds = new ThreadLocal<SessionId>();
    private ThreadLocal<Boolean> passed = new ThreadLocal<Boolean>();

    private Map<String, String> storyToJobIds = new HashMap<String, String>();

    private static final Pattern SAUCE_LABS_VIDEO_URL_PATTERN = Pattern.compile("http.*\\.flv");

    public SauceContextStoryReporter(WebDriverProvider webDriverProvider, SeleniumContext seleniumContext, java.util.Map<String, String> storyToSauceUrlMap) {
        super(seleniumContext);
        this.webDriverProvider = webDriverProvider;
        this.storyToJobIds = storyToSauceUrlMap;
    }

    @Override
    public void beforeStory(Story story, boolean givenStory) {
        storyName.set(story.getPath());
        passed.set(true);
    }

    @Override
    public void beforeScenario(Scenario scenario) {
        try {
            super.beforeScenario(scenario);
            String title = scenario.getTitle();
            ((JavascriptExecutor) webDriverProvider.get()).executeScript("sauce:context=Scenario: " + title);
            sessionIds.set(((RemoteWebDriver) webDriverProvider.get()).getSessionId());
            String payload = "{\"tags\":[" + getJobTags() + "], " + getBuildId() + "\"name\":\" " + getJobName() + "\"}";
            postJobUpdate(storyName.get(), sessionIds.get(), payload);
        } catch (WebDriverException e) {
            if (e.getMessage().startsWith("Error communicating with the remote browser. It may have died.")) {
                // do nothing, it's likely that SauceLabs timed out the job on their system.
            } else {
                throw e;
            }
        }
    }

    @Override
    public void failed(String step, Throwable cause) {
        try {
            passed.set(false);
            ((JavascriptExecutor) webDriverProvider.get()).executeScript("sauce:context=(Scenario failed)");
        } catch (RemoteWebDriverProvider.SauceLabsJobHasEnded e) {
            // do nothing.
        }
    }

    @Override
    public void pending(String step) {
        try {
            ((JavascriptExecutor) webDriverProvider.get()).executeScript("sauce:context=(Pending Steps Encountered: '"
                    + step + "', " + "No More Steps Processed)");
        } catch (WebDriverException e) {
            if (e.getMessage().startsWith("Error communicating with the remote browser. It may have died.")) {
                // do nothing, it's likely that SauceLabs timed out the job on their system.
            } else {
                throw e;
            }
        }
    }

    @Override
    public void afterScenario(Timing timing) {
        String as = "(After Scenario Steps, if any...)";
        try {
            ((JavascriptExecutor) webDriverProvider.get()).executeScript("sauce:context=" + as);
        } catch (RemoteWebDriverProvider.SauceLabsJobHasEnded e) {
            System.err.println("Couldn't set context as Sauce Labs job has ended");
        } catch (WebDriverException e) {
            System.err.println("Unexpected WebDriver error while trying to set context '" + as + "' : " + e.getMessage());
        }
    }

    @Override
    public void afterStory(boolean givenStory) {

        String storyName = this.storyName.get();
        if (storyName.equals("BeforeStories")
                || storyName.equals("AfterStories")
                || storyName.equals("BeforeStory")
                || storyName.equals("AfterStory")
                || storyName.equals("BeforeScenario")
                || storyName.equals("AfterScenario")) {
            return;
        }

        SessionId sessionId = sessionIds.get();

        if (sessionId == null ) {
            // no executed scenarios, as (most likely) excluded
            return;
        }

        boolean pass = passed.get().equals(true);
        String payload = "{ \"passed\":" + pass + "}";
        postJobUpdate(storyName, sessionId, payload);
        System.out.println("Saucelabs Job URL for " + (passed.get() ? "passing" : "failing") + " '" + storyName + "' : " + storyToJobIds.get(storyName));

    }

    private void postJobUpdate(String storyName, SessionId sessionId, String payload) {
        try {

            URL url = new URL("http://saucelabs.com/rest/v1/" + getSauceUser() + "/jobs/" + sessionId.toString());

            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(getSauceUser(), getSauceAccessKey().toCharArray());
                }
            });

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("PUT");

            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(payload);
            writer.close();

            int rc = connection.getResponseCode();
            String jobUrl = null;
            if (rc == 200) {
                jobUrl = readResponseLinesFromSauceLabToGetJobUrl(new BufferedReader(new InputStreamReader(connection.getInputStream())));
                storyToJobIds.put(storyName, jobUrl);

            }
        } catch (IOException e) {
            System.err.println("SauceContextStoryReporter: Error updating Saucelabs job info: " + e.getMessage());
        }
    }

    protected String readResponseLinesFromSauceLabToGetJobUrl(BufferedReader reader) throws IOException {
        String jobUrl = "";
        String responseLineFromSauceLabs;
        while ((responseLineFromSauceLabs = reader.readLine()) != null) {
            jobUrl = jobUrl + processSauceLabsResponseLine(responseLineFromSauceLabs);
        }
        return jobUrl;
    }

    protected String processSauceLabsResponseLine(String responseLineFromSauceLabs) {
        String jobUrl = "";
        Matcher matcher = SAUCE_LABS_VIDEO_URL_PATTERN.matcher(responseLineFromSauceLabs);
        while (matcher.find()) {
            jobUrl = matcher.group().replace("/video.flv", "");
        }
        return jobUrl;
    }

    protected String getBuildId() {
        String buildId =  System.getProperty("BUILD-ID");
        if (buildId != null) {
            return " \"build\":\"" + buildId + "\",";
        }
        return "";
    }

    protected String getJobName() {
        return storyName.get();
    }

    protected String getJobTags() {
        return "";
    }
}
