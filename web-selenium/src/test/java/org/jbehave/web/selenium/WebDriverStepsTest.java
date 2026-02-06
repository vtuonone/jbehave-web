package org.jbehave.web.selenium;

import java.util.Arrays;

import org.jbehave.core.annotations.When;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.*;
import org.jbehave.core.io.StoryLoader;
import org.jbehave.core.reporters.Format;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class WebDriverStepsTest {

    private final WebDriver driver = mock(WebDriver.class);
    private final WebDriverProvider driverProvider = new WebDriverProvider() {
        public WebDriver get() {
            return driver;
        }

        public void initialize() {
        }

        public void end() {
            driver.quit();
        }

        public boolean saveScreenshotTo(String path) {
            return false;
        }
    };

    @Test
    public void canInitializeAndQuitWebDriverBeforeAndAfterScenario() throws Throwable {
        runStory(new MyPerScenarioSteps());
        verify(driver).quit();
    }

    @Test
    public void canInitializeAndQuitWebDriverBeforeAndAfterStory() throws Throwable {
        runStory(new MyPerStorySteps());
        verify(driver).quit();
    }

    @Test
    public void canInitializeAndQuitWebDriverBeforeAndAfterStories() throws Throwable {
        runStory(new MyPerStoriesSteps());
        verify(driver).quit();
    }

    private void runStory(WebDriverSteps steps) {
        final String story = "Scenario: A simple web scenario\n"
            + "When a test is executed\n";
        String storyPath = "/path/to/story";
        StoryLoader storyLoader = new StoryLoader() {
            public String loadResourceAsText(String resourcePath) {
                return resourcePath;
            }
            public String loadStoryAsText(String storyPath) {
                return story;
            }
        };
        Configuration configuration = new MostUsefulConfiguration();
        configuration.useStoryLoader(storyLoader)
                .useStoryReporterBuilder(new StoryReporterBuilder().withFormats(new Format[0]))
                .useStoryControls(new StoryControls().doResetStateBeforeScenario(false));
        Embedder embedder = new Embedder(new StoryMapper(), new PerformableTree(), new SilentEmbedderMonitor());
        embedder.useConfiguration(configuration);
        embedder.useStepsFactory(new InstanceStepsFactory(configuration, steps));
        embedder.runStoriesAsPaths(Arrays.asList(storyPath));
    }

    public class MyPerScenarioSteps extends PerScenarioWebDriverSteps {

        public MyPerScenarioSteps() {
            super(WebDriverStepsTest.this.driverProvider);
        }

        @When("a test is executed")
        public void aTestIsExecuted() {
        }

    };

    public class MyPerStorySteps extends PerStoryWebDriverSteps {

        public MyPerStorySteps() {
            super(WebDriverStepsTest.this.driverProvider);
        }

        @When("a test is executed")
        public void aTestIsExecuted() {
        }

    };

    public class MyPerStoriesSteps extends PerStoriesWebDriverSteps {

        public MyPerStoriesSteps() {
            super(WebDriverStepsTest.this.driverProvider);
        }

        @When("a test is executed")
        public void aTestIsExecuted() {
        }

    };

}
